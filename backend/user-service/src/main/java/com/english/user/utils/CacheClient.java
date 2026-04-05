package com.english.user.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 1.将任意java对象序列化成json并存储在string类型的key中，并且可以设置TTL过期时间
    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    // 2.将任意java对象序列化成json并存储在string类型的key中，并且可以设置逻辑过期时间，用于处理缓存击穿问题
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    // 3.根据指定的key查询缓存，并反序列化为指定类型，利用缓存空值的方式解决缓存穿透问题
    /*
        * keyPrefix  -- 存入redis中的key值前缀
        * id         -- 需要查询的id
        * type       -- 指定对应返回的泛型类型
        * dbFallback -- 调用者传入对应的lambda语句<传入参数类型, 返回值类型>
        * time       -- ttl时间
        * unit       -- ttl单位
    */
    public <R, ID> R queryWithPassThrough(
            String keyPrefix,
            ID id,
            Class<R> type,
            Function<ID, R> dbFallback,
            Long time,
            TimeUnit unit
    ) {
        // 1.查Redis
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2.判断存在
        // 字符串 true，null '' false
        if (StrUtil.isNotBlank(json)) {
            // 3.存在直接返回
            return JSONUtil.toBean(json, type);
        }
        // != null <=> == ''
        if (json != null) {
            // 缓存穿透返回
            return null;
        }
        // 4.不存在查数据库
        R r = dbFallback.apply(id);
        if(r == null) {
            // 5.1 解决缓存穿透 -- 请求数据不存在于Redis与数据库中，缓存不生效，一直访问数据库
            //           缓存null值(内存消耗，不一致性)
            //           布隆过滤器(实现复杂且误判可能)
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        // 6.存在写入Redis
        this.set(key, r, time, unit);
        //7 .返回结果
        return r;
    }


    // 4.根据指定的key查询缓存，并反序列化为指定类型，利用逻辑过期的方式解决缓存击穿问题
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        // 防止拆箱返回null(网络故障 or Redis未响应)
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }


    /*
     * dataKeyPrefix  -- 存入redis中的数据key值前缀
     * lockKeyPrefix  -- 存入redis中的互斥锁key值前缀
     * id         -- 需要查询的id
     * type       -- 指定对应返回的泛型类型
     * dbFallback -- 调用者传入对应的lambda语句<传入参数类型, 返回值类型>
     * time       -- ttl时间
     * unit       -- ttl单位
     */
    public <R, ID> R queryWithLogicalExpire(
            String dataKeyPrefix,
            String lockKeyPrefix,
            ID id,
            Class<R> type,
            Function<ID, R> dbFallback,
            Long time,
            TimeUnit unit
    ) {
        // 0.数据已预热(存入Redis中)
        String key = dataKeyPrefix + id;
        // 1.查Redis
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2.判断缓存是否命中
        if(StrUtil.isBlank(json)) {
            // 3.未命中(说明Redis与数据库中都不存在)，返回错误
            return null;
        }
        // 4.命中，先把Json反序列化->RedisData->getData()(JSONObject)->Shop
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        // 5.判断是否已过期
        LocalDateTime expireTime = redisData.getExpireTime();
        // 5.1.未过期，返回数据
        if(expireTime.isAfter(LocalDateTime.now())) {
            return r;
        }
        // 6.已过期，需要过期重建
        // 6.1.获取互斥锁
        String lockKey = lockKeyPrefix + id;
        boolean isLock = tryLock(lockKey);
        // 6.2.判断是否获取成功
        if(isLock) {
            // 6.3.成功，开启独立线程，实现缓存重建
            // 查Redis
            String json2 = stringRedisTemplate.opsForValue().get(key);
            // 判断缓存是否命中
            if(StrUtil.isBlank(json2)) {
                // 未命中(说明Redis与数据库中都不存在)，返回错误
                return null;
            }
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 查数据库
                    R r1 = dbFallback.apply(id);
                    // 写入Redis
                    this.setWithLogicalExpire(key, r1, time, unit);
                } catch (Exception e){
                    throw new RuntimeException(e);
                } finally {
                    // 释放锁
                    unlock(lockKey);
                }
            });
        }
        // 6.4.不存在，返回过期信息
        return r;
    }

}

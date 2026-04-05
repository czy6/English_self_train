package com.english.langchain4j.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {

    private final StringRedisTemplate stringRedisTemplate;

    // 开始时间戳(2025-01-01 00:00:00)
    private static final long BEGIN_TIMESTAMP = 1735689600L;

    // 序列号的位数
    private static final int COUNT_BITS = 32;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // key代表不同业务的名称
    public long nextId(String keyPrefix) {
        // 0.符号位(固定为0)
        // 1.生成时间戳(31bit)
        long nowSeconds = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSeconds - BEGIN_TIMESTAMP;
        // 2.生成序列号(32bit)
        String nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long count = stringRedisTemplate.opsForValue().increment("english:" + keyPrefix + ":" + nowDate);
        // 3.拼接并返回
        return timestamp << COUNT_BITS | count;
    }

}

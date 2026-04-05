package com.english.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.english.common.utils.JwtTool;
import com.english.common.utils.UserHolder;
import com.english.user.domain.dto.LoginFormDTO;
import com.english.user.domain.po.User;
import com.english.user.domain.po.UserInfo;
import com.english.user.domain.vo.CodeVO;
import com.english.user.domain.vo.Result;
import com.english.user.domain.vo.UserInfoVO;
import com.english.user.domain.vo.UserLoginVO;
import com.english.user.mapper.UserMapper;
import com.english.user.service.IUserInfoService;
import com.english.user.service.IUserService;
import com.english.user.config.JwtProperties;
import com.english.user.utils.RedisConstants;
import com.english.user.utils.RegexUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author 虎哥
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final IUserInfoService userInfoService;
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtTool jwtTool;
    private final JwtProperties jwtProperties;

    @Override
    public Result sendCode(String phone) {
        // 1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式有误！！！");
        }
        // 2.生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 3.保存验证码至redis
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // 4.返回验证码
        CodeVO codeVO = new CodeVO();
        codeVO.setCode(code);
        return Result.ok(codeVO);
    }

    @Override
    public Result login(LoginFormDTO loginForm) {
        // 1.校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式有误");
        }
        // 2.校验验证码
        // 2.1取出验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if(cacheCode == null || !cacheCode.equals(code)) {
            // 3.不一致返回错误信息
            return Result.fail("验证码有误");
        }
        // 4.一致根据手机号查询用户信息
        User user = query().eq("phone", phone).one();
        // 5.用户不存在，则创建新用户保存至数据库
        if (user == null) {
            IUserService currentProxy = (IUserService) AopContext.currentProxy();
            user = currentProxy.createUserByPhone(phone);
        }
        
        // 6.生成双 Token
        String accessToken = jwtTool.createAccessToken(user.getId());
        String refreshToken = jwtTool.createRefreshToken(user.getId());
        
        // 7.将 Refresh Token 存入 Redis（用于注销和校验）
        String refreshTokenKey = RedisConstants.USER_REFRESH_TOKEN_KEY + user.getId();
        stringRedisTemplate.opsForValue().set(
            refreshTokenKey, 
            refreshToken, 
            jwtProperties.getRefreshTokenTTL().toMillis(), 
            TimeUnit.MILLISECONDS
        );
        
        // 8.返回双 Token
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setAccessToken(accessToken);
        userLoginVO.setRefreshToken(refreshToken);
        userLoginVO.setAccessTokenTTL(jwtProperties.getAccessTokenTTL().getSeconds());
        return Result.ok(userLoginVO);
    }

    @Transactional
    public User createUserByPhone(String phone) {
        // 1.创建用户 -> english_user表
        User user = new User();
        user.setPhone(phone);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        save(user);

        // 2.创建用户信息 -> english_user_info表
        UserInfo userInfo = new UserInfo();
        userInfo.setNickName("user_" + RandomUtil.randomString(5));
        userInfo.setAvatar("https://javaweb-warehouse.oss-cn-beijing.aliyuncs.com/default-avatar.png");
        userInfo.setLevel(1);
        userInfo.setCreateTime(LocalDateTime.now());
        userInfo.setUpdateTime(LocalDateTime.now());
        userInfoService.save(userInfo);

        return user;
    }

    @Override
    public Result getUserInfo() {
        Long userId = UserHolder.getUserId();
        User user = getById(userId);
        UserInfo userInfo = userInfoService.getById(userId);
        UserInfoVO userInfoVO = BeanUtil.copyProperties(userInfo, UserInfoVO.class);
        userInfoVO.setPhone(user.getPhone());
        return Result.ok(userInfoVO);
    }

}

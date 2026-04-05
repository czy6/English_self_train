package com.english.user.controller;

import com.english.common.utils.JwtTool;
import com.english.user.domain.dto.UserInfoDTO;
import com.english.user.domain.vo.Result;
import com.english.user.service.IUserInfoService;
import com.english.user.service.IUserService;
import com.english.user.domain.dto.LoginFormDTO;
import com.english.user.utils.RedisConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "用户相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final IUserInfoService userInfoService;
    private final JwtTool jwtTool;
    private final StringRedisTemplate stringRedisTemplate;

    @ApiOperation("验证码接口")
    @GetMapping("/code")
    public Result sendCode(@RequestParam("phone") String phone) {
        return userService.sendCode(phone);
    }

    @ApiOperation("登录接口")
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm) {
        return userService.login(loginForm);
    }

    @ApiOperation("用户信息接口")
    @GetMapping("/info")
    public Result getUserInfo() {
        return userService.getUserInfo();
    }

    @ApiOperation("修改用户信息接口")
    @PutMapping("/info")
    public Result updateUserInfo(@RequestBody UserInfoDTO userInfo) {
        return userInfoService.updateUserInfo(userInfo);
    }

    @ApiOperation("刷新 Token 接口")
    @PostMapping("/refresh")
    public Result refresh(@RequestHeader("Authorization") String refreshToken) {
        // 去掉前缀
        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }
        
        try {
            // 1. 校验 Refresh Token
            Long userId = jwtTool.parseToken(refreshToken, "refresh");
            
            // 2. 校验 Redis 中是否存在（防止注销后仍可使用）
            String refreshTokenKey = RedisConstants.USER_REFRESH_TOKEN_KEY + userId;
            String cachedToken = stringRedisTemplate.opsForValue().get(refreshTokenKey);
            
            if (cachedToken == null || !cachedToken.equals(refreshToken)) {
                return Result.fail("Refresh Token 已失效");
            }
            
            // 3. 生成新的 Access Token
            String newAccessToken = jwtTool.createAccessToken(userId);
            
            // 4. 返回
            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", newAccessToken);
            result.put("accessTokenTTL", 1800L); // 30 分钟
            
            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail("刷新失败，请重新登录");
        }
    }

    @ApiOperation("注销登录")
    @PostMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String refreshToken) {
        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }
        
        try {
            Long userId = jwtTool.parseToken(refreshToken, "refresh");
            // 删除 Redis 中的 Refresh Token
            String refreshTokenKey = RedisConstants.USER_REFRESH_TOKEN_KEY + userId;
            stringRedisTemplate.delete(refreshTokenKey);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail("注销失败");
        }
    }

}


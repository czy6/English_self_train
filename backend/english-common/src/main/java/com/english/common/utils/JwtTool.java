package com.english.common.utils;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.english.common.exception.UnauthorizedException;

import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class JwtTool {
    private final JWTSigner jwtSigner;
    private final Duration accessTokenTTL;
    private final Duration refreshTokenTTL;
    private final Duration renewThreshold;

    public JwtTool(KeyPair keyPair, Duration accessTokenTTL, Duration refreshTokenTTL, Duration renewThreshold) {
        this.jwtSigner = JWTSignerUtil.createSigner("rs256", keyPair);
        this.accessTokenTTL = accessTokenTTL;
        this.refreshTokenTTL = refreshTokenTTL;
        this.renewThreshold = renewThreshold;
    }

    /**
     * 创建 Access Token
     */
    public String createAccessToken(Long userId) {
        return JWT.create()
                .setPayload("user", userId)
                .setPayload("type", "access")
                .setExpiresAt(new Date(System.currentTimeMillis() + accessTokenTTL.toMillis()))
                .setSigner(jwtSigner)
                .sign();
    }

    /**
     * 创建 Refresh Token
     */
    public String createRefreshToken(Long userId) {
        return JWT.create()
                .setPayload("user", userId)
                .setPayload("type", "refresh")
                .setExpiresAt(new Date(System.currentTimeMillis() + refreshTokenTTL.toMillis()))
                .setSigner(jwtSigner)
                .sign();
    }

    /**
     * 解析 token（支持 access 和 refresh）
     *
     * @param token token
     * @param expectedType 期望的 token 类型（access/refresh），为 null 时不校验类型
     * @return 解析得到的用户信息
     */
    public Long parseToken(String token, String expectedType) {
        // 1.校验 token 是否为空
        if (token == null) {
            throw new UnauthorizedException("未登录");
        }
        // 2.校验并解析 jwt
        JWT jwt;
        try {
            jwt = JWT.of(token).setSigner(jwtSigner);
        } catch (Exception e) {
            throw new UnauthorizedException("无效的 token", e);
        }
        // 2.校验 jwt 是否有效
        if (!jwt.verify()) {
            throw new UnauthorizedException("无效的 token");
        }
        // 3.校验类型
        String type = (String) jwt.getPayload("type");
        if (expectedType != null && !expectedType.equals(type)) {
            throw new UnauthorizedException("Token 类型错误");
        }
        // 4.校验是否过期
        try {
            JWTValidator.of(jwt).validateDate();
        } catch (ValidateException e) {
            throw new UnauthorizedException("Token 已经过期");
        }
        // 5.数据格式校验
        Object userPayload = jwt.getPayload("user");
        if (userPayload == null) {
            throw new UnauthorizedException("无效的 token");
        }

        // 6.数据解析
        try {
           return Long.valueOf(userPayload.toString());
        } catch (RuntimeException e) {
            throw new UnauthorizedException("无效的 token");
        }
    }

    /**
     * 判断 Access Token 是否需要续期
     * 剩余时间 < renewThreshold 时续期
     */
    public boolean shouldRenew(String token) {
        try {
            JWT jwt = JWT.of(token).setSigner(jwtSigner);
            Date expires = (Date) jwt.getPayload("exp");
            if (expires == null) return false;
            
            Instant renewTime = expires.toInstant().minus(renewThreshold);
            return Instant.now().isAfter(renewTime);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 刷新 Token（使用 Refresh Token 生成新的 Access Token）
     */
    public String refreshAccessToken(String refreshToken) {
        Long userId = parseToken(refreshToken, "refresh");
        return createAccessToken(userId);
    }

}

package com.english.user.domain.vo;

import lombok.Data;

@Data
public class UserLoginVO {
    private String accessToken;
    private String refreshToken;
    private Long accessTokenTTL; // Access Token 有效期（秒）
}

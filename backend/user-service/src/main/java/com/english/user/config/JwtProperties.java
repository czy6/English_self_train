package com.english.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "hm.jwt")
public class JwtProperties {
    private Resource location;
    private String password;
    private String alias;
    
    // Access Token 有效期：30 分钟
    private Duration accessTokenTTL = Duration.ofMinutes(30);
    
    // Refresh Token 有效期：7 天
    private Duration refreshTokenTTL = Duration.ofDays(7);
    
    // 续期阈值：剩余时间 < 5 分钟时续期
    private Duration renewThreshold = Duration.ofMinutes(5);
}

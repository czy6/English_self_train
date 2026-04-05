package com.english.common.interceptors;


import cn.hutool.core.util.StrUtil;
import com.english.common.utils.JwtTool;
import com.english.common.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInfoInterceptor implements HandlerInterceptor {

    private final JwtTool jwtTool;

    public UserInfoInterceptor(JwtTool jwtTool) {
        this.jwtTool = jwtTool;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取登录信息（从 Header 中的 Authorization）
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isBlank(authorization)) {
            throw new RuntimeException("未登录");
        }
        
        // 去掉 "Bearer " 前缀
        String accessToken = authorization;
        if (authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring(7);
        }
        
        // 2.解析并校验 Token
        Long userId = jwtTool.parseToken(accessToken, "access");
        UserHolder.setUserId(userId);
        
        // 3.判断是否需要续期
        if (jwtTool.shouldRenew(accessToken)) {
            // 3.1 生成新 Token
            String newAccessToken = jwtTool.createAccessToken(userId);
            
            // 3.2 通过响应头返回给前端
            response.setHeader("X-New-Access-Token", newAccessToken);
        }
        
        // 4.放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理用户
        UserHolder.removeUserId();
    }
}

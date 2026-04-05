package com.english.langchain4j.interceptors;

import cn.hutool.core.util.StrUtil;
import com.english.langchain4j.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取登录信息
        String userInfo = request.getHeader("user-info");
        // 2.存入ThreadLocal
        if (StrUtil.isNotBlank(userInfo)) {
            UserHolder.setUserId(Long.valueOf(userInfo));
        }
        // 3.放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理用户
        UserHolder.removeUserId();
    }
}

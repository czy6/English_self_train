package com.english.common.config;

import com.english.common.interceptors.UserInfoInterceptor;
import com.english.common.utils.JwtTool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnClass(DispatcherServlet.class)
public class MvcConfig implements WebMvcConfigurer {
    
    private final JwtTool jwtTool;

    public MvcConfig(JwtTool jwtTool) {
        this.jwtTool = jwtTool;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInfoInterceptor(jwtTool))
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/user/login",      // 登录接口
                    "/user/code",       // 发送验证码
                    "/user/refresh",    // 刷新 Token
                    "/error",           // 错误页面
                    "/question/dict",   // 查词接口（百炼智能体调用）
                    "/question/wrong-questions",  // 错题接口（百炼智能体调用）
                    "/question/user/favorite/detail",  // 收藏题详情接口（百炼智能体调用）
                    "/question/user/train/record",  // 答题记录接口（百炼智能体调用）
                    "/question/user/weakness/analysis",  // 薄弱点分析接口（百炼智能体调用）
                    "/question/batch/detail",  // 批量查询试题接口（百炼智能体调用）
                    "/question/type/list"  // 题型列表接口（百炼智能体调用）
                );
    }
}

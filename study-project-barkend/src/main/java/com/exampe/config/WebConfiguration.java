package com.exampe.config;

import com.exampe.interceptor.Authorizelnterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Resource
    Authorizelnterceptor authorizelnterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加自定义的权限拦截器，拦截所有的请求
        registry.
                // 添加自定义的权限拦截器
                        addInterceptor(authorizelnterceptor)
                // 拦截所有的请求，可以根据需要调整拦截的路径，例如只拦截 /api/** 的请求
                .addPathPatterns("/**")
                // 排除不需要拦截的路径，例如根路径、登录相关路径和静态资源
                .excludePathPatterns("/", "/api/auth/**");
    }
}

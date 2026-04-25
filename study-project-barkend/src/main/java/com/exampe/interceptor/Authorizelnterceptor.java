package com.exampe.interceptor;


import com.exampe.config.SecurityConfiguration;
import com.exampe.entity.user.AccountUser;
import com.exampe.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class Authorizelnterceptor implements HandlerInterceptor {

    @Resource
    UserMapper userMapper;

    @Override
    // 这里可以获取到当前的用户信息，进行权限校验
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取当前的用户信息
        SecurityContext context = SecurityContextHolder.getContext();
        // 获取当前的认证信息
        Authentication authentication = context.getAuthentication();
        // 这里可以进行权限校验，如果没有权限，可以返回 false，拒绝访问
        User user = (User) authentication.getPrincipal();
        String username = user.getUsername();
        AccountUser accountUser = userMapper.findAccountUserByNameOrEmail(username);
        request.getSession().setAttribute("account", accountUser);
        return true;
    }
}

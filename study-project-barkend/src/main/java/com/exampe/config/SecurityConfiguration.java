package com.exampe.config;

import com.alibaba.fastjson2.JSONObject;
import com.exampe.entity.RestBean;
import com.exampe.service.AuthorizeService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Resource
    AuthorizeService authorizeService;

    @Resource
    DataSource dataSource;


    @Bean
    // 配置 SecurityFilterChain，定义安全过滤链的规则和行为
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // 配置安全过滤链，定义安全规则和行为
        // 禁用 CSRF 保护，因为这是一个后端 API，前端可能无法处理 CSRF token；如果需要启用，请改为 .csrf().enable() 并在前端携带 token
        return httpSecurity
                // 配置请求授权规则，允许访问根路径、登录相关路径和静态资源，其他请求需要认证
                .authorizeHttpRequests(auth -> auth
                        // 允许访问根路径、登录相关路径
                        .requestMatchers("/", "/api/auth/**").permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated())
                // 配置表单登录，指定登录处理 URL 和认证成功/失败的处理器
                .formLogin(form -> form
                        // 指定登录处理 URL，前端发送登录请求时应该使用这个 URL，例如 POST /api/auth/login
                        .loginProcessingUrl("/api/auth/login")
                        // 认证成功处理器，返回 JSON 格式的成功响应
                        .successHandler(this::onAuthenticationSuccess)
                        // 认证失败处理器，返回 JSON 格式的失败响应，包含错误信息
                        .failureHandler(this::onAuthenticationFailure))
                // 配置注销，指定注销 URL
                .logout(logout -> logout
                        // 指定注销 URL，前端发送注销请求时应该使用这个 URL，例如 POST /api/auth/logout
                        .logoutUrl("/api/auth/logout").
                        // 注销成功处理器，返回 JSON 格式的成功响应
                                logoutSuccessHandler(this::onAuthenticationSuccess))
                .rememberMe(remember -> remember.
                        // 指定 remember-me 参数名称，前端发送登录请求时可以携带这个参数来启用记住我功能，例如 remember=true
                                rememberMeParameter("remember")
                        // 设置 remember-me token 的有效期，例如 7 天
                        .tokenValiditySeconds(3 * 24 * 60 * 60)
                        .tokenRepository(this.tokenRepository())
                )

                // 禁用 CSRF 保护，因为这是一个后端 API，前端可能无法处理 CSRF token；如果需要启用，请改为 .csrf().enable() 并在前端携带 token
                .csrf(AbstractHttpConfigurer::disable)
                // 配置 CORS（跨域资源共享）规则，允许所有来源、方法和请求头，并允许携带凭证（如 Cookies）
                .cors(cors -> cors
                        // 配置 CORS 规则，允许所有来源、方法和请求头，并允许携带凭证（如 Cookies）
                        .configurationSource(this.corsConfigurationSource()))
                // 配置异常处理，指定认证失败的处理器，返回 JSON 格式的失败响应，包含错误信息
                .exceptionHandling(ex -> ex.
                        // 认证失败处理器，返回 JSON 格式的失败响应，包含错误信息
                                authenticationEntryPoint(this::onAuthenticationFailure))
                .build();
    }

    @Bean
    // 配置 remember-me 功能，使用 JDBC 存储 remember-me token，并在应用启动时创建相应的数据库表
    public PersistentTokenRepository tokenRepository() {
        // 创建 JdbcTokenRepositoryImpl 对象，设置数据源和在应用启动时创建数据库表的选项
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        // 设置数据源，JdbcTokenRepositoryImpl 将使用这个数据源来连接数据库并存储 remember-me token
        tokenRepository.setDataSource(dataSource);
        // 设置在应用启动时创建数据库表的选项，如果表已经存在则会抛出异常；如果需要手动创建表，请将这个选项设置为 false，并使用提供的 SQL 脚本创建表
        tokenRepository.setCreateTableOnStartup(false);
        // 返回配置好的 PersistentTokenRepository 对象，供 remember-me 功能使用
        return tokenRepository;
    }

    // 配置 CORS（跨域资源共享）规则，允许所有来源、方法和请求头，并允许携带凭证（如 Cookies）
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 如果允许携带 Cookie/凭证，请不要使用 addAllowedOrigin("*")，使用 pattern 或指定来源
        config.addAllowedOriginPattern("*");
        // 允许所有 HTTP 方法（GET、POST、PUT、DELETE 等）
        config.addAllowedMethod("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许携带凭证（如 Cookies）
        config.setAllowCredentials(true);
        // 配置 CORS 规则，注册到 URL 模式为 /** 的路径上，表示对所有路径生效
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 将 CORS 规则注册到 URL 模式为 /** 的路径上，表示对所有路径生效
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    @Bean
    // 配置 AuthenticationManager，使用 AuthorizeService 作为用户详情服务来进行认证
    public AuthenticationManager authenticationManager(HttpSecurity httpSecurity) throws Exception {
        // 获取 AuthenticationManagerBuilder 对象，并配置使用 AuthorizeService 作为用户详情服务
        AuthenticationManagerBuilder builder = httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(authorizeService);
        return builder.build();
    }


    // 配置密码编码器，使用 BCrypt 算法对密码进行加密和验证
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 认证成功处理器，返回 JSON 格式的成功响应
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setCharacterEncoding("UTF-8");
        if (request.getRequestURI().equals("/api/auth/logout")) {
            // 注销成功，返回 JSON 格式的成功响应
            response.getWriter().write(JSONObject.toJSONString(RestBean.success("注销成功")));
        } else {
            // 登录成功，返回 JSON 格式的成功响应
            response.getWriter().write(JSONObject.toJSONString(RestBean.success("登录成功")));
        }
    }

    // 认证失败处理器，返回 JSON 格式的失败响应，包含错误信息
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JSONObject.toJSONString(RestBean.failure(401, exception.getMessage())));
    }

}

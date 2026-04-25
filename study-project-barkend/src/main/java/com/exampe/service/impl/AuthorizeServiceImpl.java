package com.exampe.service.impl;

import com.exampe.entity.auth.Account;
import com.exampe.mapper.UserMapper;
import com.exampe.service.AuthorizeService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class AuthorizeServiceImpl implements AuthorizeService, UserDetailsService {

    @Resource
    UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.trim().isEmpty()) {
            throw new UsernameNotFoundException("用户名不能为空");
        }
        Account accountByNameOrEmail = userMapper.findAccountByNameOrEmail(username);
        if (accountByNameOrEmail == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return User
                .withUsername(accountByNameOrEmail.getUsername())
                .password(accountByNameOrEmail.getPassword())
                .roles("user")
                .build();
    }

    @Resource
    MailSender mailSender;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Value("${spring.mail.username}")
    String fromEmail;


    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 1.生成对应邮箱的验证码，
     * 2.邮箱和对应的验证码存储在redis里面，并设置过期时间（三分钟，如果此时重新要求发邮件
     * 只要剩余时间低于2分钟，就可以重新发送一次，重复此流程）
     * 3.发送邮件，邮件内容包含验证码
     * 4.如果发送失败，把redis里面的验证码删除
     * 5.用户在注册时，再把redis里面去除对应值，然后看验证码是否一致
     */
    @Override
    public String sendValidateEmail(String email, String sessionId, boolean hasAccount) {
        String key = "email" + sessionId + ":" + email + ":" + (hasAccount);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            Long expire = Optional.ofNullable(stringRedisTemplate.getExpire(key, TimeUnit.SECONDS)).orElse(0L);
            if (expire > 120) {
                return "请求过于频繁，请稍后再试";
            }
        }

        Account account = userMapper.findAccountByNameOrEmail(email);

        if (hasAccount && account == null) {
            return "该邮箱未注册";
        }
        if (!hasAccount && account != null) {
            return "该邮箱已被注册";
        }

        String format = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("验证码");
        message.setText("您的验证码是" + format + "，请在三分钟内使用");
        try {
            mailSender.send(message);
            // 按照密码进行存储，并设置有效期为 3 分钟。
            stringRedisTemplate.opsForValue().set(key, format, 3, TimeUnit.MINUTES);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return "邮件发送失败，请联系管理员";
        }
    }

    @Override
    public String validateAndRegister(String username, String password, String email, String code, String sessionId) {
        String key = "email" + sessionId + ":" + email + ":" + "false";

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey((key)))) {
            String string = stringRedisTemplate.opsForValue().get(key);

            Account accountByNameOrEmail = userMapper.findAccountByNameOrEmail(username);
            if (accountByNameOrEmail != null) {
                return "用户名已存在，请重新输入";
            }
            if (string == null) {
                stringRedisTemplate.delete(key);
                return "验证码已过期，请重新获取";
            }
            if (string.equals(code)) {
                password = passwordEncoder.encode(password);
                int account = userMapper.createAccount(username, password, email);
                if (account > 0) {
                    return null;
                } else {
                    return "注册失败，请稍后再试";
                }
            } else {
                return "验证码错误，请重新输入";
            }
        } else {
            return "请先获取验证码";
        }
    }

    @Override
    public String validateOnly(String email, String code, String sessionId) {
        String key = "email" + sessionId + ":" + email + ":true";
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey((key)))) {
            String string = stringRedisTemplate.opsForValue().get(key);
            if (string == null) {
                return "验证码已过期，请重新获取";
            }
            if (string.equals(code)) {
                stringRedisTemplate.delete(key);
                return null;
            } else {
                return "验证码错误，请重新输入";
            }
        } else {
            return "请先获取验证码";
        }
    }

    @Override
    public boolean resetPassword(String email, String newPassword) {
        // 对新密码进行加密
        newPassword = passwordEncoder.encode(newPassword);
        return userMapper.restPasswordByEmail(email, newPassword) > 0;
    }


}

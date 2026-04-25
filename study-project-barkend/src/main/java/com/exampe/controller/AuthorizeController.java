package com.exampe.controller;

import com.exampe.entity.RestBean;
import com.exampe.service.AuthorizeService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {

    private final String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,}$";
    private final String usernameRegex = "^[A-Za-z0-9\\p{IsHan}]+$";


    @Resource
    AuthorizeService authorizeService;


    @PostMapping("/validate-register-email")
    public RestBean<String> validateRegisterEmail(@Pattern(regexp = emailRegex) @RequestParam("email") String email, HttpSession session) {
        String string = authorizeService.sendValidateEmail(email, session.getId(), false);
        if (string == null) {
            return RestBean.success("邮件发送成功，请查收");
        } else {
            return RestBean.failure(400, string);
        }
    }


    @PostMapping("/validate-rest-email")
    public RestBean<String> validateRestEmail(@Pattern(regexp = emailRegex) @RequestParam("email") String email, HttpSession session) {

        String string = authorizeService.sendValidateEmail(email, session.getId(), true);
        if (string == null) {
            return RestBean.success("邮件发送成功，请查收");
        } else {
            return RestBean.failure(400, string);
        }
    }


    @PostMapping("/register")
    public RestBean<String> register(
            @Pattern(regexp = usernameRegex) @Length(min = 3, max = 8) @RequestParam("username") String username,
            @Length(min = 6, max = 16) @RequestParam("password") String password,
            @Pattern(regexp = emailRegex) @RequestParam("email") String email,
            @Length(min = 6, max = 6) @RequestParam("code") String code,
            HttpSession session) {

        String string = authorizeService.validateAndRegister(username, password, email, code, session.getId());
        if (string == null) {
            return RestBean.success("注册成功，请登录");
        } else {
            return RestBean.failure(400, string);
        }
    }

    /**
     * 1.用户输入邮箱，点击发送验证码
     * 2.验证码是否正确，正确就在Session里面设置一个标志，表示这个邮箱已经验证过了
     * 3.用户发起重置密码的请求，如果存在标记，允许重置密码
     */
    @PostMapping("/start-reset")
    public RestBean<String> startReset(@Pattern(regexp = emailRegex)
                                       @RequestParam("email") String email, @Length(min = 6, max = 6) @RequestParam("code") String code,
                                       HttpSession session) {

        String string = authorizeService.validateOnly(email, code, session.getId());
        if (string == null) {
            session.setAttribute("rest-password", email);
            return RestBean.success("验证成功，请继续重置密码");
        } else {
            return RestBean.failure(400, string);
        }
    }

    @PostMapping("/do-rest")
    public RestBean<String> resetPassword(@Length(min = 6, max = 16) @RequestParam("password") String password, HttpSession session) {
        String email = (String) session.getAttribute("rest-password");
        if (email == null) {
            return RestBean.failure(400, "请先验证邮箱");
        } else if (authorizeService.resetPassword(email, password)) {
            session.removeAttribute("rest-password");
            return RestBean.success("密码重置成功，请使用新密码登录");
        } else {
            return RestBean.failure(400, "重置密码失败，请稍后再试");
        }
    }

}

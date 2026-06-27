package com.worksshow.controller;

import com.worksshow.common.Result;
import com.worksshow.dto.ChangePasswordRequest;
import com.worksshow.dto.LoginRequest;
import com.worksshow.dto.LoginResponse;
import com.worksshow.dto.RegisterRequest;
import com.worksshow.dto.ResetPasswordRequest;
import com.worksshow.dto.SendCodeRequest;
import com.worksshow.dto.UpdateProfileRequest;
import com.worksshow.entity.User;
import com.worksshow.service.EmailCodeService;
import com.worksshow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口
 * <p>
 * 提供注册、登录、获取当前用户信息、更新资料、修改密码、退出登录、发送验证码接口。
 * 实际访问路径前缀为 /api(由 server.servlet.context-path 配置)。
 * <ul>
 *   <li>POST /api/user/sendCode   发送邮箱验证码(公开)</li>
 *   <li>POST /api/user/register   注册(公开)</li>
 *   <li>POST /api/user/login      登录(公开)</li>
 *   <li>GET  /api/user/info        获取当前用户(需登录)</li>
 *   <li>PUT  /api/user/info        更新用户资料(需登录)</li>
 *   <li>PUT  /api/user/password    修改密码(需登录)</li>
 *   <li>POST /api/user/forgot-password/send-code  发送重置密码验证码(公开)</li>
 *   <li>POST /api/user/forgot-password/reset      通过邮箱验证码重置密码(公开)</li>
 *   <li>POST /api/user/logout      退出登录(需登录)</li>
 * </ul>
 *
 * @author WorksShow
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailCodeService emailCodeService;

    /**
     * 发送邮箱验证码(注册前调用)
     */
    @PostMapping("/sendCode")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        log.info("发送验证码请求: email={}", request.getEmail());
        emailCodeService.sendCode(request.getEmail());
        return Result.ok("验证码已发送,请查收邮件", null);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        log.info("注册请求: nickname={}", request.getNickname());
        userService.register(request);
        return Result.ok("注册成功", null);
    }

    /**
     * 用户登录,返回 JWT token(支持手机号/邮箱登录)
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("登录请求: account={}", request.getAccount());
        return Result.ok("登录成功", userService.login(request));
    }

    /**
     * 获取当前登录用户信息(需携带 token)
     */
    @GetMapping("/info")
    public Result<User> info() {
        return Result.ok(userService.getCurrentUser());
    }

    /**
     * 更新当前用户资料(昵称、头像、性别,需携带 token)
     */
    @PutMapping("/info")
    public Result<User> updateInfo(@Valid @RequestBody UpdateProfileRequest request) {
        log.info("更新用户资料请求: nickname={}", request.getNickname());
        return Result.ok("资料更新成功", userService.updateProfile(request));
    }

    /**
     * 修改密码(需携带 token)
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("修改密码请求");
        userService.changePassword(request);
        return Result.ok("密码修改成功", null);
    }

    /**
     * 发送重置密码验证码到指定邮箱(公开接口,无需登录)
     */
    @PostMapping("/forgot-password/send-code")
    public Result<Void> sendForgotPasswordCode(@Valid @RequestBody SendCodeRequest request) {
        log.info("发送重置密码验证码请求: email={}", request.getEmail());
        userService.sendForgotPasswordCode(request.getEmail());
        return Result.ok("验证码已发送至邮箱,请查收", null);
    }

    /**
     * 通过邮箱验证码重置密码(公开接口,无需登录)
     */
    @PostMapping("/forgot-password/reset")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("邮箱验证码重置密码请求: email={}", request.getEmail());
        userService.resetPasswordByEmail(request);
        return Result.ok("密码重置成功", null);
    }

    /**
     * 退出登录
     * JWT 为无状态,服务端不存储,客户端清除本地 token 即可。
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok("已退出登录", null);
    }
}

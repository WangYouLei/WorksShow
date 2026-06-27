package com.worksshow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.worksshow.dto.ChangePasswordRequest;
import com.worksshow.dto.LoginRequest;
import com.worksshow.dto.LoginResponse;
import com.worksshow.dto.RegisterRequest;
import com.worksshow.dto.ResetPasswordRequest;
import com.worksshow.dto.UpdateProfileRequest;
import com.worksshow.entity.User;

/**
 * 用户服务接口
 * <p>
 * 继承 Mybatis-Plus 的 IService,获得单表 Service 层 CRUD 能力。
 * 额外定义注册、登录、获取当前用户、更新资料、修改密码等业务方法。
 *
 * @author WorksShow
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param request 注册参数(昵称、密码、邮箱、手机号、验证码)
     */
    void register(RegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录参数(账号、密码)
     * @return 登录响应(含 token、用户信息)
     */
    LoginResponse login(LoginRequest request);

    /**
     * 获取当前登录用户信息
     *
     * @return 用户实体(不含密码)
     */
    User getCurrentUser();

    /**
     * 更新当前用户资料(昵称、头像、性别)
     *
     * @param request 更新参数
     * @return 更新后的用户信息(不含密码)
     */
    User updateProfile(UpdateProfileRequest request);

    /**
     * 修改当前用户密码
     *
     * @param request 修改密码参数(原密码、新密码)
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * 发送重置密码验证码到指定邮箱(公开接口,邮箱需已注册)
     *
     * @param email 目标邮箱
     */
    void sendForgotPasswordCode(String email);

    /**
     * 通过邮箱验证码重置密码(公开接口)
     *
     * @param request 重置参数(邮箱、验证码、新密码)
     */
    void resetPasswordByEmail(ResetPasswordRequest request);
}

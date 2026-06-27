package com.worksshow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.worksshow.dto.ChangePasswordRequest;
import com.worksshow.dto.LoginRequest;
import com.worksshow.dto.LoginResponse;
import com.worksshow.dto.RegisterRequest;
import com.worksshow.dto.ResetPasswordRequest;
import com.worksshow.dto.UpdateProfileRequest;
import com.worksshow.entity.User;
import com.worksshow.exception.BusinessException;
import com.worksshow.mapper.UserMapper;
import com.worksshow.security.JwtUtils;
import com.worksshow.security.UserContext;
import com.worksshow.service.EmailCodeService;
import com.worksshow.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现
 * <p>
 * 实现注册、登录、获取当前用户的核心业务逻辑:
 * - 注册: 校验邮箱/手机号唯一性,验证邮箱验证码,密码 BCrypt 加密后入库
 * - 登录: 按手机号或邮箱查询,BCrypt 比对密码,通过后签发 JWT
 * - 获取当前用户: 从 UserContext 读取 userId,回查用户信息
 *
 * @author WorksShow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtils jwtUtils;
    private final EmailCodeService emailCodeService;

    /** BCrypt 密码编码器(默认强度 10) */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        // 1. 校验邮箱是否已被注册
        long emailCount = count(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()));
        if (emailCount > 0) {
            log.warn("注册失败,邮箱已被注册: {}", request.getEmail());
            throw new BusinessException(400, "邮箱已被注册");
        }

        // 2. 校验手机号是否已被注册
        long phoneCount = count(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, request.getPhone()));
        if (phoneCount > 0) {
            log.warn("注册失败,手机号已被注册: {}", request.getPhone());
            throw new BusinessException(400, "手机号已被注册");
        }

        // 3. 构造用户实体,密码 BCrypt 加密
        User user = new User();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname());
        user.setGender(0);                       // 默认未知
        user.setStatus(1);                       // 默认启用

        // 4. 入库(若并发竞态触发 DuplicateKeyException,事务回滚,验证码尚未消费)
        save(user);

        // 5. 校验邮箱验证码(校验成功后自动删除,一次性使用)
        //    放在 save 之后:若 save 失败则验证码不会被消费;若验证码错误则事务回滚(含 save)
        emailCodeService.verifyCode(request.getEmail(), request.getCode());

        log.info("用户注册成功: id={}, nickname={}", user.getId(), user.getNickname());
    }

    /**
     * 用户登录(支持手机号或邮箱登录)
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 按手机号或邮箱查询用户(用 .and() 嵌套 OR,确保逻辑删除条件 deleted=0 同时作用于两个分支)
        User user = getOne(new LambdaQueryWrapper<User>()
                .and(wrapper -> wrapper
                        .eq(User::getEmail, request.getAccount())
                        .or()
                        .eq(User::getPhone, request.getAccount())));
        if (user == null) {
            // 出于安全,用户不存在与密码错误返回相同提示,避免账号枚举
            log.warn("登录失败,账号不存在: {}", request.getAccount());
            throw new BusinessException(400, "账号或密码错误");
        }

        // 2. BCrypt 比对密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("登录失败,密码错误: account={}", request.getAccount());
            throw new BusinessException(400, "账号或密码错误");
        }

        // 3. 校验账号状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            log.warn("登录失败,账号已禁用: account={}", request.getAccount());
            throw new BusinessException(403, "账号已被禁用,请联系管理员");
        }

        // 4. 签发 JWT 并返回登录响应
        String token = jwtUtils.generateToken(user.getId(), user.getNickname());
        log.info("用户登录成功: id={}, nickname={}", user.getId(), user.getNickname());
        return new LoginResponse(token, user.getId(), user.getNickname());
    }

    /**
     * 获取当前登录用户信息
     */
    @Override
    public User getCurrentUser() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已失效");
        }

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // password 字段虽已 @JsonIgnore,此处再置空做双保险,防止意外泄露
        user.setPassword(null);
        return user;
    }

    /**
     * 更新当前用户资料(昵称、性别)
     */
    @Override
    public User updateProfile(UpdateProfileRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已失效");
        }

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 仅更新允许修改的字段(邮箱/手机号为登录账号,不在此接口修改)
        user.setNickname(request.getNickname());
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        updateById(user);
        log.info("用户资料更新成功: id={}, nickname={}", user.getId(), user.getNickname());

        user.setPassword(null);
        return user;
    }

    /**
     * 修改当前用户密码
     */
    @Override
    public void changePassword(ChangePasswordRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已失效");
        }

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 校验原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("修改密码失败,原密码错误: userId={}", userId);
            throw new BusinessException(400, "原密码错误");
        }

        // 更新为新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        updateById(user);
        log.info("用户密码修改成功: userId={}", userId);
    }

    /**
     * 发送重置密码验证码到指定邮箱(公开接口)
     */
    @Override
    public void sendForgotPasswordCode(String email) {
        emailCodeService.sendCodeForReset(email);
        log.info("重置密码验证码已发送: email={}", email);
    }

    /**
     * 通过邮箱验证码重置密码(公开接口)
     */
    @Override
    public void resetPasswordByEmail(ResetPasswordRequest request) {
        String email = request.getEmail();

        // 校验邮箱验证码(校验成功后自动删除,一次性使用)
        emailCodeService.verifyCode(email, request.getCode());

        // 通过邮箱查询用户
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .last("LIMIT 1"));
        if (user == null) {
            // 理论不会走到这里(sendCodeForReset 已校验已注册),防御性处理
            throw new BusinessException(404, "该邮箱未注册");
        }

        // 更新为新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        updateById(user);
        log.info("用户通过邮箱验证码重置密码成功: userId={}, email={}", user.getId(), email);
    }
}

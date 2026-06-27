package com.worksshow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求参数
 *
 * @author WorksShow
 */
@Data
public class RegisterRequest {

    /** 昵称(展示名,2-20 字符) */
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 20, message = "昵称长度需在 2-20 个字符之间")
    private String nickname;

    /** 密码(6-30 字符,服务端 BCrypt 加密后入库) */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 30, message = "密码长度需在 6-30 个字符之间")
    private String password;

    /** 邮箱(格式 + 长度校验,与 DB 列 VARCHAR(100) 对齐) */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100 个字符")
    private String email;

    /** 手机号(登录账号之一,11位数字) */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 邮箱验证码(注册前通过 /user/sendCode 获取) */
    @NotBlank(message = "验证码不能为空")
    private String code;
}

package com.worksshow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 忘记密码重置请求参数(通过邮箱验证码重置)
 *
 * @author WorksShow
 */
@Data
public class ResetPasswordRequest {

    /** 邮箱(重置目标账号) */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100 个字符")
    private String email;

    /** 邮箱验证码 */
    @NotBlank(message = "验证码不能为空")
    private String code;

    /** 新密码(6-30 字符,服务端 BCrypt 加密后入库) */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 30, message = "新密码长度需在 6-30 个字符之间")
    private String newPassword;
}

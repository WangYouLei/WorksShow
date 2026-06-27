package com.worksshow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求参数
 * <p>
 * 支持手机号或邮箱作为登录账号。
 *
 * @author WorksShow
 */
@Data
public class LoginRequest {

    /** 账号(手机号或邮箱) */
    @NotBlank(message = "账号不能为空")
    private String account;

    /** 密码(明文,服务端校验后不存储) */
    @NotBlank(message = "密码不能为空")
    private String password;
}

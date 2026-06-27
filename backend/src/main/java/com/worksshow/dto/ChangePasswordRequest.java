package com.worksshow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求参数
 *
 * @author WorksShow
 */
@Data
public class ChangePasswordRequest {

    /** 原密码(明文,服务端校验) */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /** 新密码(6-30 字符,服务端 BCrypt 加密后入库) */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 30, message = "新密码长度需在 6-30 个字符之间")
    private String newPassword;
}

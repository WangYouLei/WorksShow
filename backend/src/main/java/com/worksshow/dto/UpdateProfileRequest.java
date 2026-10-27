package com.worksshow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户资料请求参数
 * <p>
 * 仅允许修改展示信息(昵称/性别),邮箱与手机号作为登录账号不支持在此接口修改。
 *
 * @author WorksShow
 */
@Data
public class UpdateProfileRequest {

    /** 昵称(展示名,2-20 字符) */
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 20, message = "昵称长度需在 2-20 个字符之间")
    private String nickname;

    /** 性别:0-未知 1-男 2-女 */
    @Min(value = 0, message = "性别取值不合法")
    @Max(value = 2, message = "性别取值不合法")
    private Integer gender;
}

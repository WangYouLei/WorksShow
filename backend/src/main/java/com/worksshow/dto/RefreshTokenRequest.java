package com.worksshow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 token 请求参数
 * <p>
 * access token 过期后,前端使用 refresh token 调用 /user/refresh-token 获取新的 access token。
 *
 * @author WorksShow
 */
@Data
public class RefreshTokenRequest {

    /** refresh token(登录时颁发,7天有效,滚动刷新) */
    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}

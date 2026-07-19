package com.worksshow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应数据
 * <p>
 * 替代裸 Map,提供编译期类型安全与明确的接口契约。
 * <p>
 * access token 有效期 15 分钟,过期后使用 refresh token 刷新获取新 token。
 *
 * @author WorksShow
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    /** Access Token(JWT) */
    private String token;

    /** Refresh Token(用于 access token 过期后刷新) */
    private String refreshToken;

    /** 用户ID */
    private Long userId;

    /** 昵称(展示名) */
    private String nickname;
}

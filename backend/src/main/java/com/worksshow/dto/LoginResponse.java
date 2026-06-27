package com.worksshow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应数据
 * <p>
 * 替代裸 Map,提供编译期类型安全与明确的接口契约。
 *
 * @author WorksShow
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    /** JWT token */
    private String token;

    /** 用户ID */
    private Long userId;

    /** 昵称(展示名) */
    private String nickname;
}

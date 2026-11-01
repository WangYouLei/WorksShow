package com.worksshow.dto;

import lombok.Data;

/**
 * 退出登录请求参数
 * <p>
 * 携带当前设备的 refreshToken 时,登出仅删除该设备的 refresh token(其他设备登录态不受影响);
 * 不携带时仅将当前 access token 加入黑名单。
 *
 * @author WorksShow
 */
@Data
public class LogoutRequest {

    /** 当前设备的 refresh token(可选) */
    private String refreshToken;
}

package com.worksshow.security;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;

/**
 * 当前登录用户上下文
 * <p>
 * 重构为委托 Sa-Token 的 {@link StpUtil}:
 * <ul>
 *   <li>userId 来自 JWT subject(StpUtil#getLoginIdAsLong)</li>
 *   <li>nickname 来自 JWT 自定义 claim "nickname"(StpUtil#getExtra)</li>
 * </ul>
 * 业务层调用 {@link #getCurrentUserId()} / {@link #get()} 的接口签名保持不变,
 * 因此原 13 处业务调用无需改动。
 * <p>
 * 保留未登录返回 null 的原语义,供业务层做防御性判断;
 * 拦截器已保证受保护接口进入时一定已登录,因此 null 分支理论上不会走到。
 *
 * @author WorksShow
 */
public class UserContext {

    /** 获取当前登录用户(未登录时为 null) */
    public static LoginUser get() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Object nickname = StpUtil.getExtra("nickname");
            return new LoginUser(userId, nickname == null ? null : nickname.toString());
        } catch (NotLoginException e) {
            return null;
        }
    }

    /** 获取当前用户ID(未登录时为 null) */
    public static Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (NotLoginException e) {
            return null;
        }
    }

    /**
     * 登录用户信息(不可变记录)
     *
     * @param userId   用户ID
     * @param nickname 昵称
     */
    public record LoginUser(Long userId, String nickname) {
    }
}

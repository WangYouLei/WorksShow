package com.worksshow.security;

/**
 * 当前登录用户上下文
 * <p>
 * 基于 ThreadLocal 在一次请求内传递当前登录用户信息。
 * 由 JwtAuthenticationInterceptor 在请求前置入,
 * afterCompletion 中清除,避免线程复用导致的脏数据。
 *
 * @author WorksShow
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    /** 置入当前登录用户 */
    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    /** 获取当前登录用户(未登录时为 null) */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 获取当前用户ID */
    public static Long getCurrentUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.userId();
    }

    /** 清除当前线程的用户信息 */
    public static void clear() {
        HOLDER.remove();
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

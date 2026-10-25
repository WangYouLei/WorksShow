package com.worksshow.service;

/**
 * Token 黑名单服务接口
 * <p>
 * 用于在 token 泄漏或用户登出/改密时,主动失效 access token。
 * 黑名单存储在 Redis 中,过期时间与原 token 有效期一致。
 *
 * @author WorksShow
 */
public interface TokenBlacklistService {

    /**
     * 将 token 加入黑名单
     *
     * @param token     token 值
     * @param expireSec 过期时间(秒)
     */
    void blacklist(String token, long expireSec);

    /**
     * 检查 token 是否在黑名单中
     *
     * @param token token 值
     * @return true-在黑名单,false-不在
     */
    boolean isBlacklisted(String token);
}

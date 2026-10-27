package com.worksshow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.worksshow.entity.RefreshToken;

/**
 * Refresh Token 服务接口
 * <p>
 * 提供生成、验证、删除 refresh token 的能力。
 *
 * @author WorksShow
 */
public interface RefreshTokenService extends IService<RefreshToken> {

    /**
     * 生成并保存 refresh token
     *
     * @param userId 用户ID
     * @return refresh token 值
     */
    String generateToken(Long userId);

    /**
     * 验证 refresh token 是否有效
     *
     * @param refreshToken token 值
     * @return 用户ID(无效返回 null)
     */
    Long validateToken(String refreshToken);

    /**
     * 删除指定用户的所有 refresh token(改密/登出时调用)
     *
     * @param userId 用户ID
     */
    void deleteByUserId(Long userId);

    /**
     * 原子删除指定值的 refresh token(逻辑删除)
     * <p>
     * 用于刷新时的轮换:单条 UPDATE 天然原子,删除失败即说明该 token
     * 已被并发请求使用(重放),调用方应拒绝刷新。
     *
     * @param refreshToken token 值
     * @return true=删除成功;false=token 不存在或已被使用
     */
    boolean deleteByToken(String refreshToken);
}

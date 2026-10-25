package com.worksshow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.worksshow.entity.RefreshToken;
import com.worksshow.mapper.RefreshTokenMapper;
import com.worksshow.service.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Refresh Token 服务实现
 * <p>
 * Refresh token 有效期 7 天,存储在数据库中。
 *
 * @author WorksShow
 */
@Slf4j
@Service
public class RefreshTokenServiceImpl extends ServiceImpl<RefreshTokenMapper, RefreshToken> implements RefreshTokenService {

    /** Refresh token 有效期: 7 天 */
    private static final int REFRESH_TOKEN_EXPIRE_DAYS = 7;

    @Override
    public String generateToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken(token);
        refreshToken.setUserId(userId);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRE_DAYS));

        save(refreshToken);
        log.info("生成 refresh token: userId={}", userId);
        return token;
    }

    @Override
    public Long validateToken(String refreshToken) {
        RefreshToken token = getOne(new LambdaQueryWrapper<RefreshToken>()
                .eq(RefreshToken::getRefreshToken, refreshToken)
                .last("LIMIT 1"));

        if (token == null) {
            log.warn("refresh token 不存在: {}", refreshToken);
            return null;
        }

        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("refresh token 已过期: userId={}", token.getUserId());
            removeById(token.getId());
            return null;
        }

        return token.getUserId();
    }

    @Override
    public void deleteByUserId(Long userId) {
        remove(new LambdaQueryWrapper<RefreshToken>().eq(RefreshToken::getUserId, userId));
        log.info("删除用户所有 refresh token: userId={}", userId);
    }
}

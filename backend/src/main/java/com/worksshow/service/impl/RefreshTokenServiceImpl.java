package com.worksshow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.worksshow.entity.RefreshToken;
import com.worksshow.mapper.RefreshTokenMapper;
import com.worksshow.service.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Refresh Token 服务实现
 * <p>
 * Refresh token 有效期 7 天,存储在数据库中。
 * 库中仅存 token 的 SHA-256 哈希(64位hex),不落明文:
 * 数据库或备份泄漏时无法反查 token 冒充用户。
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
        refreshToken.setRefreshToken(sha256Hex(token));
        refreshToken.setUserId(userId);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRE_DAYS));

        save(refreshToken);
        log.info("生成 refresh token: userId={}", userId);
        return token;
    }

    @Override
    public Long validateToken(String refreshToken) {
        RefreshToken token = getOne(new LambdaQueryWrapper<RefreshToken>()
                .eq(RefreshToken::getRefreshToken, sha256Hex(refreshToken))
                .last("LIMIT 1"));

        if (token == null) {
            log.warn("refresh token 不存在(哈希未命中)");
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

    @Override
    public boolean deleteByToken(String refreshToken) {
        // @TableLogic 下 remove 生成单条 UPDATE ... SET deleted=1 WHERE refresh_token=? AND deleted=0,
        // 原子生效:并发重放同一 token 时仅一个请求能删除成功(影响行数>0)
        boolean removed = remove(new LambdaQueryWrapper<RefreshToken>()
                .eq(RefreshToken::getRefreshToken, sha256Hex(refreshToken)));
        if (!removed) {
            log.warn("按值删除 refresh token 失败(不存在或已被使用),疑似重放");
        }
        return removed;
    }

    /** 计算 token 的 SHA-256 十六进制(小写,64 字符,与表列 VARCHAR(64) 精确匹配) */
    private static String sha256Hex(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 是 JDK 标准算法,理论上不会缺失
            throw new IllegalStateException("SHA-256 算法不可用", e);
        }
    }
}

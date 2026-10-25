package com.worksshow.service.impl;

import com.worksshow.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Token 黑名单服务实现
 * <p>
 * 使用 Redis 存储黑名单:
 * - 键: "token:blacklist:{token}"
 * - 值: userId
 * - 过期时间: 与原 token 有效期一致
 *
 * @author WorksShow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    /** 黑名单 key 前缀 */
    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";

    @Override
    public void blacklist(String token, long expireSec) {
        String key = BLACKLIST_KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, "1", expireSec);
        log.info("token 加入黑名单: {}", token);
    }

    @Override
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_KEY_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}

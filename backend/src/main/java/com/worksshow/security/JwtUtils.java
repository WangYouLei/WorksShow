package com.worksshow.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

/**
 * JWT 工具类
 * <p>
 * 负责 token 的生成、解析与校验。
 * 采用 HS256 算法签名,密钥从 application.yml 读取(Base64 编码)。
 * token 中携带 userId(subject) 与 nickname(自定义 claim)。
 *
 * @author WorksShow
 */
@Slf4j
@Component
public class JwtUtils {

    /** 签名密钥(Base64),来自配置文件 */
    @Value("${jwt.secret}")
    private String secret;

    /** token 有效期(毫秒),来自配置文件 */
    @Value("${jwt.expiration}")
    private long expiration;

    /** 解码后的签名 Key 对象 */
    private Key key;

    /**
     * 初始化:将 Base64 密钥解码为 HMAC Key
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT 密钥初始化完成,token 有效期: {} ms", expiration);
    }

    /**
     * 生成 token
     *
     * @param userId   用户ID(作为 subject)
     * @param nickname 昵称(自定义 claim)
     * @return JWT 字符串
     */
    public String generateToken(Long userId, String nickname) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("nickname", nickname)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 token,返回 Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 校验 token 是否合法且未过期
     */
    public boolean validateToken(String token) {
        return parseAndValidate(token) != null;
    }

    /**
     * 解析并校验 token,返回 Claims;失败返回 null。
     * 供拦截器复用,避免同一 token 多次 HMAC 验签。
     */
    public Claims parseAndValidate(String token) {
        try {
            return parseToken(token);
        } catch (Exception e) {
            log.warn("JWT 校验失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 token 中获取用户ID
     */
    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    /**
     * 从 token 中获取昵称
     */
    public String getNickname(String token) {
        return parseToken(token).get("nickname", String.class);
    }
}

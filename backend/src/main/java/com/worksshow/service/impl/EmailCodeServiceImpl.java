package com.worksshow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.worksshow.entity.User;
import com.worksshow.exception.BusinessException;
import com.worksshow.mapper.UserMapper;
import com.worksshow.service.EmailCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 邮箱验证码服务实现
 * <p>
 * 使用 Redis Hash 存储验证码,5分钟自动过期(Redis TTL),60秒发送频率限制。
 * 验证码校验:原子递增尝试次数(HINCRBY)+ 原子删除(DEL)保证一次性使用。
 * 基于 Redis 实现,支持多实例部署(与 token 黑名单共用同一 Redis)。
 *
 * @author WorksShow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCodeServiceImpl implements EmailCodeService {

    private final JavaMailSender mailSender;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    /** 发件人邮箱(从配置文件 spring.mail.username 读取) */
    @Value("${spring.mail.username}")
    private String fromEmail;

    /** Redis key 前缀 */
    private static final String KEY_PREFIX = "email:code:";
    /** Hash 字段:验证码 */
    private static final String FIELD_CODE = "code";
    /** Hash 字段:发送时间戳(毫秒,用于频率限制) */
    private static final String FIELD_SEND_TIME = "sendTime";
    /** Hash 字段:已尝试次数 */
    private static final String FIELD_ATTEMPTS = "attempts";

    /** 验证码有效期: 5分钟(毫秒),作为 Redis TTL */
    private static final long CODE_EXPIRE_MS = 5 * 60 * 1000;

    /** 验证码长度 */
    private static final int CODE_LENGTH = 6;

    /** 发送频率限制: 60秒内不可重复发送(毫秒) */
    private static final long SEND_INTERVAL_MS = 60 * 1000;

    /** 验证码最大尝试次数:超过后作废,需重新获取 */
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    /** 密码学安全随机数生成器(验证码生成) */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String key(String email) {
        return KEY_PREFIX + email;
    }

    @Override
    public void sendCode(String email) {
        // 注册场景:已注册邮箱静默跳过(防滥用,避免账号枚举)
        Long registeredCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email));
        if (registeredCount != null && registeredCount > 0) {
            log.warn("邮箱已注册,跳过发送注册验证码: email={}", email);
            return;
        }
        generateAndSendCode(email);
    }

    @Override
    public void sendCodeForReset(String email) {
        // 重置密码场景:校验邮箱已注册(未注册静默跳过,防滥用 + 避免账号枚举)
        Long registeredCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email));
        if (registeredCount == null || registeredCount == 0) {
            log.warn("邮箱未注册,跳过发送重置验证码: email={}", email);
            return;
        }
        generateAndSendCode(email);
    }

    /**
     * 生成并发送验证码(频率限制 + 生成 + 存储 + 发送)
     * 注册与重置密码共用此逻辑。
     */
    private void generateAndSendCode(String email) {
        String redisKey = key(email);

        // 频率限制: 60秒内不可重复发送
        Object sendTimeObj = redisTemplate.opsForHash().get(redisKey, FIELD_SEND_TIME);
        if (sendTimeObj != null) {
            long sendTime = Long.parseLong(sendTimeObj.toString());
            long elapsed = System.currentTimeMillis() - sendTime;
            if (elapsed < SEND_INTERVAL_MS) {
                long remaining = (SEND_INTERVAL_MS - elapsed) / 1000;
                log.warn("验证码发送过于频繁: email={}, 剩余{}秒", email, remaining);
                throw new BusinessException(429, "验证码已发送,请" + remaining + "秒后重试");
            }
        }

        // 生成6位数字验证码(使用 SecureRandom 防止预测)
        String code = generateCode();

        // 存储验证码(Redis Hash,5分钟 TTL 自动过期,无需定时清理任务)
        long now = System.currentTimeMillis();
        Map<String, String> fields = new HashMap<>();
        fields.put(FIELD_CODE, code);
        fields.put(FIELD_SEND_TIME, String.valueOf(now));
        fields.put(FIELD_ATTEMPTS, "0");
        redisTemplate.opsForHash().putAll(redisKey, fields);
        redisTemplate.expire(redisKey, Duration.ofMillis(CODE_EXPIRE_MS));

        // 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【WorksShow】邮箱验证码");
            message.setText("您的验证码是:" + code + "\n\n"
                    + "验证码5分钟内有效,请勿泄露给他人。\n\n"
                    + "如非本人操作,请忽略此邮件。");
            mailSender.send(message);
            log.info("验证码发送成功: email={}", email);
        } catch (Exception e) {
            // 发送失败,移除已存储的验证码,避免占用频率限制
            redisTemplate.delete(redisKey);
            log.error("验证码发送失败: email={}", email, e);
            throw new BusinessException(500, "验证码发送失败,请检查邮箱配置或稍后重试");
        }
    }

    @Override
    public void verifyCode(String email, String code) {
        String redisKey = key(email);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);

        // 未发送验证码 / 已过期(Redis TTL 到期自动删除,entries 为空即视为过期)
        if (entries.isEmpty()) {
            log.warn("验证码校验失败,未发送或已过期: email={}", email);
            throw new BusinessException(400, "请先获取验证码");
        }

        String storedCode = entries.get(FIELD_CODE).toString();

        // 验证码不匹配:原子递增尝试次数(HINCRBY),超过阈值则作废验证码
        if (!storedCode.equals(code)) {
            Long attempts = redisTemplate.opsForHash().increment(redisKey, FIELD_ATTEMPTS, 1);
            if (attempts != null && attempts >= MAX_VERIFY_ATTEMPTS) {
                redisTemplate.delete(redisKey);
                log.warn("验证码校验失败,尝试次数超限,验证码已作废: email={}, attempts={}", email, attempts);
                throw new BusinessException(400, "验证码错误次数过多,请重新获取");
            }
            log.warn("验证码校验失败,验证码错误: email={}, attempts={}", email, attempts);
            throw new BusinessException(400, "验证码错误");
        }

        // 验证成功,原子性删除验证码(一次性使用)
        Boolean removed = redisTemplate.delete(redisKey);
        if (!Boolean.TRUE.equals(removed)) {
            log.warn("验证码校验异常: 并发重复验证,email={}", email);
            throw new BusinessException(400, "验证码已使用");
        }
        log.info("验证码校验成功: email={}", email);
    }

    /**
     * 生成6位随机数字验证码(使用 SecureRandom)
     */
    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}

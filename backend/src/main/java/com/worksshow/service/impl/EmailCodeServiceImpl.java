package com.worksshow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.worksshow.entity.User;
import com.worksshow.exception.BusinessException;
import com.worksshow.mapper.UserMapper;
import com.worksshow.service.EmailCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 邮箱验证码服务实现
 * <p>
 * 使用内存 ConcurrentHashMap 存储验证码,5分钟过期,60秒发送频率限制。
 * 定时任务每5分钟清理过期条目,防止内存泄漏。
 * 生产环境建议替换为 Redis 实现,支持分布式与自动过期。
 *
 * @author WorksShow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCodeServiceImpl implements EmailCodeService {

    private final JavaMailSender mailSender;
    private final UserMapper userMapper;

    /** 发件人邮箱(从配置文件 spring.mail.username 读取) */
    @Value("${spring.mail.username}")
    private String fromEmail;

    /** 验证码有效期: 5分钟(毫秒) */
    private static final long CODE_EXPIRE_MS = 5 * 60 * 1000;

    /** 验证码长度 */
    private static final int CODE_LENGTH = 6;

    /** 发送频率限制: 60秒内不可重复发送(毫秒) */
    private static final long SEND_INTERVAL_MS = 60 * 1000;

    /** 密码学安全随机数生成器(验证码生成) */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /** 验证码存储: key=email, value=CodeEntry */
    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    /**
     * 验证码条目(内部记录类)
     *
     * @param code       验证码
     * @param expireTime 过期时间戳(毫秒)
     * @param sendTime   发送时间戳(毫秒)
     */
    private record CodeEntry(String code, long expireTime, long sendTime) {}

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
        // 频率限制: 60秒内不可重复发送
        CodeEntry existing = codeStore.get(email);
        if (existing != null && System.currentTimeMillis() - existing.sendTime() < SEND_INTERVAL_MS) {
            long remaining = (SEND_INTERVAL_MS - (System.currentTimeMillis() - existing.sendTime())) / 1000;
            log.warn("验证码发送过于频繁: email={}, 剩余{}秒", email, remaining);
            throw new BusinessException(429, "验证码已发送,请" + remaining + "秒后重试");
        }

        // 生成6位数字验证码(使用 SecureRandom 防止预测)
        String code = generateCode();

        // 存储验证码(5分钟有效)
        long now = System.currentTimeMillis();
        codeStore.put(email, new CodeEntry(code, now + CODE_EXPIRE_MS, now));

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
            codeStore.remove(email);
            log.error("验证码发送失败: email={}", email, e);
            throw new BusinessException(500, "验证码发送失败,请检查邮箱配置或稍后重试");
        }
    }

    @Override
    public void verifyCode(String email, String code) {
        CodeEntry entry = codeStore.get(email);

        // 未发送验证码
        if (entry == null) {
            log.warn("验证码校验失败,未发送验证码: email={}", email);
            throw new BusinessException(400, "请先获取验证码");
        }

        // 已过期
        if (System.currentTimeMillis() > entry.expireTime()) {
            codeStore.remove(email);
            log.warn("验证码校验失败,已过期: email={}", email);
            throw new BusinessException(400, "验证码已过期,请重新获取");
        }

        // 验证码不匹配
        if (!entry.code().equals(code)) {
            log.warn("验证码校验失败,验证码错误: email={}", email);
            throw new BusinessException(400, "验证码错误");
        }

        // 验证成功,移除验证码(一次性使用)
        codeStore.remove(email);
        log.info("验证码校验成功: email={}", email);
    }

    /**
     * 定时清理过期验证码,每5分钟执行一次,防止内存泄漏。
     * 处理"请求了验证码但从未验证"的条目。
     */
    @Scheduled(fixedRate = CODE_EXPIRE_MS)
    public void cleanupExpiredCodes() {
        long now = System.currentTimeMillis();
        int removed = 0;
        Iterator<Map.Entry<String, CodeEntry>> it = codeStore.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, CodeEntry> entry = it.next();
            if (now > entry.getValue().expireTime()) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) {
            log.info("定时清理过期验证码: 移除{}条,剩余{}条", removed, codeStore.size());
        }
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

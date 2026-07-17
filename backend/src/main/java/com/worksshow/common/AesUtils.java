package com.worksshow.common;

import com.worksshow.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES 对称加解密工具
 * <p>
 * 用于加解密用户存储的 EdgeOne API Token(单个 Token,非 SecretId/SecretKey 对)。
 * 采用 AES/CBC/PKCS5Padding,每次加密生成随机 IV 并拼接在密文前,
 * 最终 Base64 编码输出。密钥由配置项 edgeone.encrypt-key 提供
 * (生产环境从环境变量 EDGEONE_ENCRYPT_KEY 注入),经 SHA-256 派生为
 * 256 位 AES 密钥。
 * <p>
 * 注意: 与用户密码的 BCrypt 单向哈希不同,此处需可逆解密,
 * 故使用 AES 对称加密。
 *
 * @author WorksShow
 */
@Slf4j
@Component
public class AesUtils {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    /** IV 长度(AES block size,固定 16 字节) */
    private static final int IV_LENGTH = 16;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesUtils(@Value("${edgeone.encrypt-key}") String keyStr) {
        this.secretKey = deriveKey(keyStr);
        log.info("AesUtils 初始化完成");
    }

    /**
     * 由配置密钥字符串派生 256 位 AES 密钥
     */
    private SecretKeySpec deriveKey(String keyStr) {
        try {
            byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                    .digest(keyStr.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 是 JDK 标准算法,理论上不会缺失
            throw new IllegalStateException("SHA-256 算法不可用", e);
        }
    }

    /**
     * 加密明文
     *
     * @param plaintext 明文
     * @return Base64(IV + 密文),null 入参返回 null
     */
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] cipherBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            // 拼接 IV + 密文后 Base64 编码
            byte[] combined = new byte[IV_LENGTH + cipherBytes.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(cipherBytes, 0, combined, IV_LENGTH, cipherBytes.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("AES 加密失败", e);
            throw new BusinessException(500, "数据加密失败");
        }
    }

    /**
     * 解密密文
     *
     * @param ciphertext Base64(IV + 密文)
     * @return 明文,null 入参返回 null
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(ciphertext);
            byte[] iv = new byte[IV_LENGTH];
            byte[] cipherBytes = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, cipherBytes, 0, cipherBytes.length);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES 解密失败", e);
            throw new BusinessException(500, "数据解密失败");
        }
    }
}

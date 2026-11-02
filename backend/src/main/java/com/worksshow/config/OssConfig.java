package com.worksshow.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 阿里云 OSS 客户端配置
 * <p>
 * OSSClient 线程安全,全局单例复用;应用关闭时自动 shutdown 释放连接。
 * AK/SK 为空时启动失败并给出明确提示(fail-fast,与 JWT_SECRET 同策略;
 * SDK 对空凭据本身也会在建客户端时抛 InvalidCredentialsException)。
 * 预签名 URL 生成仅为本地 HMAC 计算,不发起网络请求,
 * 凭据错误只会在实际调用 OSS 接口时暴露。
 *
 * @author WorksShow
 */
@Configuration
@RequiredArgsConstructor
public class OssConfig {

    private final OssProperties ossProperties;

    @Bean(destroyMethod = "shutdown")
    public OSS ossClient() {
        if (!StringUtils.hasText(ossProperties.getAccessKeyId())
                || !StringUtils.hasText(ossProperties.getAccessKeySecret())) {
            throw new IllegalStateException(
                    "OSS 凭据未配置:请通过环境变量 OSS_ACCESS_KEY_ID / OSS_ACCESS_KEY_SECRET 注入"
                            + "(RAM 子账号,仅授予 Bucket 的 PutObject 权限)后重启应用");
        }
        return new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret());
    }
}

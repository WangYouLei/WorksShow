package com.worksshow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置
 * <p>
 * 用于用户图片/视频对象存储。采用"后端签名 + 前端直传"模式:
 * AK/SK 仅在后端用于生成预签名 URL,永不下发给前端。
 * 凭据为空时启动失败(fail-fast,与 JWT_SECRET 同策略,见 {@link OssConfig})。
 *
 * @author WorksShow
 */
@Data
@Component
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    /** OSS Endpoint(如 https://oss-cn-hangzhou.aliyuncs.com) */
    private String endpoint;

    /** Bucket 名称 */
    private String bucket;

    /** AccessKey ID(RAM 子账号,仅授予该 Bucket 的 PutObject) */
    private String accessKeyId;

    /** AccessKey Secret(仅从环境变量注入) */
    private String accessKeySecret;

    /** 对象根目录(objectKey 前缀,最终形如 uploads/{userId}/images/{uuid}.jpg) */
    private String dir = "uploads";

    /** 预签名上传 URL 有效期(秒) */
    private Integer uploadUrlExpireSeconds = 600;

    /** 每用户每日上传签名次数上限(防刷签名写满 Bucket) */
    private Integer dailyLimit = 200;
}

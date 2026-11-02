package com.worksshow.service.impl;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.worksshow.config.OssProperties;
import com.worksshow.dto.OssUploadTokenVO;
import com.worksshow.exception.BusinessException;
import com.worksshow.security.UserContext;
import com.worksshow.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * OSS 对象存储服务实现
 * <p>
 * 安全设计:
 * <ul>
 *   <li>objectKey 由后端基于 JWT 的 userId 生成(uploads/{userId}/{类型}/{uuid}.{ext}),
 *       用户之间目录隔离,前端无法指定任意路径</li>
 *   <li>UUID 文件名使公开 URL 不可枚举(横向越权防护),Bucket 公共读也不允许匿名列举</li>
 *   <li>预签名 URL 限 PUT、限单个 key、按时效过期,AK/SK 永不出后端</li>
 * </ul>
 * 文件大小限制由前端校验(预签名 PUT 无法在 OSS 侧限制大小)。
 *
 * @author WorksShow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final OssProperties ossProperties;
    private final OSS ossClient;
    private final StringRedisTemplate stringRedisTemplate;

    /** 图片扩展名白名单 */
    private static final Set<String> IMAGE_EXTS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    /** 视频扩展名白名单 */
    private static final Set<String> VIDEO_EXTS = Set.of(".mp4", ".webm");

    /** 扩展名 → Content-Type(签名与前端 PUT 请求头必须完全一致,否则 OSS 返回 403) */
    private static final Map<String, String> EXT_CONTENT_TYPES = Map.of(
            ".jpg", "image/jpeg", ".jpeg", "image/jpeg", ".png", "image/png",
            ".webp", "image/webp", ".gif", "image/gif",
            ".mp4", "video/mp4", ".webm", "video/webm");

    /** 每日上传签名配额 Redis key 前缀 */
    private static final String UPLOAD_LIMIT_PREFIX = "oss:upload-limit:";

    @Override
    public OssUploadTokenVO generateUploadToken(String type, String fileName) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已失效");
        }
        // 0. 每日签名配额校验(预签名 PUT 无服务端大小限制,防刷签名写满 Bucket)
        checkDailyQuota(userId);

        // 1. 扩展名白名单校验(图片/视频各自独立)
        String ext = extractExtension(fileName);
        String dir = resolveObjectDir(type, ext);

        // 2. 生成随机 objectKey:UUID 保证不可枚举,userId 前缀隔离用户
        String objectKey = ossProperties.getDir() + "/" + userId + "/" + dir + "/"
                + UUID.randomUUID().toString().replace("-", "") + ext;

        // 3. 签发限时限 key 的 PUT 预签名 URL(本地 HMAC 计算,无网络请求)。
        //    Content-Type 必须纳入签名:浏览器对 File body 必然携带该头,
        //    签名缺省按空串计算,两端不一致会导致 SignatureDoesNotMatch
        String contentType = EXT_CONTENT_TYPES.getOrDefault(ext, "application/octet-stream");
        int expireSeconds = ossProperties.getUploadUrlExpireSeconds();
        Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000L);
        GeneratePresignedUrlRequest request =
                new GeneratePresignedUrlRequest(ossProperties.getBucket(), objectKey, HttpMethod.PUT);
        request.setContentType(contentType);
        request.setExpiration(expiration);
        URL signedUrl = ossClient.generatePresignedUrl(request);

        OssUploadTokenVO vo = new OssUploadTokenVO();
        vo.setUploadUrl(signedUrl.toString());
        vo.setObjectKey(objectKey);
        vo.setPublicUrl(buildPublicUrl(objectKey));
        vo.setContentType(contentType);
        vo.setExpireSeconds(expireSeconds);

        log.info("签发上传凭证: userId={}, type={}, objectKey={}", userId, type, objectKey);
        return vo;
    }

    /**
     * 每日上传签名配额校验(Redis 计数,按自然日)。
     * 首次计数时设置过期,跨日自动清零。
     */
    private void checkDailyQuota(Long userId) {
        String key = UPLOAD_LIMIT_PREFIX + userId + ":"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, Duration.ofDays(2));
        }
        if (count != null && count > ossProperties.getDailyLimit()) {
            throw new BusinessException(429, "今日上传次数已用完,明天再来试试");
        }
    }

    /**
     * 提取小写扩展名(含点号),无扩展名或为空时抛业务异常
     */
    private String extractExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() - 1) {
            throw new BusinessException(400, "文件名缺少扩展名");
        }
        return fileName.substring(idx).toLowerCase();
    }

    /**
     * 按媒体类型校验扩展名白名单,返回 objectKey 中的子目录名
     */
    private String resolveObjectDir(String type, String ext) {
        if ("image".equals(type)) {
            if (!IMAGE_EXTS.contains(ext)) {
                throw new BusinessException(400, "不支持的图片格式,仅支持 jpg/jpeg/png/webp/gif");
            }
            return "images";
        }
        if (!VIDEO_EXTS.contains(ext)) {
            throw new BusinessException(400, "不支持的视频格式,仅支持 mp4/webm");
        }
        return "videos";
    }

    /**
     * 拼接公开访问 URL:https://{bucket}.{endpoint域名}/{objectKey}
     * (Bucket 为公共读,URL 确定可渲染,无需回查 OSS)
     */
    private String buildPublicUrl(String objectKey) {
        if (!StringUtils.hasText(ossProperties.getBucket()) || !StringUtils.hasText(ossProperties.getEndpoint())) {
            throw new BusinessException(500, "对象存储未配置,请联系管理员");
        }
        String host = ossProperties.getEndpoint().replaceFirst("^https?://", "");
        return "https://" + ossProperties.getBucket() + "." + host + "/" + objectKey;
    }
}

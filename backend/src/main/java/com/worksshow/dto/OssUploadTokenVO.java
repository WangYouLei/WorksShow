package com.worksshow.dto;

import lombok.Data;

/**
 * OSS 上传凭证 VO
 * <p>
 * 前端流程:先 PUT 文件二进制到 uploadUrl(携带 Content-Type),
 * 成功后将 publicUrl 作为普通字段(cover/videoUrl/avatar)走业务接口保存。
 *
 * @author WorksShow
 */
@Data
public class OssUploadTokenVO {

    /** 预签名上传 URL(仅对该 objectKey 有效,限 PUT,按时效过期) */
    private String uploadUrl;

    /** 对象存储 key(uploads/{userId}/{images|videos}/{uuid}.{ext}) */
    private String objectKey;

    /** 上传成功后的公开访问 URL(bucket 公共读) */
    private String publicUrl;

    /** 签名时纳入的 Content-Type(前端 PUT 必须携带完全一致的值,否则 403) */
    private String contentType;

    /** 凭证有效期(秒) */
    private Integer expireSeconds;
}

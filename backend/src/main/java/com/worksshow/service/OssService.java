package com.worksshow.service;

import com.worksshow.dto.OssUploadTokenVO;

/**
 * OSS 对象存储服务
 * <p>
 * 负责"后端签名 + 前端直传"模式的凭证签发:
 * 校验扩展名白名单后,为当前登录用户生成随机 objectKey
 * 与限时限 key 的预签名 PUT URL。
 *
 * @author WorksShow
 */
public interface OssService {

    /**
     * 生成上传凭证
     *
     * @param type     媒体类型:image-图片 video-视频
     * @param fileName 原始文件名(用于提取并校验扩展名)
     * @return 预签名 URL + objectKey + 公开访问 URL
     */
    OssUploadTokenVO generateUploadToken(String type, String fileName);
}

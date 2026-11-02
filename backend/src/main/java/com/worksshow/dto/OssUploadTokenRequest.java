package com.worksshow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 申请 OSS 上传凭证请求参数
 * <p>
 * type 与 fileName 中的扩展名联合校验:图片(jpg/jpeg/png/webp/gif)
 * 或视频(mp4/webm)。objectKey 由后端生成,前端无法指定上传路径。
 *
 * @author WorksShow
 */
@Data
public class OssUploadTokenRequest {

    /** 媒体类型:image-图片 video-视频 */
    @NotBlank(message = "类型不能为空")
    @Pattern(regexp = "^(image|video)$", message = "类型仅支持 image 或 video")
    private String type;

    /** 原始文件名(仅用于提取并校验扩展名,不参与 objectKey 生成) */
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过 255 个字符")
    private String fileName;
}

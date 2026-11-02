package com.worksshow.controller;

import com.worksshow.common.Result;
import com.worksshow.dto.OssUploadTokenRequest;
import com.worksshow.dto.OssUploadTokenVO;
import com.worksshow.service.OssService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OSS 对象存储接口
 * <p>
 * 用户图片/视频上传采用"后端签名 + 前端直传":
 * 前端先申请预签名 URL,再将文件 PUT 直传 OSS,不经后端服务器。
 * 实际访问路径前缀为 /api(由 server.servlet.context-path 配置),
 * 接口默认受 Sa-Token 拦截保护(需登录)。
 * <ul>
 *   <li>POST /api/oss/upload-token  申请上传凭证(需登录)</li>
 * </ul>
 *
 * @author WorksShow
 */
@Slf4j
@RestController
@RequestMapping("/oss")
@RequiredArgsConstructor
public class OssController {

    private final OssService ossService;

    /**
     * 申请上传凭证
     * <p>
     * 返回限时限 key 的预签名 PUT URL;前端直传成功后,
     * 将 publicUrl 作为普通字段(封面/视频/头像)走各业务保存接口。
     */
    @PostMapping("/upload-token")
    public Result<OssUploadTokenVO> uploadToken(@Valid @RequestBody OssUploadTokenRequest request) {
        log.info("申请上传凭证请求: type={}", request.getType());
        return Result.ok(ossService.generateUploadToken(request.getType(), request.getFileName()));
    }
}

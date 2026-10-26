package com.worksshow.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * 本地部署站点公开访问接口
 * <p>
 * 将用户通过「本服务器部署」方式落盘的简历 HTML 公开提供给外网访问:
 * GET /api/sites/{key} → 返回 {local-deploy.dir}/{key}.html
 * <p>
 * 无需登录(简历分享链接必须匿名可访问),已在 SaTokenConfig 放行。
 * key 仅允许字母/数字/连字符,拼接文件名前严格校验,防止路径穿越。
 *
 * @author WorksShow
 */
@Slf4j
@RestController
@RequestMapping("/sites")
public class LocalSiteController {

    /** 站点 key 合法格式(与部署时生成的 32 位 hex 兼容) */
    private static final Pattern KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9-]{8,64}$");

    /** 响应类型:HTML(UTF-8) */
    private static final MediaType HTML_UTF8 = MediaType.parseMediaType("text/html;charset=UTF-8");

    /** 本地部署站点文件存储目录(与 DeploymentServiceImpl 保持一致) */
    @Value("${local-deploy.dir:./local-sites}")
    private String localDeployDir;

    /**
     * 访问本地部署的简历页面
     *
     * @param key 站点key(部署时生成)
     * @return 页面 HTML(UTF-8)
     */
    @GetMapping("/{key}")
    public ResponseEntity<String> site(@PathVariable String key) {
        // key 格式校验,防止 "../" 等路径穿越
        if (!KEY_PATTERN.matcher(key).matches()) {
            return notFound();
        }
        try {
            Path dir = Paths.get(localDeployDir).toAbsolutePath().normalize();
            Path file = dir.resolve(key + ".html").normalize();
            // 二次防御:解析后的路径必须仍在站点目录内
            if (!file.startsWith(dir) || !Files.isRegularFile(file)) {
                return notFound();
            }
            String html = Files.readString(file, StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(HTML_UTF8)
                    .body(html);
        } catch (IOException e) {
            log.error("读取本地部署页面失败: key={}", key, e);
            return ResponseEntity.status(500)
                    .contentType(HTML_UTF8)
                    .body("<!DOCTYPE html><html><body style=\"font-family:sans-serif;text-align:center;padding-top:80px\">" +
                            "<h1>500</h1><p>页面读取失败,请稍后重试</p></body></html>");
        }
    }

    /** 404 提示页 */
    private ResponseEntity<String> notFound() {
        return ResponseEntity.status(404)
                .contentType(HTML_UTF8)
                .body("<!DOCTYPE html><html><body style=\"font-family:sans-serif;text-align:center;padding-top:80px\">" +
                        "<h1>404</h1><p>页面不存在或已被移除</p></body></html>");
    }
}

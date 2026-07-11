package com.worksshow.controller;

import com.worksshow.common.Result;
import com.worksshow.dto.EdgeOneConfigRequestDTO;
import com.worksshow.dto.EdgeOneConfigVO;
import com.worksshow.service.UserEdgeoneConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * EdgeOne 配置接口
 * <p>
 * 管理用户部署到 EdgeOne Pages 所需的 API 凭证。
 * 一次配置后多次部署复用。secret_key 永不返回前端。
 * 实际访问路径前缀为 /api(由 server.servlet.context-path 配置)。
 * <ul>
 *   <li>GET    /api/edgeone-config    获取当前用户配置(脱敏)</li>
 *   <li>POST   /api/edgeone-config    保存/更新配置(明文入库前加密)</li>
 *   <li>DELETE /api/edgeone-config    删除配置(逻辑删除)</li>
 * </ul>
 *
 * @author WorksShow
 */
@Slf4j
@RestController
@RequestMapping("/edgeone-config")
@RequiredArgsConstructor
public class UserEdgeoneConfigController {

    private final UserEdgeoneConfigService userEdgeoneConfigService;

    /**
     * 获取当前用户的 EdgeOne 配置(脱敏)
     */
    @GetMapping
    public Result<EdgeOneConfigVO> get() {
        log.info("获取EdgeOne配置请求");
        return Result.ok(userEdgeoneConfigService.getMyConfig());
    }

    /**
     * 保存或更新 EdgeOne 配置
     */
    @PostMapping
    public Result<Void> save(@Valid @RequestBody EdgeOneConfigRequestDTO dto) {
        log.info("保存EdgeOne配置请求");
        userEdgeoneConfigService.saveOrUpdate(dto);
        return Result.ok("保存成功", null);
    }

    /**
     * 删除 EdgeOne 配置
     */
    @DeleteMapping
    public Result<Void> delete() {
        log.info("删除EdgeOne配置请求");
        userEdgeoneConfigService.deleteMyConfig();
        return Result.ok("删除成功", null);
    }
}

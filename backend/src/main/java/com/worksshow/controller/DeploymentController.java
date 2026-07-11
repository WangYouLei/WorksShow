package com.worksshow.controller;

import com.worksshow.common.Result;
import com.worksshow.dto.DeploymentRequestDTO;
import com.worksshow.dto.DeploymentVO;
import com.worksshow.service.DeploymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 模板部署接口
 * <p>
 * 将简历模板部署到 EdgeOne Pages。
 * 实际访问路径前缀为 /api(由 server.servlet.context-path 配置)。
 * <ul>
 *   <li>POST /api/deployment          部署简历(传入渲染后的HTML)</li>
 *   <li>GET  /api/deployment/list     获取当前用户部署记录列表</li>
 *   <li>GET  /api/deployment/{id}     获取部署详情</li>
 * </ul>
 *
 * @author WorksShow
 */
@Slf4j
@RestController
@RequestMapping("/deployment")
@RequiredArgsConstructor
public class DeploymentController {

    private final DeploymentService deploymentService;

    /**
     * 部署简历模板到 EdgeOne Pages
     * <p>
     * 同步部署:请求提交后等待 CLI 执行完成(通常数秒至数十秒)。
     */
    @PostMapping
    public Result<DeploymentVO> deploy(@Valid @RequestBody DeploymentRequestDTO dto) {
        log.info("部署请求: portfolioId={}, projectName={}", dto.getPortfolioId(), dto.getProjectName());
        return Result.ok("部署完成", deploymentService.deploy(dto));
    }

    /**
     * 获取当前用户部署记录列表
     */
    @GetMapping("/list")
    public Result<List<DeploymentVO>> list() {
        log.info("获取部署记录列表请求");
        return Result.ok(deploymentService.listMine());
    }

    /**
     * 获取部署详情
     */
    @GetMapping("/{id}")
    public Result<DeploymentVO> get(@PathVariable Long id) {
        log.info("获取部署详情请求: id={}", id);
        return Result.ok(deploymentService.getDetail(id));
    }
}

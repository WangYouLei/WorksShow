package com.worksshow.controller;

import com.worksshow.common.Result;
import com.worksshow.dto.DeploymentRequestDTO;
import com.worksshow.dto.DeploymentVO;
import com.worksshow.service.DeploymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 *   <li>POST /api/deployment/{id}/offline  下线 EdgeOne 站点(提示页覆盖原内容)</li>
 *   <li>DELETE /api/deployment/{id}   删除部署记录(仅本地记录)</li>
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

    /**
     * 下线 EdgeOne 部署站点(伪删除)。
     * <p>
     * EdgeOne CLI 无删除项目能力,此接口向原项目部署"站点已下线"
     * 提示页覆盖原内容(项目与 URL 保持不变)。同步执行,耗时与部署相当。
     */
    @PostMapping("/{id}/offline")
    public Result<DeploymentVO> offline(@PathVariable Long id) {
        log.info("下线站点请求: id={}", id);
        // 不带自定义成功消息:CLI 执行失败时记录状态为失败,由前端按记录状态提示
        return Result.ok(deploymentService.offline(id));
    }

    /**
     * 删除部署记录(仅删除本地记录,EdgeOne 线上站点不受影响)。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除部署记录请求: id={}", id);
        deploymentService.deleteMine(id);
        return Result.ok("记录已删除", null);
    }
}

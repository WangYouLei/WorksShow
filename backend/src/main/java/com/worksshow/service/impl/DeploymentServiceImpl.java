package com.worksshow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.worksshow.client.EdgeOnePagesClient;
import com.worksshow.dto.DecryptedEdgeOneConfig;
import com.worksshow.dto.DeploymentRequestDTO;
import com.worksshow.dto.DeploymentVO;
import com.worksshow.dto.EdgeOneDeployResult;
import com.worksshow.entity.Deployment;
import com.worksshow.entity.Portfolio;
import com.worksshow.exception.BusinessException;
import com.worksshow.mapper.DeploymentMapper;
import com.worksshow.security.UserContext;
import com.worksshow.service.DeploymentService;
import com.worksshow.service.PortfolioService;
import com.worksshow.service.UserEdgeoneConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * 模板部署服务实现
 * <p>
 * 编排部署全流程:校验简历归属 → 解密 EdgeOne 凭证 → 调用 CLI 部署 → 更新记录状态。
 * 部署为同步执行,部署期间 HTTP 请求阻塞等待 CLI 完成。
 *
 * @author WorksShow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeploymentServiceImpl extends ServiceImpl<DeploymentMapper, Deployment> implements DeploymentService {

    private final UserEdgeoneConfigService edgeoneConfigService;
    private final PortfolioService portfolioService;
    private final EdgeOnePagesClient edgeOnePagesClient;

    /** 本地部署站点文件存储目录(相对路径基于服务运行目录) */
    @Value("${local-deploy.dir:./local-sites}")
    private String localDeployDir;

    /** 本地部署站点对外基础地址(含 context-path,如 http://example.com/api);
     *  为空时按当前请求动态构造(反向代理场景建议显式配置) */
    @Value("${local-deploy.base-url:}")
    private String localDeployBaseUrl;

    /**
     * 部署简历模板(EdgeOne Pages 或 本服务器)
     */
    @Override
    public DeploymentVO deploy(DeploymentRequestDTO dto) {
        Long userId = UserContext.getCurrentUserId();

        // 1. 校验简历归属
        Portfolio portfolio = portfolioService.getById(dto.getPortfolioId());
        if (portfolio == null) {
            throw new BusinessException(404, "简历不存在");
        }
        if (!portfolio.getUserId().equals(userId)) {
            log.warn("越权部署简历: portfolioId={}, ownerId={}, currentUserId={}",
                    portfolio.getId(), portfolio.getUserId(), userId);
            throw new BusinessException(403, "无权操作该简历");
        }

        // 2. 部署方式:local-本服务器,其余按 EdgeOne 处理(兼容不传的旧前端)
        boolean local = Deployment.DEPLOY_TYPE_LOCAL.equals(dto.getDeployType());

        // 3. 确定项目名(仅展示用):
        //    EdgeOne 需校验配置并作为 CLI -n 参数;本地部署为空时用简历名称
        DecryptedEdgeOneConfig config = null;
        String projectName = (dto.getProjectName() != null && !dto.getProjectName().isBlank())
                ? dto.getProjectName()
                : null;
        if (local) {
            if (projectName == null) {
                projectName = portfolio.getName();
            }
        } else {
            config = edgeoneConfigService.getDecryptedConfig();
            if (projectName == null) {
                projectName = config.projectName();
            }
            if (projectName == null || projectName.isBlank()) {
                throw new BusinessException(400, "请填写EdgeOne项目名,或在配置中设置默认项目名");
            }
        }

        // 4. 先创建部署记录(状态=部署中),即使后续失败也有据可查
        Deployment deployment = new Deployment();
        deployment.setUserId(userId);
        deployment.setPortfolioId(portfolio.getId());
        deployment.setTemplateId(portfolio.getTemplateId());
        deployment.setProjectName(projectName);
        deployment.setDeployType(local ? Deployment.DEPLOY_TYPE_LOCAL : Deployment.DEPLOY_TYPE_EDGEONE);
        deployment.setDescription(dto.getDescription());
        deployment.setStatus(Deployment.STATUS_DEPLOYING);
        save(deployment);
        log.info("创建部署记录: id={}, userId={}, portfolioId={}, deployType={}",
                deployment.getId(), userId, portfolio.getId(), deployment.getDeployType());

        // 5. 执行部署
        if (local) {
            deployToLocal(deployment, dto.getHtml());
        } else {
            deployToEdgeOne(deployment, dto.getHtml(), config, projectName);
        }
        updateById(deployment);
        log.info("部署完成: id={}, success={}", deployment.getId(),
                deployment.getStatus() == Deployment.STATUS_SUCCESS);

        return toVO(deployment);
    }

    /**
     * 本地部署:HTML 写入站点目录,由本服务通过 /api/sites/{key} 公开提供访问。
     * 同一简历重复部署复用同一 key(覆盖更新,访问链接保持不变)。
     */
    private void deployToLocal(Deployment deployment, String html) {
        try {
            String key = reuseOrCreateLocalKey(deployment.getUserId(), deployment.getPortfolioId());
            Path dir = Paths.get(localDeployDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path file = dir.resolve(key + ".html");
            Files.writeString(file, html, StandardCharsets.UTF_8);
            log.info("本地部署写入文件: {}", file);

            deployment.setLocalKey(key);
            deployment.setStatus(Deployment.STATUS_SUCCESS);
            deployment.setDeployUrl(buildLocalSiteUrl(key));
        } catch (IOException e) {
            log.error("本地部署写入文件失败: deploymentId={}", deployment.getId(), e);
            deployment.setStatus(Deployment.STATUS_FAILED);
            deployment.setErrorMessage("写入部署文件失败: " + e.getMessage());
        }
    }

    /** EdgeOne 部署:调用 CLI 上传 */
    private void deployToEdgeOne(Deployment deployment, String html,
                                 DecryptedEdgeOneConfig config, String projectName) {
        EdgeOneDeployResult result = edgeOnePagesClient.deploy(
                html, config.apiToken(), projectName);
        if (result.success()) {
            deployment.setStatus(Deployment.STATUS_SUCCESS);
            deployment.setDeployUrl(result.deployUrl());
        } else {
            deployment.setStatus(Deployment.STATUS_FAILED);
            deployment.setErrorMessage(result.errorMessage());
        }
    }

    /**
     * 查找该简历最近一次成功的本地部署 key 以复用(链接不变);
     * 无则生成新 key(32位随机hex)。
     */
    private String reuseOrCreateLocalKey(Long userId, Long portfolioId) {
        // 按用户+简历串行化:并发部署同一简历时先查后插会产生两个 key,
        // 导致新旧链接内容分叉(单实例部署足够)
        synchronized (("local-deploy:" + userId + ":" + portfolioId).intern()) {
            Deployment last = lambdaQuery()
                    .eq(Deployment::getUserId, userId)
                    .eq(Deployment::getPortfolioId, portfolioId)
                    .eq(Deployment::getDeployType, Deployment.DEPLOY_TYPE_LOCAL)
                    .eq(Deployment::getStatus, Deployment.STATUS_SUCCESS)
                    .isNotNull(Deployment::getLocalKey)
                    .orderByDesc(Deployment::getCreateTime)
                    .last("LIMIT 1")
                    .one();
            return last != null ? last.getLocalKey() : UUID.randomUUID().toString().replace("-", "");
        }
    }

    /** 构造本地站点完整访问URL:配置了 base-url 用配置,否则按当前请求动态构造 */
    private String buildLocalSiteUrl(String key) {
        if (localDeployBaseUrl != null && !localDeployBaseUrl.isBlank()) {
            return localDeployBaseUrl.replaceAll("/+$", "") + "/sites/" + key;
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/sites/").path(key)
                .build().toUriString();
    }

    /**
     * 获取当前用户的部署记录列表
     */
    @Override
    public List<DeploymentVO> listMine() {
        Long userId = UserContext.getCurrentUserId();
        List<Deployment> list = list(new LambdaQueryWrapper<Deployment>()
                .eq(Deployment::getUserId, userId)
                .orderByDesc(Deployment::getCreateTime));
        return list.stream().map(this::toVO).toList();
    }

    /**
     * 获取部署详情
     */
    @Override
    public DeploymentVO getDetail(Long id) {
        Deployment deployment = getById(id);
        if (deployment == null) {
            throw new BusinessException(404, "部署记录不存在");
        }
        checkOwnership(deployment);
        return toVO(deployment);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 校验部署记录归属当前登录用户
     */
    private void checkOwnership(Deployment deployment) {
        Long userId = UserContext.getCurrentUserId();
        if (!deployment.getUserId().equals(userId)) {
            log.warn("越权访问部署记录: deploymentId={}, ownerId={}, currentUserId={}",
                    deployment.getId(), deployment.getUserId(), userId);
            throw new BusinessException(403, "无权操作该部署记录");
        }
    }

    /**
     * 实体转 VO
     */
    private DeploymentVO toVO(Deployment d) {
        DeploymentVO vo = new DeploymentVO();
        vo.setId(d.getId());
        vo.setPortfolioId(d.getPortfolioId());
        vo.setTemplateId(d.getTemplateId());
        vo.setProjectName(d.getProjectName());
        vo.setDeployType(d.getDeployType());
        vo.setDescription(d.getDescription());
        vo.setStatus(d.getStatus());
        vo.setDeployUrl(d.getDeployUrl());
        vo.setErrorMessage(d.getErrorMessage());
        vo.setCreateTime(d.getCreateTime());
        vo.setUpdateTime(d.getUpdateTime());
        return vo;
    }
}

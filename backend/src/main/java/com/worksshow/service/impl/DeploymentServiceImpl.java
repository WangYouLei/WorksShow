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
import org.springframework.stereotype.Service;

import java.util.List;

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

    /**
     * 部署简历模板到 EdgeOne Pages
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

        // 2. 获取解密后的 EdgeOne 配置(未配置则抛异常)
        DecryptedEdgeOneConfig config = edgeoneConfigService.getDecryptedConfig();

        // 3. 确定项目名:请求传入优先,否则用配置中的默认项目名
        String projectName = (dto.getProjectName() != null && !dto.getProjectName().isBlank())
                ? dto.getProjectName()
                : config.projectName();
        if (projectName == null || projectName.isBlank()) {
            throw new BusinessException(400, "请填写EdgeOne项目名,或在配置中设置默认项目名");
        }

        // 4. 先创建部署记录(状态=部署中),即使后续失败也有据可查
        Deployment deployment = new Deployment();
        deployment.setUserId(userId);
        deployment.setPortfolioId(portfolio.getId());
        deployment.setTemplateId(portfolio.getTemplateId());
        deployment.setProjectName(projectName);
        deployment.setDescription(dto.getDescription());
        deployment.setStatus(Deployment.STATUS_DEPLOYING);
        save(deployment);
        log.info("创建部署记录: id={}, userId={}, portfolioId={}", deployment.getId(), userId, portfolio.getId());

        // 5. 调用 EdgeOne CLI 部署
        EdgeOneDeployResult result = edgeOnePagesClient.deploy(
                dto.getHtml(), config.apiToken(), projectName);

        // 6. 根据结果更新记录
        if (result.success()) {
            deployment.setStatus(Deployment.STATUS_SUCCESS);
            deployment.setDeployUrl(result.deployUrl());
        } else {
            deployment.setStatus(Deployment.STATUS_FAILED);
            deployment.setErrorMessage(result.errorMessage());
        }
        updateById(deployment);
        log.info("部署完成: id={}, success={}", deployment.getId(), result.success());

        return toVO(deployment);
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
        vo.setDescription(d.getDescription());
        vo.setStatus(d.getStatus());
        vo.setDeployUrl(d.getDeployUrl());
        vo.setErrorMessage(d.getErrorMessage());
        vo.setCreateTime(d.getCreateTime());
        vo.setUpdateTime(d.getUpdateTime());
        return vo;
    }
}

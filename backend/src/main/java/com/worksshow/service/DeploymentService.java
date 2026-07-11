package com.worksshow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.worksshow.dto.DeploymentRequestDTO;
import com.worksshow.dto.DeploymentVO;
import com.worksshow.entity.Deployment;

import java.util.List;

/**
 * 模板部署服务接口
 * <p>
 * 管理简历模板部署到 EdgeOne Pages 的完整流程:
 * 校验简历归属 → 获取 EdgeOne 凭证 → 调用 CLI 部署 → 记录结果。
 *
 * @author WorksShow
 */
public interface DeploymentService extends IService<Deployment> {

    /**
     * 部署简历模板到 EdgeOne Pages
     * <p>
     * 同步执行:前端渲染好 HTML 后提交,后端调用 CLI 部署并返回结果。
     *
     * @param dto 部署请求(简历ID + HTML + 项目名)
     * @return 部署记录(含状态与访问URL)
     */
    DeploymentVO deploy(DeploymentRequestDTO dto);

    /**
     * 获取当前用户的部署记录列表
     *
     * @return 部署列表,按创建时间倒序
     */
    List<DeploymentVO> listMine();

    /**
     * 获取部署详情
     * <p>
     * 需校验部署记录归属于当前登录用户。
     *
     * @param id 部署记录ID
     * @return 部署详情
     */
    DeploymentVO getDetail(Long id);
}

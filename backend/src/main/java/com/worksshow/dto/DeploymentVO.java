package com.worksshow.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部署记录响应 VO
 * <p>
 * 不包含用户敏感信息,仅返回部署状态与访问 URL。
 *
 * @author WorksShow
 */
@Data
public class DeploymentVO {

    /** 部署记录ID */
    private Long id;

    /** 简历实例ID */
    private Long portfolioId;

    /** 模板ID */
    private String templateId;

    /** EdgeOne 项目名 */
    private String projectName;

    /** 部署描述 */
    private String description;

    /** 部署状态:0-部署中 1-成功 2-失败 */
    private Integer status;

    /** 部署成功后的访问URL */
    private String deployUrl;

    /** 部署失败时的错误信息 */
    private String errorMessage;

    /** 创建时间(部署发起时间) */
    private LocalDateTime createTime;

    /** 更新时间(部署完成时间) */
    private LocalDateTime updateTime;
}

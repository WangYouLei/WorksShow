package com.worksshow.dto;

/**
 * EdgeOne CLI 部署结果(仅内部使用)
 * <p>
 * 解析 edgeone pages deploy 命令的 stdout 得到。
 * 仅供 DeploymentService 内部使用,不通过 Controller 返回。
 *
 * @param success      是否成功
 * @param deployUrl    部署成功后的访问URL(含 eo_token/eo_time 查询参数,必须完整保留)
 * @param projectId    EdgeOne 项目ID(如 pages-xxxxxxxx,由 CLI 返回)
 * @param errorMessage 失败时的错误信息
 * @author WorksShow
 */
public record EdgeOneDeployResult(
        boolean success,
        String deployUrl,
        String projectId,
        String errorMessage
) {
}

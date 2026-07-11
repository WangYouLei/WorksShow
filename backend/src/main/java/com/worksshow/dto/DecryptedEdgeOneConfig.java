package com.worksshow.dto;

/**
 * 解密后的 EdgeOne 配置(仅内部使用)
 * <p>
 * 部署服务调用 EdgeOne CLI 时需要明文 API Token,此 record
 * 用于在 Service 层之间传递解密后的明文。严禁出现在 Controller 响应中。
 *
 * @param apiToken   明文 API Token
 * @param projectName 项目名(CLI -n参数用,可空,为空则需部署时单独指定)
 * @author WorksShow
 */
public record DecryptedEdgeOneConfig(
        String apiToken,
        String projectName
) {
}

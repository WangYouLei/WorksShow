package com.worksshow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 模板部署请求 DTO
 * <p>
 * 前端将渲染好的完整 HTML 发送给后端,后端调用 EdgeOne CLI 部署。
 * 一次部署对应一个简历实例(portfolio)。
 *
 * @author WorksShow
 */
@Data
public class DeploymentRequestDTO {

    /** 简历实例ID(关联 portfolio.id) */
    @NotNull(message = "简历实例ID不能为空")
    private Long portfolioId;

    /** 部署方式:edgeone-EdgeOne Pages(默认) local-本服务器 */
    private String deployType;

    /** 渲染后的完整 HTML 内容(由前端模板渲染产出,限制 500KB 防止滥用) */
    @NotBlank(message = "部署内容(HTML)不能为空")
    @Size(max = 500_000, message = "HTML 内容不能超过 500KB")
    private String html;

    /** EdgeOne 项目名(CLI -n 参数用,为空则用用户配置中的默认项目名)。
     *  仅允许字母开头+字母/数字/下划线/连字符,防止以 - 开头被 CLI 解析为其他选项(参数注入) */
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9-_]{0,99}$",
            message = "项目名须以字母或数字开头,仅可包含字母/数字/下划线/连字符")
    @Size(max = 100, message = "项目名长度不能超过100")
    private String projectName;

    /** 部署描述(可选) */
    @Size(max = 500, message = "部署描述长度不能超过500")
    private String description;
}

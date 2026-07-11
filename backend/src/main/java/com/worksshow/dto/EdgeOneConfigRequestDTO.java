package com.worksshow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * EdgeOne 配置保存/更新请求 DTO
 * <p>
 * 前端传入明文 API Token,后端 AES 加密后入库。
 * 一次配置后多次部署复用,无需每次填写。
 *
 * @author WorksShow
 */
@Data
public class EdgeOneConfigRequestDTO {

    /** EdgeOne Pages API Token(明文,入库前加密) */
    @NotBlank(message = "API Token不能为空")
    @Size(max = 255, message = "API Token长度不能超过255")
    private String apiToken;

    /** EdgeOne Pages 项目名(CLI -n参数用,可选,部署时的默认项目名) */
    @Size(max = 100, message = "项目名长度不能超过100")
    private String projectName;
}

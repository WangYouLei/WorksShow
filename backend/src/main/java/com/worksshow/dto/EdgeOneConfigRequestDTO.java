package com.worksshow.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * EdgeOne 配置保存/更新请求 DTO
 * <p>
 * 前端传入明文 API Token,后端 AES 加密后入库。
 * 一次配置后多次部署复用,无需每次填写。
 * <p>
 * 更新场景下 apiToken 可留空(仅更新 projectName);
 * 首次配置时 apiToken 必填,由 Service 层校验。
 *
 * @author WorksShow
 */
@Data
public class EdgeOneConfigRequestDTO {

    /** EdgeOne Pages API Token(明文,入库前加密;更新时可留空表示不修改) */
    @Size(max = 255, message = "API Token长度不能超过255")
    private String apiToken;

    /** EdgeOne Pages 项目名(CLI -n参数用,可选,部署时的默认项目名) */
    @Size(max = 100, message = "项目名长度不能超过100")
    private String projectName;
}

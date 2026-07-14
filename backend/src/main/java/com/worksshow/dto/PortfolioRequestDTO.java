package com.worksshow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 简历实例创建/更新请求 DTO
 * <p>
 * 仅包含简历实例本身的字段(模板ID + 简历名称)。
 * 简历内容(个人信息/作品/经历/技能)统一通过 /api/user-profile 管理,
 * 所有模板共享同一份用户档案。
 *
 * @author WorksShow
 */
@Data
public class PortfolioRequestDTO {

    /** 模板ID(aurora/mo-yun/pastel) */
    @NotBlank(message = "模板ID不能为空")
    private String templateId;

    /** 简历名称(用户自定义,1-50 字符) */
    @NotBlank(message = "简历名称不能为空")
    @Size(max = 50, message = "简历名称长度不能超过50")
    private String name;
}

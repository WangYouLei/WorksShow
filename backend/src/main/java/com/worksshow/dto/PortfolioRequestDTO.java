package com.worksshow.dto;

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
    private String templateId;

    /** 简历名称(用户自定义) */
    private String name;
}

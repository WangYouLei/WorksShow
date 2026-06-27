package com.worksshow.dto;

import lombok.Data;

import java.util.List;

/**
 * 简历完整数据 DTO(简历详情/渲染数据)
 * <p>
 * 用于 GET /api/portfolio/{id} 返回的简历详情,
 * 由"简历实例元数据"(id/templateId/name) + "用户档案内容"
 * (profile/works/experiences/skills)组合而成。
 * 用户档案内容来自 user_* 表,所有模板共享同一份。
 * <p>
 * 内部 DTO 类复用 UserProfileDTO 中的定义,避免重复。
 *
 * @author WorksShow
 */
@Data
public class PortfolioDataDTO {

    /** 简历ID */
    private Long id;

    /** 模板ID(aurora/mo-yun/pastel) */
    private String templateId;

    /** 简历名称 */
    private String name;

    /** 个人信息(来自 user_profile) */
    private UserProfileDTO.ProfileDTO profile;

    /** 作品列表(来自 user_work) */
    private List<UserProfileDTO.WorkDTO> works;

    /** 经历列表(来自 user_experience) */
    private List<UserProfileDTO.ExperienceDTO> experiences;

    /** 技能列表(来自 user_skill) */
    private List<UserProfileDTO.SkillDTO> skills;
}

package com.worksshow.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 用户简历档案 DTO(用户档案完整数据)
 * <p>
 * 接收/返回用户档案的完整数据,包含个人信息(profile)与
 * 作品/经历/技能列表。用于 GET/PUT /api/user-profile。
 * 一个用户只有一份档案,所有简历模板共享。
 * <p>
 * 内部类(ProfileDTO/WorkDTO/ExperienceDTO/SkillDTO/StatDTO/SocialDTO)
 * 同时被 PortfolioDataDTO 复用,避免重复定义。
 *
 * @author WorksShow
 */
@Data
public class UserProfileDTO {

    /** 个人信息 */
    private ProfileDTO profile;

    /** 作品列表 */
    private List<WorkDTO> works;

    /** 经历列表(工作 + 教育) */
    private List<ExperienceDTO> experiences;

    /** 技能列表 */
    private List<SkillDTO> skills;

    /**
     * 个人信息
     * <p>
     * 对应 UserProfile 实体的 p_* 字段:name→pName、nameEn→pNameEn、
     * title→pTitle、tagline→pTagline、bio→pBio、
     * email→pEmail、location→pLocation、availability→pAvailability、
     * stats→pStats、socials→pSocials。
     */
    @Data
    public static class ProfileDTO {
        /** 中文姓名(→pName) */
        private String name;
        /** 英文名(→pNameEn) */
        private String nameEn;
        /** 职位/方向(→pTitle) */
        private String title;
        /** 一句话标语(→pTagline) */
        private String tagline;
        /** 个人简介(→pBio) */
        private String bio;
        /** 展示用邮箱(→pEmail,与 DB 列 VARCHAR(100) 对齐) */
        @Size(max = 100, message = "邮箱长度不能超过 100 个字符")
        private String email;
        /** 所在地(→pLocation) */
        private String location;
        /** 状态(→pAvailability) */
        private String availability;
        /** 统计项(→pStats) */
        private List<StatDTO> stats;
        /** 社交链接(→pSocials) */
        private List<SocialDTO> socials;
    }

    /**
     * 作品
     */
    @Data
    public static class WorkDTO {
        /** 记录ID(更新时传入定位记录,新建时为 null) */
        private Long id;
        /** 作品标题 */
        private String title;
        /** 分类(如"AI产品") */
        private String category;
        /** 封面图URL */
        private String cover;
        /** 作品描述 */
        private String description;
        /** 年份 */
        private String year;
        /** 外链URL */
        private String link;
        /** 技术标签数组 */
        private List<String> tags;
        /** 是否高亮旗舰(前端布尔,入库转 Integer) */
        private Boolean highlight;
    }

    /**
     * 经历(工作 / 教育)
     */
    @Data
    public static class ExperienceDTO {
        /** 记录ID(更新时传入定位记录,新建时为 null) */
        private Long id;
        /** 时间段(如"2022-2024") */
        private String period;
        /** 职位/学位 */
        private String title;
        /** 机构/学校 */
        private String organization;
        /** 经历描述 */
        private String description;
        /** 类型:work-工作经历 education-教育经历 */
        private String type;
    }

    /**
     * 技能(按分类)
     */
    @Data
    public static class SkillDTO {
        /** 记录ID(更新时传入定位记录,新建时为 null) */
        private Long id;
        /** 分类中文名(如"前端开发") */
        private String category;
        /** 分类英文名(如"Frontend") */
        private String categoryEn;
        /** 技能项数组 */
        private List<String> items;
    }

    /**
     * 统计项
     */
    @Data
    public static class StatDTO {
        /** 标签(如"年经验") */
        private String label;
        /** 值(如"5+") */
        private String value;
    }

    /**
     * 社交链接
     */
    @Data
    public static class SocialDTO {
        /** 平台名称(如"GitHub") */
        private String name;
        /** 链接URL */
        private String url;
        /** 图标(图标名或URL) */
        private String icon;
    }
}

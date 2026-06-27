package com.worksshow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户简历档案表实体 - 对应数据库 user_profile 表
 * <p>
 * 存储用户级个人信息,所有简历模板共享此档案。
 * 与 user 表 1:1 关联(懒创建:用户首次保存档案时才 INSERT)。
 * p_email 为简历展示用邮箱,可与登录邮箱(user.email)不同。
 * 统计项(pStats)与社交链接(pSocials)以 JSON 列存储。
 * autoResultMap=true 用于让 JacksonTypeHandler 生效。
 *
 * @author WorksShow
 */
@Data
@TableName(value = "user_profile", autoResultMap = true)
public class UserProfile {

    /** 主键ID(数据库自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID(关联 user.id) */
    private Long userId;

    /** 中文姓名 */
    private String pName;

    /** 英文名 */
    private String pNameEn;

    /** 职位/方向(如"独立开发者") */
    private String pTitle;

    /** 一句话标语/Hero 副标题 */
    private String pTagline;

    /** 个人简介(关于我正文) */
    private String pBio;

    /** 展示用邮箱(可与登录邮箱不同) */
    private String pEmail;

    /** 所在地 */
    private String pLocation;

    /** 状态(如"通常24小时内回复") */
    private String pAvailability;

    /** 统计项数组(JSON 列,如 [{label,value}]) */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Stat> pStats;

    /** 社交链接数组(JSON 列,如 [{name,url,icon}]) */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Social> pSocials;

    /** 创建时间(数据库自动维护) */
    private LocalDateTime createTime;

    /** 更新时间(数据库自动维护) */
    private LocalDateTime updateTime;

    /** 逻辑删除标志:0-未删除 1-已删除(Mybatis-Plus 自动处理) */
    @TableLogic
    private Integer deleted;

    /**
     * 统计项(简历顶部展示的数字,如"5+ 年经验")
     */
    @Data
    public static class Stat {
        /** 标签(如"年经验") */
        private String label;
        /** 值(如"5+") */
        private String value;
    }

    /**
     * 社交链接(如 GitHub / Twitter)
     */
    @Data
    public static class Social {
        /** 平台名称(如"GitHub") */
        private String name;
        /** 链接URL */
        private String url;
        /** 图标(图标名或URL) */
        private String icon;
    }
}

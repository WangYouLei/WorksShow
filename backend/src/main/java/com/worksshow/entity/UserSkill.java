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
 * 用户技能表实体 - 对应数据库 user_skill 表
 * <p>
 * 用户级技能档案,所有简历模板共享。
 * 按分类存储技能,items 以 JSON 列存储技能项数组。
 * autoResultMap=true 用于让 JacksonTypeHandler 生效。
 *
 * @author WorksShow
 */
@Data
@TableName(value = "user_skill", autoResultMap = true)
public class UserSkill {

    /** 主键ID(数据库自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID(关联 user.id) */
    private Long userId;

    /** 分类中文名(如"前端开发") */
    private String category;

    /** 分类英文名(如"Frontend") */
    private String categoryEn;

    /** 技能项数组(JSON 列,如 ["Vue","React","TypeScript"]) */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> items;

    /** 排序序号(越小越靠前) */
    private Integer sortOrder;

    /** 创建时间(数据库自动维护) */
    private LocalDateTime createTime;

    /** 更新时间(数据库自动维护) */
    private LocalDateTime updateTime;

    /** 逻辑删除标志:0-未删除 1-已删除(Mybatis-Plus 自动处理) */
    @TableLogic
    private Integer deleted;
}

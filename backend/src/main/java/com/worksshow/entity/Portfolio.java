package com.worksshow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历主表实体 - 对应数据库 portfolio 表
 * <p>
 * 一份简历绑定一个模板,仅记录"模板实例"元数据(模板ID + 简历名称)。
 * 简历内容(个人信息/作品/经历/技能)统一存于 user_* 档案表,
 * 所有模板共享同一份用户档案,无需在每份简历重复填写。
 *
 * @author WorksShow
 */
@Data
@TableName(value = "portfolio", autoResultMap = true)
public class Portfolio {

    /** 主键ID(数据库自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID(关联 user.id) */
    private Long userId;

    /** 模板ID(aurora/mo-yun/pastel) */
    private String templateId;

    /** 简历名称(用户自定义) */
    private String name;

    /** 创建时间(数据库自动维护) */
    private LocalDateTime createTime;

    /** 更新时间(数据库自动维护) */
    private LocalDateTime updateTime;

    /** 逻辑删除标志:0-未删除 1-已删除(Mybatis-Plus 自动处理) */
    @TableLogic
    private Integer deleted;
}

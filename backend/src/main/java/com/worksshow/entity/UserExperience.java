package com.worksshow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户经历表实体 - 对应数据库 user_experience 表
 * <p>
 * 用户级经历档案,所有简历模板共享。
 * 工作经历与教育经历统一存储,通过 type 字段区分。
 *
 * @author WorksShow
 */
@Data
@TableName(value = "user_experience", autoResultMap = true)
public class UserExperience {

    /** 主键ID(数据库自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID(关联 user.id) */
    private Long userId;

    /** 时间段(如"2024"或"2022-2024") */
    private String period;

    /** 职位/学位 */
    private String title;

    /** 机构/学校 */
    private String organization;

    /** 经历描述 */
    private String description;

    /** 类型:work-工作经历 education-教育经历 */
    private String type;

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

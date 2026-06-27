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
 * 求职意向实体 - 对应数据库 career_intention 表
 * <p>
 * 一个用户一份求职意向(由数据库唯一索引 uk_user 约束),
 * 期望行业/城市以 JSON 列存储。
 * autoResultMap=true 用于让 JacksonTypeHandler 生效。
 *
 * @author WorksShow
 */
@Data
@TableName(value = "career_intention", autoResultMap = true)
public class CareerIntention {

    /** 主键ID(数据库自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID(关联 user.id) */
    private Long userId;

    /** 求职状态:0-离职正在找 1-在职考虑机会 2-在职暂不考虑 */
    private Integer jobStatus;

    /** 期望职位(如"前端开发工程师") */
    private String expectedPosition;

    /** 职位类别(技术/产品/设计/运营等) */
    private String positionType;

    /** 期望行业数组(JSON 列,如 ["互联网","金融"]) */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> expectedIndustry;

    /** 期望城市数组(JSON 列,如 ["北京","上海"]) */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> expectedCity;

    /** 工作性质:0-全职 1-兼职 2-实习 3-远程 */
    private Integer workType;

    /** 期望最低薪资(千元,如15表示15k) */
    private Integer salaryMin;

    /** 期望最高薪资(千元,如25表示25k) */
    private Integer salaryMax;

    /** 到岗时间(如"随时""一周内""一个月内") */
    private String arrivalTime;

    /** 自我评价/求职宣言(可选) */
    private String selfEvaluation;

    /** 创建时间(数据库自动维护) */
    private LocalDateTime createTime;

    /** 更新时间(数据库自动维护) */
    private LocalDateTime updateTime;

    /** 逻辑删除标志:0-未删除 1-已删除(Mybatis-Plus 自动处理) */
    @TableLogic
    private Integer deleted;
}

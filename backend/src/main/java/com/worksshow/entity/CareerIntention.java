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

    /** 期望最低薪资(元,如15000) */
    private Integer salaryMin;

    /** 期望最高薪资(元,如25000) */
    private Integer salaryMax;

    /** 到岗时间(日期字符串,如"2026-07-01") */
    private String arrivalTime;

    /** 自我评价/求职宣言(可选) */
    private String selfEvaluation;

    /** 创建时间(数据库自动维护) */
    private LocalDateTime createTime;

    /** 更新时间(数据库自动维护) */
    private LocalDateTime updateTime;

    /** 逻辑删除:0-未删除 非0-已删除(删除时置为本行主键ID,避免与唯一索引冲突,Mybatis-Plus 自动处理) */
    @TableLogic(value = "0", delval = "id")
    private Long deleted;
}

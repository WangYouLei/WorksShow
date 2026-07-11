package com.worksshow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户自定义域名实体 - 对应数据库 custom_domain 表
 * <p>
 * 用户保存的自定义域名,可独立复用,一个域名可关联多次部署
 * (多个页面通过 deployment.path 子路径区分)。
 *
 * @author WorksShow
 */
@Data
@TableName(value = "custom_domain", autoResultMap = true)
public class CustomDomain {

    /** 主键ID(数据库自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID(关联 user.id) */
    private Long userId;

    /** 域名(如 myresume.edgeone.app) */
    private String domain;

    /** 域名备注名(展示用,如"求职简历域名") */
    private String name;

    /** 创建时间(数据库自动维护) */
    private LocalDateTime createTime;

    /** 更新时间(数据库自动维护) */
    private LocalDateTime updateTime;

    /** 逻辑删除标志:0-未删除 1-已删除(Mybatis-Plus 自动处理) */
    @TableLogic
    private Integer deleted;
}

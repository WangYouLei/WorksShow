package com.worksshow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 生成模板实体 - 对应数据库 ai_template 表
 * <p>
 * 存储按用户需求生成的完整单文件 HTML 模板。
 * 不可表单编辑,重新生成写入新记录并注入最新档案数据;
 * 首次部署时懒创建 portfolio 实例复用现有部署链路。
 *
 * @author WorksShow
 */
@Data
@TableName("ai_template")
public class AiTemplate {

    /** 主键ID(数据库自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 模板名称 */
    private String name;

    /** 用户的设计需求描述 */
    private String requirement;

    /** 生成的完整单文件 HTML */
    private String html;

    /** 懒创建的简历实例ID(首次部署时创建) */
    private Long portfolioId;

    /** 创建时间(数据库自动维护) */
    private LocalDateTime createTime;
}

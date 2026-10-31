package com.worksshow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 对话消息实体 - 对应数据库 ai_message 表
 * <p>
 * 对话按 portfolio_id 分组,多轮上下文取最近若干条。
 * actions_json 仅 assistant 消息有值,记录 AI 建议的修改动作,
 * 由前端确认后再应用到简历数据。
 *
 * @author WorksShow
 */
@Data
@TableName("ai_message")
public class AiMessage {

    /** 主键ID(数据库自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 所属简历ID */
    private Long portfolioId;

    /** 角色: user / assistant */
    private String role;

    /** 对话正文 */
    private String content;

    /** AI 建议的修改动作(JSON 数组) */
    private String actionsJson;

    /** 创建时间(数据库自动维护) */
    private LocalDateTime createTime;
}

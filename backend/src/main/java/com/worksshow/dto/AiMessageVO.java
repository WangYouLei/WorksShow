package com.worksshow.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 对话消息 VO(用于历史记录回显)
 *
 * @author WorksShow
 */
@Data
public class AiMessageVO {

    /** 消息ID */
    private Long id;

    /** 角色: user / assistant */
    private String role;

    /** 对话正文 */
    private String content;

    /** AI 建议的修改动作(仅 assistant 消息有值) */
    private List<Map<String, Object>> actions;

    /** 创建时间 */
    private LocalDateTime createTime;
}

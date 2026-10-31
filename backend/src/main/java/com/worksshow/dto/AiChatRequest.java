package com.worksshow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * AI 对话请求
 *
 * @author WorksShow
 */
@Data
public class AiChatRequest {

    /** 简历ID(对话按简历分组,同时用于归属校验与每日配额统计) */
    @NotNull(message = "简历ID不能为空")
    private Long portfolioId;

    /** 用户消息内容 */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容过长(最多 2000 字)")
    private String message;

    /**
     * 当前主题配置
     * <p>
     * 主题暂未持久化,由前端随请求带上,供 AI 参考当前配色后再给出调整建议。
     */
    private Map<String, Object> theme;
}

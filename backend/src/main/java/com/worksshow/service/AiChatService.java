package com.worksshow.service;

import com.worksshow.dto.AiChatRequest;
import com.worksshow.dto.AiMessageVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 对话服务
 * <p>
 * 面向「作品集编辑器」的 AI 助理:接收用户消息, 流式返回正文,
 * 并附带经过白名单校验的结构化修改动作。所有修改都由前端确认后应用,
 * 后端不直接改写简历数据。
 *
 * @author WorksShow
 */
public interface AiChatService {

    /**
     * 发起一轮对话(SSE 流式)
     * <p>
     * 事件序列: delta(多次, 正文增量) → actions(一次, 动作数组) → done。
     * 失败时以 error 事件结束。
     *
     * @param request 对话请求
     * @return SSE 发射器
     */
    SseEmitter chat(AiChatRequest request);

    /**
     * 获取某份简历的对话历史(按时间正序)
     *
     * @param portfolioId 简历ID
     * @return 消息列表
     */
    List<AiMessageVO> history(Long portfolioId);

    /**
     * 清空某份简历的对话历史
     *
     * @param portfolioId 简历ID
     */
    void clear(Long portfolioId);
}

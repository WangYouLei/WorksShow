package com.worksshow.controller;

import com.worksshow.common.Result;
import com.worksshow.dto.AiChatRequest;
import com.worksshow.dto.AiMessageVO;
import com.worksshow.service.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 对话接口
 * <p>
 * 实际访问路径前缀为 /api(由 server.servlet.context-path 配置)。
 * <ul>
 *   <li>POST   /api/ai/chat       发起一轮对话(SSE 流式)</li>
 *   <li>GET    /api/ai/messages   获取某份简历的对话历史</li>
 *   <li>DELETE /api/ai/messages   清空某份简历的对话历史</li>
 * </ul>
 *
 * @author WorksShow
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    /**
     * 发起一轮对话
     * <p>
     * 响应为 SSE 事件流: delta(正文增量) → actions(动作数组) → done;
     * 失败时以 error 事件结束。显式声明 UTF-8,避免中文在流中乱码。
     */
    @PostMapping(value = "/chat", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chat(@Valid @RequestBody AiChatRequest request) {
        log.info("AI 对话请求: portfolioId={}", request.getPortfolioId());
        return aiChatService.chat(request);
    }

    /**
     * 获取对话历史(按时间正序)
     */
    @GetMapping("/messages")
    public Result<List<AiMessageVO>> messages(@RequestParam Long portfolioId) {
        log.info("获取 AI 对话历史: portfolioId={}", portfolioId);
        return Result.ok(aiChatService.history(portfolioId));
    }

    /**
     * 清空对话历史
     */
    @DeleteMapping("/messages")
    public Result<Void> clear(@RequestParam Long portfolioId) {
        log.info("清空 AI 对话: portfolioId={}", portfolioId);
        aiChatService.clear(portfolioId);
        return Result.ok("已清空对话", null);
    }
}

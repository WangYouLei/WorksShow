package com.worksshow.controller;

import com.worksshow.common.Result;
import com.worksshow.dto.AiTemplateGenerateRequest;
import com.worksshow.dto.AiTemplateVO;
import com.worksshow.service.AiTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 模板生成接口
 * <p>
 * 实际访问路径前缀为 /api(由 server.servlet.context-path 配置)。
 * <ul>
 *   <li>POST   /api/ai/template/generate        生成模板(SSE 流式)</li>
 *   <li>GET    /api/ai/template/list            我的 AI 模板列表(不含 html)</li>
 *   <li>GET    /api/ai/template/{id}            模板详情(含 html)</li>
 *   <li>DELETE /api/ai/template/{id}            删除模板</li>
 *   <li>GET    /api/ai/template/{id}/portfolio  获取部署用简历实例ID(懒创建)</li>
 * </ul>
 *
 * @author WorksShow
 */
@Slf4j
@RestController
@RequestMapping("/ai/template")
@RequiredArgsConstructor
public class AiTemplateController {

    private final AiTemplateService aiTemplateService;

    /**
     * 生成模板
     * <p>
     * 响应为 SSE 事件流: delta(HTML 增量,供进度展示) → done({id,name});
     * 失败时以 error 事件结束。前端在 done 后经详情接口获取清洗后的完整 HTML。
     */
    @PostMapping(value = "/generate", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter generate(@Valid @RequestBody AiTemplateGenerateRequest request) {
        log.info("AI 模板生成请求: name={}", request.getName());
        return aiTemplateService.generate(request);
    }

    /** 我的 AI 模板列表 */
    @GetMapping("/list")
    public Result<List<AiTemplateVO>> list() {
        return Result.ok(aiTemplateService.listMine());
    }

    /** 模板详情(含完整 HTML) */
    @GetMapping("/{id}")
    public Result<AiTemplateVO> detail(@PathVariable Long id) {
        log.info("获取 AI 模板详情: id={}", id);
        return Result.ok(aiTemplateService.detail(id));
    }

    /** 删除模板 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除 AI 模板: id={}", id);
        aiTemplateService.delete(id);
        return Result.ok("已删除", null);
    }

    /** 获取部署用简历实例ID(首次调用懒创建并回填) */
    @GetMapping("/{id}/portfolio")
    public Result<Long> portfolio(@PathVariable Long id) {
        log.info("获取 AI 模板部署实例: id={}", id);
        return Result.ok(aiTemplateService.ensureDeployPortfolio(id));
    }
}

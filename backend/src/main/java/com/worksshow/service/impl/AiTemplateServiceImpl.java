package com.worksshow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worksshow.client.AiChatClient;
import com.worksshow.config.AiProperties;
import com.worksshow.dto.AiTemplateGenerateRequest;
import com.worksshow.dto.AiTemplateVO;
import com.worksshow.dto.PortfolioRequestDTO;
import com.worksshow.dto.UserProfileDTO;
import com.worksshow.entity.AiTemplate;
import com.worksshow.exception.BusinessException;
import com.worksshow.mapper.AiTemplateMapper;
import com.worksshow.security.UserContext;
import com.worksshow.service.AiTemplateService;
import com.worksshow.service.PortfolioService;
import com.worksshow.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * AI 模板生成服务实现
 * <p>
 * 职责:
 * <ul>
 *   <li>每日配额校验(生成成本远高于对话)</li>
 *   <li>Prompt 组装(生成规范 + 当前用户档案 + 设计需求)</li>
 *   <li>流式转发 + 产物提取清洗(截取文档 + 剥离外链脚本)</li>
 *   <li>落库与懒创建部署用 portfolio 实例</li>
 * </ul>
 *
 * @author WorksShow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiTemplateServiceImpl implements AiTemplateService {

    /** 生成的 HTML 允许的最大字符数(防异常输出撑爆存储) */
    private static final int MAX_HTML_LENGTH = 200_000;

    /** 懒创建 portfolio 实例使用的模板标识 */
    private static final String AI_TEMPLATE_ID = "ai-generated";

    /** SSE 超时(毫秒): 长输出生成需要比对话更长的窗口 */
    private static final long SSE_TIMEOUT_MS = 300_000L;

    private static final String SYSTEM_PROMPT = """
            你是资深网页设计师兼前端工程师。请根据用户的设计需求,为其生成一个完整的个人作品集/简历单页网站。

            【硬性规范】
            1. 只输出一个完整的 HTML 文档: 以 <!DOCTYPE html> 开始, 以 </html> 结束; 不要输出任何解释文字、思考过程或 Markdown 代码块标记
            2. 所有 CSS 与 JavaScript 全部内联在文档中; 禁止引用任何外部 JS 文件; 字体可引用 Google Fonts(https://fonts.googleapis.com)
            3. 响应式布局, 手机与桌面端都要正常浏览; 页面滚动交互可用原生 JS 实现简单的出现动画
            4. 内容必须使用下方提供的真实简历数据, 不得虚构; 数据为空的区块直接省略
            5. 数据存在时应包含这些区块: 个人简介(姓名/头衔/一句话定位/简介)、技能、作品、经历, 页脚放联系方式
            6. 中文排版, 视觉完成度要高, 可以大胆使用渐变、网格、大面积色块, 但整体要协调统一

            【简历数据(JSON, 空数组表示没有数据)】
            %s
            """;

    private final AiProperties aiProperties;
    private final AiChatClient aiChatClient;
    private final AiTemplateMapper aiTemplateMapper;
    private final UserProfileService userProfileService;
    private final PortfolioService portfolioService;
    private final ObjectMapper objectMapper;
    private final TaskExecutor taskExecutor;

    @Override
    public SseEmitter generate(AiTemplateGenerateRequest request) {
        Long userId = requireUserId();
        checkDailyLimit(userId);
        // 档案数据必须在 HTTP 线程读取: getMyProfile 依赖请求上下文(Sa-Token),
        // 后台线程没有 HttpServletRequest 会抛错;chat 通道同理(generate 前先取 detail)
        UserProfileDTO profile;
        try {
            profile = userProfileService.getMyProfile();
        } catch (Exception e) {
            throw new BusinessException(500, "读取简历档案失败,请稍后重试");
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        taskExecutor.execute(() -> runGenerate(emitter, userId, request, profile));
        return emitter;
    }

    @Override
    public List<AiTemplateVO> listMine() {
        Long userId = requireUserId();
        return aiTemplateMapper.selectList(new LambdaQueryWrapper<AiTemplate>()
                        .eq(AiTemplate::getUserId, userId)
                        .orderByDesc(AiTemplate::getId))
                .stream()
                .map(AiTemplateVO::summary)
                .toList();
    }

    @Override
    public AiTemplateVO detail(Long id) {
        AiTemplate template = getOwned(id);
        return AiTemplateVO.detail(template);
    }

    @Override
    public void delete(Long id) {
        getOwned(id);
        aiTemplateMapper.deleteById(id);
        log.info("删除 AI 模板: id={}, userId={}", id, UserContext.getCurrentUserId());
    }

    @Override
    public Long ensureDeployPortfolio(Long id) {
        AiTemplate template = getOwned(id);
        if (template.getPortfolioId() != null) {
            return template.getPortfolioId();
        }
        PortfolioRequestDTO createReq = new PortfolioRequestDTO();
        createReq.setTemplateId(AI_TEMPLATE_ID);
        createReq.setName(template.getName());
        Long portfolioId = portfolioService.create(createReq).getId();
        AiTemplate update = new AiTemplate();
        update.setId(template.getId());
        update.setPortfolioId(portfolioId);
        aiTemplateMapper.updateById(update);
        log.info("AI 模板懒创建部署实例: templateId={}, portfolioId={}", id, portfolioId);
        return portfolioId;
    }

    /** 后台线程执行一次生成: 流式转发 → 提取清洗 → 落库 */
    private void runGenerate(SseEmitter emitter, Long userId, AiTemplateGenerateRequest request, UserProfileDTO profile) {
        try {
            List<Map<String, Object>> messages = buildMessages(profile, request.getRequirement());

            StringBuilder raw = new StringBuilder();
            Consumer<String> onDelta = text -> {
                raw.append(text);
                emit(emitter, "delta", Map.of("text", text));
            };
            aiChatClient.stream(messages, aiProperties.getTemplateMaxTokens(), onDelta);

            String html = extractHtml(raw.toString());
            AiTemplate entity = new AiTemplate();
            entity.setUserId(userId);
            entity.setName(resolveName(request));
            entity.setRequirement(request.getRequirement());
            entity.setHtml(html);
            aiTemplateMapper.insert(entity);

            emit(emitter, "done", Map.of("id", entity.getId(), "name", entity.getName()));
            emitter.complete();
            log.info("AI 模板生成完成: id={}, userId={}, length={}", entity.getId(), userId, html.length());
        } catch (Exception e) {
            log.warn("AI 模板生成失败: userId={}, error={}", userId, e.getMessage());
            String message = e instanceof BusinessException ? e.getMessage() : "AI 模板生成失败,请稍后重试";
            emit(emitter, "error", Map.of("message", message));
            emitter.complete();
        }
    }

    /** 组装消息: 系统提示词(生成规范 + 档案数据) + 用户需求 */
    private List<Map<String, Object>> buildMessages(UserProfileDTO profile, String requirement) throws Exception {
        String dataJson = objectMapper.writeValueAsString(profile);
        return List.of(
                Map.of("role", "system", "content", String.format(SYSTEM_PROMPT, dataJson)),
                Map.of("role", "user", "content", requirement));
    }

    /**
     * 从模型输出中提取并清洗 HTML:
     * 截取首个 doctype/html 到最后一个 </html>; 剥离外链脚本标签。
     */
    private String extractHtml(String raw) {
        String text = raw;
        int start = indexOfIgnoreCase(text, "<!doctype");
        if (start < 0) {
            start = indexOfIgnoreCase(text, "<html");
        }
        if (start < 0) {
            throw new BusinessException(502, "生成结果中没有有效的 HTML 文档,请换个描述重试");
        }
        int end = text.lastIndexOf("</html>");
        String html = end > start ? text.substring(start, end + 7) : text.substring(start);
        html = html
                // 普通外链脚本: <script src=...>...</script>
                .replaceAll("(?is)<script\\b[^>]*\\bsrc\\s*=\\s*(['\"])[^'\"]*\\1[^>]*>.*?</script>", "")
                // 自闭合外链脚本: <script src=... />
                .replaceAll("(?is)<script\\b[^>]*\\bsrc\\s*=\\s*(['\"])[^'\"]*\\1[^>]*/>", "")
                .trim();
        if (html.length() > MAX_HTML_LENGTH) {
            throw new BusinessException(502, "生成内容超出长度限制,请简化需求后重试");
        }
        return html;
    }

    /** 名称解析: 用户填了用用户的, 否则取需求前缀 */
    private String resolveName(AiTemplateGenerateRequest request) {
        if (request.getName() != null && !request.getName().isBlank()) {
            return request.getName().trim();
        }
        String req = request.getRequirement().trim();
        return req.length() <= 12 ? req : "AI 定制 · " + req.substring(0, 12);
    }

    /** 每日生成配额(按当日成功落库数统计) */
    private void checkDailyLimit(Long userId) {
        Long count = aiTemplateMapper.selectCount(new LambdaQueryWrapper<AiTemplate>()
                .eq(AiTemplate::getUserId, userId)
                .ge(AiTemplate::getCreateTime, LocalDate.now().atStartOfDay()));
        if (count != null && count >= aiProperties.getTemplateDailyLimit()) {
            throw new BusinessException(429, "今日 AI 模板生成次数已用完,明天再来试试");
        }
    }

    /** 按归属加载模板, 不存在或非本人一律 404 */
    private AiTemplate getOwned(Long id) {
        Long userId = requireUserId();
        AiTemplate template = aiTemplateMapper.selectById(id);
        if (template == null || !template.getUserId().equals(userId)) {
            throw new BusinessException(404, "模板不存在");
        }
        return template;
    }

    /** 发送 SSE 事件(客户端已断开时静默忽略) */
    private void emit(SseEmitter emitter, String name, Object payload) {
        try {
            emitter.send(SseEmitter.event().name(name).data(objectMapper.writeValueAsString(payload)));
        } catch (IOException | IllegalStateException e) {
            log.debug("SSE 发送失败(客户端可能已断开): {}", e.getMessage());
        }
    }

    private Long requireUserId() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已失效");
        }
        return userId;
    }

    private int indexOfIgnoreCase(String text, String target) {
        return text.toLowerCase().indexOf(target);
    }
}

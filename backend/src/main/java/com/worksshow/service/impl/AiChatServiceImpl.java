package com.worksshow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worksshow.client.AiChatClient;
import com.worksshow.config.AiProperties;
import com.worksshow.dto.AiChatRequest;
import com.worksshow.dto.AiMessageVO;
import com.worksshow.dto.PortfolioDataDTO;
import com.worksshow.entity.AiMessage;
import com.worksshow.entity.Portfolio;
import com.worksshow.exception.BusinessException;
import com.worksshow.mapper.AiMessageMapper;
import com.worksshow.security.UserContext;
import com.worksshow.service.AiActionValidator;
import com.worksshow.service.AiChatService;
import com.worksshow.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * AI 对话服务实现
 * <p>
 * 职责:
 * <ul>
 *   <li>校验简历归属与每日配额</li>
 *   <li>组装 Prompt(系统提示词 + 当前简历数据 + 当前主题 + 最近对话)</li>
 *   <li>流式转发正文, 同时切分输出中的动作段</li>
 *   <li>动作经白名单校验后下发, 并在对话结束后落库</li>
 * </ul>
 * 模型输出约定: 先自然语言回复, 再输出标记 {@code [[ACTIONS]]}, 之后是 JSON 数组。
 *
 * @author WorksShow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    /** 回复正文与动作段的分隔标记 */
    private static final String ACTION_MARKER = "[[ACTIONS]]";

    /** 参与上下文的历史消息条数 */
    private static final int HISTORY_LIMIT = 10;

    /** SSE 超时(毫秒) */
    private static final long SSE_TIMEOUT_MS = 180_000L;

    /** 注入系统的简历数据最大长度(防止超长作品集撑爆上下文) */
    private static final int MAX_DATA_LENGTH = 12_000;

    /** 角色常量 */
    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";

    private static final String SYSTEM_PROMPT = """
            你是「作品集编辑器」内的 AI 助理。用户正在编辑一份作品集/简历，你可以帮他改写文案、补全内容、调整配色主题。

            【可以修改的字段】
            1. update_data —— 修改简历内容，patch 只包含需要变更的部分：
               - profile：对象，字段与下方“当前简历数据”中的 profile 保持一致
               - works：作品数组（改动多项时给出完整数组）
               - experiences：经历数组
               - skills：技能数组
            2. update_theme —— 修改主题，patch 只允许以下字段：
               - palette.primary / secondary / tertiary / bg / bgSoft / card / ink / inkSoft / border，取值必须是 #RRGGBB
               - fontFamily：sans | serif | rounded
               - radius：sharp | soft | round
               - density：compact | normal | loose

            【要求】
            - 用户没有明确要求修改时，actions 返回 []
            - 不要编造用户没提供的事实；补写文案要与现有经历一致，不要虚构公司、时间、数据
            - 回复使用简体中文，简洁直接，不要复述本段格式说明

            【当前简历数据(JSON)】
            %s

            【当前主题(JSON)】
            %s

            【输出格式】严格按以下顺序，不要输出多余内容：
            自然语言回复
            [[ACTIONS]]
            JSON 数组（无修改时输出 []）
            """;

    private final AiProperties aiProperties;
    private final AiChatClient aiChatClient;
    private final AiActionValidator actionValidator;
    private final PortfolioService portfolioService;
    private final AiMessageMapper aiMessageMapper;
    private final ObjectMapper objectMapper;
    private final TaskExecutor taskExecutor;

    @Override
    public SseEmitter chat(AiChatRequest request) {
        Long userId = requireUserId();
        // getDetail 内部会校验简历归属, 归属不符直接 404
        PortfolioDataDTO detail = portfolioService.getDetail(request.getPortfolioId());
        checkDailyLimit(userId);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        taskExecutor.execute(() -> runChat(emitter, userId, request, detail));
        return emitter;
    }

    @Override
    public List<AiMessageVO> history(Long portfolioId) {
        Long userId = requireUserId();
        checkOwnership(userId, portfolioId);

        List<AiMessage> messages = aiMessageMapper.selectList(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getUserId, userId)
                .eq(AiMessage::getPortfolioId, portfolioId)
                .orderByAsc(AiMessage::getCreateTime)
                .orderByAsc(AiMessage::getId));

        List<AiMessageVO> result = new ArrayList<>(messages.size());
        for (AiMessage message : messages) {
            AiMessageVO vo = new AiMessageVO();
            vo.setId(message.getId());
            vo.setRole(message.getRole());
            vo.setContent(message.getContent());
            vo.setActions(actionValidator.parseStored(message.getActionsJson()));
            vo.setCreateTime(message.getCreateTime());
            result.add(vo);
        }
        return result;
    }

    @Override
    public void clear(Long portfolioId) {
        Long userId = requireUserId();
        checkOwnership(userId, portfolioId);

        aiMessageMapper.delete(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getUserId, userId)
                .eq(AiMessage::getPortfolioId, portfolioId));
        log.info("清空 AI 对话: userId={}, portfolioId={}", userId, portfolioId);
    }

    /** 后台线程执行一轮对话: 流式转发 + 动作切分 + 落库 */
    private void runChat(SseEmitter emitter, Long userId, AiChatRequest request, PortfolioDataDTO detail) {
        ReplyStreamParser parser = new ReplyStreamParser();
        try {
            List<AiMessage> historyMessages = loadHistory(userId, request.getPortfolioId());
            saveMessage(userId, request.getPortfolioId(), ROLE_USER, request.getMessage(), null);

            List<Map<String, Object>> messages = buildMessages(detail, request.getTheme(), historyMessages, request.getMessage());

            Consumer<String> onText = text -> emit(emitter, "delta", Map.of("text", text));
            aiChatClient.stream(messages, chunk -> parser.feed(chunk, onText));

            String reply = parser.finish(onText);
            List<Map<String, Object>> actions = actionValidator.validate(parser.actions());

            emit(emitter, "actions", actions);
            saveMessage(userId, request.getPortfolioId(), ROLE_ASSISTANT, reply,
                    actions.isEmpty() ? null : objectMapper.writeValueAsString(actions));
            emit(emitter, "done", Map.of());
            emitter.complete();
        } catch (Exception e) {
            log.warn("AI 对话失败: userId={}, portfolioId={}, error={}", userId, request.getPortfolioId(), e.getMessage());
            String message = e instanceof BusinessException ? e.getMessage() : "AI 服务繁忙,请稍后重试";
            emit(emitter, "error", Map.of("message", message));
            emitter.complete();
        }
    }

    /** 组装 OpenAI 兼容的消息列表 */
    private List<Map<String, Object>> buildMessages(PortfolioDataDTO detail, Map<String, Object> theme,
                                                    List<AiMessage> historyMessages, String userMessage) throws Exception {
        String dataJson = abbreviate(objectMapper.writeValueAsString(detail), MAX_DATA_LENGTH);
        String themeJson = theme == null || theme.isEmpty() ? "{}" : objectMapper.writeValueAsString(theme);

        List<Map<String, Object>> messages = new ArrayList<>(historyMessages.size() + 2);
        messages.add(Map.of("role", "system", "content", String.format(SYSTEM_PROMPT, dataJson, themeJson)));
        for (AiMessage history : historyMessages) {
            if (history.getContent() == null || history.getContent().isBlank()) {
                continue;
            }
            messages.add(Map.of("role", history.getRole(), "content", history.getContent()));
        }
        messages.add(Map.of("role", "user", "content", userMessage));
        return messages;
    }

    /** 加载最近若干条历史消息(按时间正序, 用于对话上下文) */
    private List<AiMessage> loadHistory(Long userId, Long portfolioId) {
        List<AiMessage> latest = aiMessageMapper.selectList(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getUserId, userId)
                .eq(AiMessage::getPortfolioId, portfolioId)
                .orderByDesc(AiMessage::getId)
                .last("LIMIT " + HISTORY_LIMIT));
        Collections.reverse(latest);
        return latest;
    }

    /**
     * 轻量归属校验: 仅查 portfolio 主表。
     * <p>
     * history/clear 只需确认简历存在且属于当前用户,
     * 无需 getDetail 连带加载整份用户档案(4 张 user_* 表)。
     * getById 受 @TableLogic 影响, 逻辑删除的简历返回 null, 与 404 语义一致。
     */
    private void checkOwnership(Long userId, Long portfolioId) {
        Portfolio portfolio = portfolioService.getById(portfolioId);
        if (portfolio == null || !portfolio.getUserId().equals(userId)) {
            throw new BusinessException(404, "简历不存在");
        }
    }

    /** 校验当日配额(user 消息计数) */
    private void checkDailyLimit(Long userId) {
        Long todayCount = aiMessageMapper.selectCount(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getUserId, userId)
                .eq(AiMessage::getRole, ROLE_USER)
                .ge(AiMessage::getCreateTime, LocalDate.now().atStartOfDay()));
        if (todayCount != null && todayCount >= aiProperties.getDailyLimit()) {
            throw new BusinessException(429, "今日 AI 对话次数已用完,明天再来试试");
        }
    }

    /** 落库一条消息(create_time 由数据库默认值维护) */
    private void saveMessage(Long userId, Long portfolioId, String role, String content, String actionsJson) {
        AiMessage message = new AiMessage();
        message.setUserId(userId);
        message.setPortfolioId(portfolioId);
        message.setRole(role);
        message.setContent(content);
        message.setActionsJson(actionsJson);
        aiMessageMapper.insert(message);
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

    private String abbreviate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max);
    }

    /**
     * 回复流解析器
     * <p>
     * 目标: 在流式输出中实时吐出正文, 并在遇到 {@link #ACTION_MARKER} 后
     * 把后续内容作为动作 JSON 收集起来。为避免标记被拆成多个数据块,
     * 正文末尾会保留 (标记长度 - 1) 个字符暂不发送。
     */
    static final class ReplyStreamParser {

        private final StringBuilder reply = new StringBuilder();
        private final StringBuilder pending = new StringBuilder();
        private final StringBuilder actions = new StringBuilder();
        private boolean inActions = false;

        void feed(String chunk, Consumer<String> onText) {
            if (inActions) {
                actions.append(chunk);
                return;
            }
            pending.append(chunk);

            int index = pending.indexOf(ACTION_MARKER);
            if (index >= 0) {
                flushText(pending.substring(0, index), onText);
                pending.delete(0, index + ACTION_MARKER.length());
                inActions = true;
                actions.append(pending);
                pending.setLength(0);
                return;
            }

            // 保留可能是标记前缀的尾部字符, 其余立即发送
            int keep = ACTION_MARKER.length() - 1;
            int emitLength = pending.length() - keep;
            if (emitLength > 0) {
                flushText(pending.substring(0, emitLength), onText);
                pending.delete(0, emitLength);
            }
        }

        /** 输出结束后兜底: 未出现标记则剩余内容仍是正文 */
        String finish(Consumer<String> onText) {
            if (!inActions && !pending.isEmpty()) {
                flushText(pending.toString(), onText);
                pending.setLength(0);
            }
            return reply.toString();
        }

        String actions() {
            return actions.toString();
        }

        private void flushText(String text, Consumer<String> onText) {
            if (text.isEmpty()) {
                return;
            }
            reply.append(text);
            onText.accept(text);
        }
    }
}

package com.worksshow.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worksshow.config.AiProperties;
import com.worksshow.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * OpenAI 兼容的对话客户端(流式)
 * <p>
 * 对接 {@code POST {base-url}/chat/completions}(stream=true),
 * 逐块解析 SSE 的 {@code data:} 行,把增量正文回调给调用方。
 * 默认指向阿里云百炼 DashScope 的兼容模式,亦可用于 DeepSeek 等其他兼容服务。
 *
 * @author WorksShow
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiChatClient {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    private RestClient restClient;

    @PostConstruct
    void init() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds()));
        this.restClient = RestClient.builder()
                .baseUrl(aiProperties.getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    /**
     * 发起流式对话
     *
     * @param messages 完整消息列表(system + 历史 + 当前用户消息)
     * @param onDelta  增量正文回调
     * @throws BusinessException 未配置密钥或上游返回失败时抛出
     */
    public void stream(List<Map<String, Object>> messages, Consumer<String> onDelta) {
        stream(messages, aiProperties.getMaxTokens(), onDelta);
    }

    /**
     * 发起流式对话(自定义输出 token 上限)
     * <p>
     * AI 模板生成等长输出场景需要覆盖默认 max_tokens。
     *
     * @param messages   完整消息列表
     * @param maxTokens  单次回复最大 token 数
     * @param onDelta    增量正文回调
     */
    public void stream(List<Map<String, Object>> messages, int maxTokens, Consumer<String> onDelta) {
        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            throw new BusinessException(500, "AI 服务未配置密钥,请联系管理员");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", aiProperties.getModel());
        payload.put("messages", messages);
        payload.put("stream", true);
        payload.put("max_tokens", maxTokens);
        payload.put("temperature", aiProperties.getTemperature());

        restClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + aiProperties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .body(payload)
                .exchange((request, response) -> {
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        String body = readQuietly(response.getBody());
                        log.warn("AI 上游返回失败: status={}, body={}", response.getStatusCode().value(), abbreviate(body));
                        throw new BusinessException(502, "AI 服务暂不可用,请稍后重试");
                    }
                    readStream(response.getBody(), onDelta);
                    return null;
                });
    }

    /** 逐行读取上游 SSE,提取 choices[0].delta.content */
    private void readStream(java.io.InputStream body, Consumer<String> onDelta) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data:")) {
                    continue;
                }
                String data = line.substring(5).trim();
                if (data.isEmpty() || "[DONE]".equals(data)) {
                    continue;
                }
                JsonNode node;
                try {
                    node = objectMapper.readTree(data);
                } catch (Exception e) {
                    log.warn("跳过无法解析的流式数据块: {}", abbreviate(data));
                    continue;
                }
                // 上游可能返回错误对象(如额度不足),此处直接中断并抛出
                JsonNode error = node.path("error");
                if (!error.isMissingNode()) {
                    log.warn("AI 上游流式错误: {}", abbreviate(error.toString()));
                    throw new BusinessException(502, "AI 服务暂不可用,请稍后重试");
                }
                JsonNode content = node.path("choices").path(0).path("delta").path("content");
                if (content.isTextual() && !content.asText().isEmpty()) {
                    onDelta.accept(content.asText());
                }
            }
        } catch (IOException e) {
            log.warn("AI 流式响应读取中断: {}", e.getMessage());
            throw new BusinessException(502, "AI 响应中断,请稍后重试");
        }
    }

    private String readQuietly(java.io.InputStream body) {
        try {
            return new String(body.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    private String abbreviate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 300 ? text : text.substring(0, 300) + "...";
    }
}

package com.worksshow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * AI 动作校验器
 * <p>
 * 模型输出的 actions 属于不可信输入, 必须按白名单过滤后才能下发给前端:
 * <ul>
 *   <li>update_data: 只允许 profile / works / experiences / skills 四个顶层字段</li>
 *   <li>update_theme: 只允许主题白名单字段, 颜色必须是 #RRGGBB, 枚举必须是约定取值</li>
 * </ul>
 * 非法条目直接丢弃, 不报错(模型偶发跑偏不应中断整轮对话)。
 *
 * @author WorksShow
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiActionValidator {

    /** 动作类型 */
    private static final String TYPE_UPDATE_DATA = "update_data";
    private static final String TYPE_UPDATE_THEME = "update_theme";

    /** 主题调色板允许的字段 */
    private static final Set<String> PALETTE_KEYS =
            Set.of("primary", "secondary", "tertiary", "bg", "bgSoft", "card", "ink", "inkSoft", "border");

    /** 枚举字段与允许取值 */
    private static final Map<String, Set<String>> ENUM_FIELDS = Map.of(
            "fontFamily", Set.of("sans", "serif", "rounded"),
            "radius", Set.of("sharp", "soft", "round"),
            "density", Set.of("compact", "normal", "loose"));

    /** 简历内容允许变更的顶层字段 */
    private static final Set<String> DATA_KEYS = Set.of("profile", "works", "experiences", "skills");

    /** 颜色格式 */
    private static final Pattern HEX_COLOR = Pattern.compile("^#[0-9a-fA-F]{6}$");

    /** 单个动作 patch 序列化后的最大字节数(防止模型输出超大数据撑爆响应) */
    private static final int MAX_PATCH_LENGTH = 20_000;

    private final ObjectMapper objectMapper;

    /**
     * 校验模型输出的动作数组
     *
     * @param raw 模型输出的原始文本(可能带 ``` 代码块或多余说明)
     * @return 通过白名单校验的动作列表, 无有效动作时返回空列表
     */
    public List<Map<String, Object>> validate(String raw) {
        JsonNode array = extractArray(raw);
        List<Map<String, Object>> result = new ArrayList<>();
        if (array == null) {
            return result;
        }
        for (JsonNode node : array) {
            Map<String, Object> action = sanitize(node);
            if (action != null) {
                result.add(action);
            }
        }
        return result;
    }

    /** 解析库中已存的 actions_json(历史回显用, 解析失败按空列表处理) */
    public List<Map<String, Object>> parseStored(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        JsonNode array = extractArray(json);
        if (array == null) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (JsonNode node : array) {
            result.add(objectMapper.convertValue(node, Map.class));
        }
        return result;
    }

    /** 从模型输出中提取 JSON 数组(去掉代码块标记与前后说明文字) */
    private JsonNode extractArray(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String text = raw.trim();
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start < 0 || end <= start) {
            log.warn("AI 动作输出中没有 JSON 数组: {}", abbreviate(text));
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(text.substring(start, end + 1));
            return node.isArray() ? node : null;
        } catch (Exception e) {
            log.warn("AI 动作 JSON 解析失败: {}", abbreviate(text));
            return null;
        }
    }

    /** 单条动作校验: 未知类型或 patch 无有效字段时丢弃 */
    private Map<String, Object> sanitize(JsonNode node) {
        String type = node.path("type").asText("");
        JsonNode patch = node.path("patch");
        if (!patch.isObject()) {
            return null;
        }
        Map<String, Object> sanitizedPatch;
        switch (type) {
            case TYPE_UPDATE_DATA -> sanitizedPatch = sanitizeData(patch);
            case TYPE_UPDATE_THEME -> sanitizedPatch = sanitizeTheme(patch);
            default -> {
                log.warn("忽略未知动作类型: {}", type);
                return null;
            }
        }
        if (sanitizedPatch.isEmpty()) {
            return null;
        }
        Map<String, Object> action = new HashMap<>();
        action.put("type", type);
        action.put("patch", sanitizedPatch);
        return action;
    }

    /** 主题 patch 校验: 颜色走 hex 正则, 枚举走白名单 */
    private Map<String, Object> sanitizeTheme(JsonNode patch) {
        Map<String, Object> result = new HashMap<>();

        JsonNode paletteNode = patch.path("palette");
        if (paletteNode.isObject()) {
            Map<String, Object> palette = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = paletteNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String value = entry.getValue().asText("");
                if (PALETTE_KEYS.contains(entry.getKey()) && HEX_COLOR.matcher(value).matches()) {
                    palette.put(entry.getKey(), value);
                }
            }
            if (!palette.isEmpty()) {
                result.put("palette", palette);
            }
        }

        for (Map.Entry<String, Set<String>> entry : ENUM_FIELDS.entrySet()) {
            JsonNode valueNode = patch.path(entry.getKey());
            if (valueNode.isTextual() && entry.getValue().contains(valueNode.asText())) {
                result.put(entry.getKey(), valueNode.asText());
            }
        }
        return result;
    }

    /** 简历内容 patch 校验: 只保留约定的四个顶层字段, 并限制体积 */
    private Map<String, Object> sanitizeData(JsonNode patch) {
        Map<String, Object> result = new HashMap<>();
        for (String key : DATA_KEYS) {
            JsonNode valueNode = patch.path(key);
            if (valueNode.isMissingNode() || valueNode.isNull()) {
                continue;
            }
            boolean shapeValid = "profile".equals(key) ? valueNode.isObject() : valueNode.isArray();
            if (!shapeValid) {
                log.warn("忽略字段结构不正确的动作数据: {}", key);
                continue;
            }
            String serialized = valueNode.toString();
            if (serialized.length() > MAX_PATCH_LENGTH) {
                log.warn("忽略体积过大的动作数据: key={}, length={}", key, serialized.length());
                continue;
            }
            result.put(key, objectMapper.convertValue(valueNode, Object.class));
        }
        return result;
    }

    private String abbreviate(String text) {
        return text.length() <= 200 ? text : text.substring(0, 200) + "...";
    }
}

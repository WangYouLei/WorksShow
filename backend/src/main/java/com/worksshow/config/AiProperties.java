package com.worksshow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 对话相关配置
 * <p>
 * 对接任意 OpenAI 兼容的 chat/completions 接口(默认阿里云百炼 DashScope 兼容模式)。
 * 密钥仅从环境变量注入,不在源码中出现明文。
 *
 * @author WorksShow
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** 接口基础地址(含 /v1,不含 /chat/completions) */
    private String baseUrl;

    /** API Key(为空时调用直接返回友好错误) */
    private String apiKey;

    /** 模型名 */
    private String model = "qwen-plus";

    /** 单次回复最大 token 数 */
    private Integer maxTokens = 1500;

    /** 采样温度 */
    private Double temperature = 0.3;

    /** 每个用户每日对话次数上限 */
    private Integer dailyLimit = 30;

    /** 每个用户每日 AI 模板生成次数上限(生成成本远高于对话) */
    private Integer templateDailyLimit = 5;

    /** AI 模板生成的单次输出 token 上限(整页 HTML 需要更长输出) */
    private Integer templateMaxTokens = 8000;

    /** 读取超时(秒):流式输出期间为两次数据块之间的最大间隔 */
    private Integer timeoutSeconds = 120;
}

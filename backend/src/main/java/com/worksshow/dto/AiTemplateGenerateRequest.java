package com.worksshow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 模板生成请求
 *
 * @author WorksShow
 */
@Data
public class AiTemplateGenerateRequest {

    /** 设计需求描述(风格/版式/氛围等) */
    @NotBlank(message = "设计需求不能为空")
    @Size(max = 1000, message = "设计需求过长(最多 1000 字)")
    private String requirement;

    /** 模板名称(可选,为空时按需求前缀自动命名) */
    @Size(max = 50, message = "模板名称过长(最多 50 字)")
    private String name;
}

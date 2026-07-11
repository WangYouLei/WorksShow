package com.worksshow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 自定义域名新增/更新请求 DTO
 *
 * @author WorksShow
 */
@Data
public class CustomDomainRequestDTO {

    /** 域名(如 myresume.edgeone.app) */
    @NotBlank(message = "域名不能为空")
    @Size(max = 255, message = "域名长度不能超过255")
    private String domain;

    /** 域名备注名(展示用,可选) */
    @Size(max = 100, message = "备注名长度不能超过100")
    private String name;
}

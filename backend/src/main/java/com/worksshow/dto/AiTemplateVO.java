package com.worksshow.dto;

import com.worksshow.entity.AiTemplate;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 生成模板 VO
 * <p>
 * 列表接口不含 html(产物可达上百 KB,按需经详情接口获取)。
 *
 * @author WorksShow
 */
@Data
public class AiTemplateVO {

    /** 模板ID */
    private Long id;

    /** 模板名称 */
    private String name;

    /** 用户的设计需求描述 */
    private String requirement;

    /** 生成的完整 HTML(仅详情接口返回) */
    private String html;

    /** 懒创建的简历实例ID(未部署过为 null) */
    private Long portfolioId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 转换 VO(不含 html) */
    public static AiTemplateVO summary(AiTemplate entity) {
        AiTemplateVO vo = new AiTemplateVO();
        vo.id = entity.getId();
        vo.name = entity.getName();
        vo.requirement = entity.getRequirement();
        vo.portfolioId = entity.getPortfolioId();
        vo.createTime = entity.getCreateTime();
        return vo;
    }

    /** 转换 VO(含 html) */
    public static AiTemplateVO detail(AiTemplate entity) {
        AiTemplateVO vo = summary(entity);
        vo.html = entity.getHtml();
        return vo;
    }
}

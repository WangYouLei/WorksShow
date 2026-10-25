package com.worksshow.dto;

import com.worksshow.entity.Portfolio;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历实例 VO(返回前端用)
 * <p>
 * 排除 userId 与 deleted 等内部字段。
 *
 * @author WorksShow
 */
@Data
public class PortfolioVO {

    /** 主键ID */
    private Long id;

    /** 模板ID(aurora/mo-yun/pastel) */
    private String templateId;

    /** 简历名称(用户自定义) */
    private String name;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 从 Entity 转换 */
    public static PortfolioVO from(Portfolio portfolio) {
        if (portfolio == null) {
            return null;
        }
        PortfolioVO vo = new PortfolioVO();
        vo.setId(portfolio.getId());
        vo.setTemplateId(portfolio.getTemplateId());
        vo.setName(portfolio.getName());
        vo.setCreateTime(portfolio.getCreateTime());
        vo.setUpdateTime(portfolio.getUpdateTime());
        return vo;
    }
}

package com.worksshow.dto;

import com.worksshow.entity.CustomDomain;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 自定义域名 VO(返回前端用)
 * <p>
 * 排除 userId 与 deleted 等内部字段。
 *
 * @author WorksShow
 */
@Data
public class CustomDomainVO {

    /** 主键ID */
    private Long id;

    /** 域名(如 myresume.edgeone.app) */
    private String domain;

    /** 域名备注名(展示用) */
    private String name;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 从 Entity 转换 */
    public static CustomDomainVO from(CustomDomain domain) {
        if (domain == null) {
            return null;
        }
        CustomDomainVO vo = new CustomDomainVO();
        vo.setId(domain.getId());
        vo.setDomain(domain.getDomain());
        vo.setName(domain.getName());
        vo.setCreateTime(domain.getCreateTime());
        vo.setUpdateTime(domain.getUpdateTime());
        return vo;
    }
}

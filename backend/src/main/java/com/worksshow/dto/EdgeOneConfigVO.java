package com.worksshow.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * EdgeOne 配置响应 VO(脱敏)
 * <p>
 * api_token 永不返回明文或密文;仅返回脱敏后的展示串
 * (首尾各保留若干字符),便于用户确认已配置过。
 *
 * @author WorksShow
 */
@Data
public class EdgeOneConfigVO {

    /** 配置记录ID */
    private Long id;

    /** 脱敏后的 API Token(如 c+KH****5678) */
    private String apiTokenMasked;

    /** EdgeOne Pages 项目名(CLI -n参数用) */
    private String projectName;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

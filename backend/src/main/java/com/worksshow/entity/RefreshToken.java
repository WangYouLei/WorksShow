package com.worksshow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Refresh Token 实体 - 对应数据库 refresh_token 表
 * <p>
 * 用于 access token 过期后刷新获取新 token。
 * - refresh_token: UUID 随机字符串,唯一
 * - user_id: 关联用户
 * - expires_at: 过期时间(7天),用于判断是否有效
 * - 使用逻辑删除,便于审计和清理过期记录
 *
 * @author WorksShow
 */
@Data
@TableName("refresh_token")
public class RefreshToken {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Refresh Token 值(UUID) */
    private String refreshToken;

    /** 关联用户ID */
    private Long userId;

    /** 过期时间 */
    private LocalDateTime expiresAt;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除标志:0-未删除 1-已删除 */
    @TableLogic
    private Integer deleted;
}

-- ============================================================
-- Refresh Token 表 refresh_token
-- ------------------------------------------------------------
-- 说明: 存储 refresh token,用于 access token 过期后刷新获取新 token。
--       access token 有效期缩短至 15 分钟,降低泄漏窗口。
-- ============================================================
USE `works_show`;

CREATE TABLE IF NOT EXISTS `refresh_token` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT              COMMENT '主键ID',
    `refresh_token` VARCHAR(64)  NOT NULL                            COMMENT 'Refresh Token值(UUID)',
    `user_id`       BIGINT       NOT NULL                            COMMENT '关联用户ID(关联 user.id)',
    `expires_at`    DATETIME     NOT NULL                            COMMENT '过期时间(7天)',
    `create_time`   DATETIME              DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time`   DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT               DEFAULT 0                  COMMENT '逻辑删除:0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refresh_token` (`refresh_token`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Refresh Token表(用于access token过期刷新)';

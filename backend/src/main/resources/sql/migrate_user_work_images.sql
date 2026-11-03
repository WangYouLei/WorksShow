-- ============================================================
-- 一次性迁移: user_work 表新增图集字段 images
-- ------------------------------------------------------------
-- 说明: 支持作品多图(OSS 直传 URL 数组,详情弹窗以图集形式浏览)。
--       schema.sql 的 CREATE TABLE 已含此列,此文件仅供已建库手动执行。
--       执行: mysql -uroot -p works_show < migrate_user_work_images.sql
--       (列已存在会报 Duplicate column,忽略即可)
-- ============================================================
USE `works_show`;

ALTER TABLE `user_work`
    ADD COLUMN `images` JSON DEFAULT NULL COMMENT '作品图集URL数组 ["https://...","https://..."](详情弹窗图集浏览,可多张)' AFTER `video_url`;

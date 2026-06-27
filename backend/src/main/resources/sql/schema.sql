-- ============================================================
-- WorksShow 数据库初始化脚本
-- 数据库: works_show
-- 说明: 用户表 + 用户简历档案(user_profile/user_work/user_experience/user_skill)
--       + 简历模板实例(portfolio) + 求职意向表
-- 字符集: utf8mb4 (支持 emoji 等四字节字符)
-- 注意: 使用 CREATE TABLE IF NOT EXISTS,可安全重复执行,
--       不会清空已有数据(切勿手动 DROP)。
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `works_show`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `works_show`;

-- ============================================================
-- 用户表 user
-- ------------------------------------------------------------
-- 说明: 存储系统用户信息,用于登录注册
-- - 登录账号为手机号(phone)或邮箱(email),不再使用 username
-- - nickname 作为展示名,注册时由用户填写
-- - password 字段存储 BCrypt 加密后的密文,非明文
-- - deleted 字段配合 Mybatis-plus 逻辑删除
-- - create_time / update_time 由数据库自动维护
-- - 唯一索引包含 deleted 字段: 解决"逻辑删除后邮箱/手机号无法
--   重新注册"问题。
-- ============================================================
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT              COMMENT '主键ID',
    `password`    VARCHAR(100) NOT NULL                            COMMENT '密码(BCrypt加密存储)',
    `email`       VARCHAR(100) NOT NULL                            COMMENT '邮箱(登录账号之一)',
    `phone`       VARCHAR(20)  NOT NULL                            COMMENT '手机号(登录账号之一)',
    `nickname`    VARCHAR(50)  NOT NULL                            COMMENT '昵称(展示名,用户自定义)',
    `avatar`      VARCHAR(255)          DEFAULT NULL               COMMENT '头像URL',
    `gender`      TINYINT               DEFAULT 0                  COMMENT '性别:0-未知 1-男 2-女',
    `status`      TINYINT               DEFAULT 1                  COMMENT '账号状态:0-禁用 1-启用',
    `create_time` DATETIME              DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time` DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT               DEFAULT 0                  COMMENT '逻辑删除:0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    -- 复合唯一索引: 包含 deleted,使逻辑删除后邮箱/手机号可重新注册
    UNIQUE KEY `uk_email` (`email`, `deleted`),
    UNIQUE KEY `uk_phone` (`phone`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ============================================================
-- 数据库迁移: 移除 username 字段 + 手机号字段升级(已存在 user 表时执行)
-- ------------------------------------------------------------
-- 说明:
--   1. 登录账号改为手机号/邮箱,移除 username 字段及其唯一索引
--   2. 手机号从可选改为必填,并添加唯一索引
--   3. nickname 从可选改为必填
-- 若表中已有 NULL 手机号/nickname 数据,需先补充再执行。
-- ============================================================
ALTER TABLE `user` DROP INDEX `uk_username`;
ALTER TABLE `user` DROP COLUMN `username`;
ALTER TABLE `user` MODIFY COLUMN `phone` VARCHAR(20) NOT NULL COMMENT '手机号(登录账号之一)';
ALTER TABLE `user` MODIFY COLUMN `nickname` VARCHAR(50) NOT NULL COMMENT '昵称(展示名,用户自定义)';
-- 若 uk_phone 索引已存在会报错,可先执行: DROP INDEX `uk_phone` ON `user`;
ALTER TABLE `user` ADD UNIQUE KEY `uk_phone` (`phone`, `deleted`);

-- ============================================================
-- 说明: 此处不预置测试用户(避免 BCrypt 密钥哈希不一致问题)
-- 启动后端后,通过注册接口创建账号:
--   POST http://localhost:8080/api/user/register
--   Body: { "nickname": "管理员", "password": "123456", "email": "admin@example.com", "phone": "13800138000", "code": "123456" }
-- ============================================================


-- ============================================================
-- 用户简历档案表 user_profile(个人信息,与 user 1:1)
-- ------------------------------------------------------------
-- 说明: 存储用户级个人信息(姓名/职位/简介/邮箱/社交/统计),
--       所有简历模板共享此档案,无需在每份简历重复填写。
--       采用懒创建:用户首次保存档案时才 INSERT,注册流程
--       不依赖此表。p_email 为简历展示用邮箱,可与登录邮箱
--       (user.email)不同。嵌套数组(stats/socials)用 JSON 列存储。
-- ============================================================
CREATE TABLE IF NOT EXISTS `user_profile` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT              COMMENT '主键ID',
    `user_id`         BIGINT       NOT NULL                            COMMENT '所属用户ID(关联 user.id)',
    `p_name`          VARCHAR(50)           DEFAULT NULL               COMMENT '中文姓名',
    `p_name_en`       VARCHAR(100)          DEFAULT NULL               COMMENT '英文名',
    `p_title`         VARCHAR(200)          DEFAULT NULL               COMMENT '职位/方向(如"独立开发者")',
    `p_tagline`       VARCHAR(500)          DEFAULT NULL               COMMENT '一句话标语/Hero副标题',
    `p_bio`           TEXT                  DEFAULT NULL               COMMENT '个人简介(关于我正文)',
    `p_email`         VARCHAR(100)          DEFAULT NULL               COMMENT '展示用邮箱(可与登录邮箱不同)',
    `p_location`      VARCHAR(100)          DEFAULT NULL               COMMENT '所在地',
    `p_availability`  VARCHAR(200)          DEFAULT NULL               COMMENT '状态(如"通常24小时内回复")',
    `p_stats`         JSON                  DEFAULT NULL               COMMENT '统计项数组 [{label,value}]',
    `p_socials`       JSON                  DEFAULT NULL               COMMENT '社交链接数组 [{name,url,icon}]',
    `create_time`     DATETIME              DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time`     DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT               DEFAULT 0                  COMMENT '逻辑删除:0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    -- 一个用户一份档案(含 deleted 解决逻辑删除后重建)
    UNIQUE KEY `uk_user` (`user_id`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户简历档案表(所有模板共享的个人信息)';

-- ============================================================
-- 简历信息表
-- ------------------------------------------------------------
-- 说明: 一个用户可拥有多份简历,每份绑定一个模板(对应前端
--       /editor/:templateId)。简历内容(个人信息/作品/经历/技能)
--       统一存于 user_* 档案表,所有模板共享,无需重复填写;
--       portfolio 仅作为"模板实例"记录用户启用了哪个模板。
--       嵌套数组(stats/socials/tags/items)用 JSON 列存储。
-- 环境: MySQL 5.7+ (使用 JSON 类型)
-- ============================================================

-- ------------------------------------------------------------
-- 1. 简历主表 portfolio(模板实例,不含内容)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `portfolio` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT              COMMENT '主键ID',
    `user_id`         BIGINT       NOT NULL                            COMMENT '所属用户ID(关联 user.id)',
    `template_id`     VARCHAR(50)  NOT NULL                            COMMENT '模板ID(aurora/mo-yun/pastel)',
    `name`            VARCHAR(100) NOT NULL                            COMMENT '简历名称(用户自定义)',
    `create_time`     DATETIME              DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time`     DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT               DEFAULT 0                  COMMENT '逻辑删除:0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_user_template` (`user_id`, `template_id`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '简历主表(模板实例,内容存于user_*档案表)';

-- ------------------------------------------------------------
-- 2. 用户作品表 user_work(所有简历共享)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_work` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT              COMMENT '主键ID',
    `user_id`         BIGINT       NOT NULL                            COMMENT '所属用户ID(关联 user.id)',
    `title`           VARCHAR(200) NOT NULL                            COMMENT '作品标题',
    `category`        VARCHAR(100)          DEFAULT NULL               COMMENT '分类(如"AI产品")',
    `cover`           VARCHAR(255)          DEFAULT NULL               COMMENT '封面图URL',
    `description`     TEXT                  DEFAULT NULL               COMMENT '作品描述',
    `year`            VARCHAR(20)           DEFAULT NULL               COMMENT '年份',
    `link`            VARCHAR(500)          DEFAULT NULL               COMMENT '外链URL',
    `tags`            JSON                  DEFAULT NULL               COMMENT '技术标签数组 ["Vue","TypeScript"]',
    `highlight`       TINYINT               DEFAULT 0                  COMMENT '是否高亮旗舰:0-否 1-是',
    `sort_order`      INT                   DEFAULT 0                  COMMENT '排序序号(越小越靠前)',
    `create_time`     DATETIME              DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time`     DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT               DEFAULT 0                  COMMENT '逻辑删除:0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户作品表(所有简历共享)';

-- ------------------------------------------------------------
-- 3. 用户经历表 user_experience(工作+教育统一存储,所有简历共享)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_experience` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT              COMMENT '主键ID',
    `user_id`         BIGINT       NOT NULL                            COMMENT '所属用户ID(关联 user.id)',
    `period`          VARCHAR(100)          DEFAULT NULL               COMMENT '时间段(如"2024"或"2022-2024")',
    `title`           VARCHAR(200)          DEFAULT NULL               COMMENT '职位/学位',
    `organization`    VARCHAR(200)          DEFAULT NULL               COMMENT '机构/学校',
    `description`     TEXT                  DEFAULT NULL               COMMENT '经历描述',
    `type`            VARCHAR(20)           DEFAULT 'work'             COMMENT '类型:work-工作经历 education-教育经历',
    `sort_order`      INT                   DEFAULT 0                  COMMENT '排序序号',
    `create_time`     DATETIME              DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time`     DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT               DEFAULT 0                  COMMENT '逻辑删除:0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户经历表(工作与教育经历,所有简历共享)';

-- ------------------------------------------------------------
-- 4. 用户技能表 user_skill(按分类存储,所有简历共享)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_skill` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT              COMMENT '主键ID',
    `user_id`         BIGINT       NOT NULL                            COMMENT '所属用户ID(关联 user.id)',
    `category`        VARCHAR(100)          DEFAULT NULL               COMMENT '分类中文名(如"前端开发")',
    `category_en`     VARCHAR(100)          DEFAULT NULL               COMMENT '分类英文名(如"Frontend")',
    `items`           JSON                  DEFAULT NULL               COMMENT '技能项数组 ["Vue","React","TypeScript"]',
    `sort_order`      INT                   DEFAULT 0                  COMMENT '排序序号',
    `create_time`     DATETIME              DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time`     DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT               DEFAULT 0                  COMMENT '逻辑删除:0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户技能表(所有简历共享)';


-- ============================================================
-- 求职意向表 career_intention
-- ------------------------------------------------------------
-- 说明: 一个用户一份求职意向(与 user 1:1),通过唯一索引
--       (user_id, deleted) 保证,记录用户期望的职位/行业/
--       薪资/城市/到岗时间等求职意向信息。
-- ============================================================
CREATE TABLE IF NOT EXISTS `career_intention` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT              COMMENT '主键ID',
    `user_id`           BIGINT       NOT NULL                            COMMENT '所属用户ID(关联 user.id)',
    `job_status`        TINYINT      DEFAULT 0                          COMMENT '求职状态:0-离职正在找 1-在职考虑机会 2-在职暂不考虑',
    `expected_position` VARCHAR(100)          DEFAULT NULL               COMMENT '期望职位(如"前端开发工程师")',
    `position_type`     VARCHAR(50)           DEFAULT NULL               COMMENT '职位类别(技术/产品/设计/运营等)',
    `expected_industry` JSON                  DEFAULT NULL               COMMENT '期望行业数组 ["互联网","金融"]',
    `expected_city`     JSON                  DEFAULT NULL               COMMENT '期望城市数组 ["北京","上海"]',
    `work_type`         TINYINT      DEFAULT 0                          COMMENT '工作性质:0-全职 1-兼职 2-实习 3-远程',
    `salary_min`        INT                   DEFAULT NULL               COMMENT '期望最低薪资(千元,如15表示15k)',
    `salary_max`        INT                   DEFAULT NULL               COMMENT '期望最高薪资(千元,如25表示25k)',
    `arrival_time`      VARCHAR(50)           DEFAULT NULL               COMMENT '到岗时间(如"随时""一周内""一个月内")',
    `self_evaluation`   TEXT                  DEFAULT NULL               COMMENT '自我评价/求职宣言(可选)',
    `create_time`       DATETIME              DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `update_time`       DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT               DEFAULT 0                  COMMENT '逻辑删除:0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    -- 一个用户一份求职意向(含 deleted 解决逻辑删除后重建)
    UNIQUE KEY `uk_user` (`user_id`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '求职意向表(一个用户一份)';

-- ============================================================
-- 数据库迁移: 简历结构重构(仅旧库执行,一次性迁移)
-- ------------------------------------------------------------
-- 说明:
--   本次重构将简历内容从 portfolio 子表提升为 user 级档案,
--   所有模板共享同一份内容。
--   - 全新库:此块已用 /* */ 注释,无需执行,直接跳过
--   - 旧库:执行前请备份,取消下方 /* */ 注释后执行
--   迁移步骤:
--   1) 旧 portfolio 中的 p_* 个人信息迁入 user_profile(每用户一份)
--   2) 旧 portfolio_work/experience/skill 迁入 user_*(按 user_id)
--   3) 瘦身 portfolio:删除所有 p_* 字段
--   4) 删除旧 portfolio_work/experience/skill 三张子表
-- 注意:
--   - 同一用户多份简历的内容会合并到 user_id 下,如有差异需人工去重
--   - 各 ALTER/INSERT 若对应旧字段/旧表不存在会报错,按实际情况注释跳过
--   - p_avatar 字段在早期版本存在,如旧库仍有则一并删除
-- ============================================================

/* 旧库迁移语句(全新库默认整块注释,旧库取消此块注释后执行;新库执行会因旧表/旧字段不存在而报错)

-- 1) 个人信息:portfolio -> user_profile(每用户取最新 update_time 的一份)
INSERT INTO `user_profile` (user_id, p_name, p_name_en, p_title, p_tagline, p_bio, p_email, p_location, p_availability, p_stats, p_socials, create_time, update_time)
SELECT p.user_id, p.p_name, p.p_name_en, p.p_title, p.p_tagline, p.p_bio, p.p_email, p.p_location, p.p_availability, p.p_stats, p.p_socials, p.create_time, p.update_time
FROM `portfolio` p
INNER JOIN (
    SELECT user_id, MAX(update_time) AS max_ts
    FROM `portfolio` WHERE deleted = 0
    GROUP BY user_id
) t ON t.user_id = p.user_id AND t.max_ts = p.update_time
WHERE p.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM `user_profile` up WHERE up.user_id = p.user_id AND up.deleted = 0);

-- 2) 作品:portfolio_work -> user_work
INSERT INTO `user_work` (user_id, title, category, cover, description, year, link, tags, highlight, sort_order, create_time, update_time, deleted)
SELECT p.user_id, w.title, w.category, w.cover, w.description, w.year, w.link, w.tags, w.highlight, w.sort_order, w.create_time, w.update_time, w.deleted
FROM `portfolio_work` w
JOIN `portfolio` p ON p.id = w.portfolio_id;

-- 3) 经历:portfolio_experience -> user_experience
INSERT INTO `user_experience` (user_id, period, title, organization, description, type, sort_order, create_time, update_time, deleted)
SELECT p.user_id, e.period, e.title, e.organization, e.description, e.type, e.sort_order, e.create_time, e.update_time, e.deleted
FROM `portfolio_experience` e
JOIN `portfolio` p ON p.id = e.portfolio_id;

-- 4) 技能:portfolio_skill -> user_skill
INSERT INTO `user_skill` (user_id, category, category_en, items, sort_order, create_time, update_time, deleted)
SELECT p.user_id, s.category, s.category_en, s.items, s.sort_order, s.create_time, s.update_time, s.deleted
FROM `portfolio_skill` s
JOIN `portfolio` p ON p.id = s.portfolio_id;

-- 5) 瘦身 portfolio:删除所有 p_* 字段(若字段不存在会报错,按需注释跳过)
ALTER TABLE `portfolio` DROP COLUMN `p_name`;
ALTER TABLE `portfolio` DROP COLUMN `p_name_en`;
ALTER TABLE `portfolio` DROP COLUMN `p_title`;
ALTER TABLE `portfolio` DROP COLUMN `p_tagline`;
ALTER TABLE `portfolio` DROP COLUMN `p_bio`;
ALTER TABLE `portfolio` DROP COLUMN `p_email`;
ALTER TABLE `portfolio` DROP COLUMN `p_location`;
ALTER TABLE `portfolio` DROP COLUMN `p_availability`;
ALTER TABLE `portfolio` DROP COLUMN `p_stats`;
ALTER TABLE `portfolio` DROP COLUMN `p_socials`;
-- 早期版本残留字段,如旧库仍有则取消注释执行
-- ALTER TABLE `portfolio` DROP COLUMN `p_avatar`;

-- 6) 删除旧子表(数据已迁入 user_*,确认无误后执行)
DROP TABLE IF EXISTS `portfolio_work`;
DROP TABLE IF EXISTS `portfolio_experience`;
DROP TABLE IF EXISTS `portfolio_skill`;

*/ -- 迁移块结束

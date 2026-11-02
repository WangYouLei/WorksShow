# WorksShow · 可视化作品集编辑器

> 填一次内容，多模板切换，一键导出与部署 —— 打造属于你的个人品牌站点。

🇬🇧 **[English README](README.md)**

<p align="center">
  <img alt="Vue" src="https://img.shields.io/badge/Vue-3.4-42b883?logo=vue.js&logoColor=white">
  <img alt="TypeScript" src="https://img.shields.io/badge/TypeScript-5.3-3178c6?logo=typescript&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.2.5-6db33f?logo=springboot&logoColor=white">
  <img alt="MySQL" src="https://img.shields.io/badge/MySQL-8.0-4479a1?logo=mysql&logoColor=white">
  <img alt="License" src="https://img.shields.io/badge/License-MIT-yellow">
</p>

***

## 📖 这是什么

**WorksShow** 是一个面向开发者与设计师的**个人作品集/简历网站生成器**。

你不需要写一行代码：在可视化编辑器里填好个人信息、作品、经历和技能，然后从 **10 套风格迥异的模板**中挑一套喜欢的，就能得到一个可以随时导出、随时上线的个人网站。

- 想换风格？切换模板即可，**内容无需重填**
- 想保存到本地？**一键导出**为一个独立的 HTML 文件，双击就能打开
- 想让全世界看到？**一键部署**到腾讯云 EdgeOne Pages，秒获公开访问链接
- 不知道怎么写简历文案？内置 **AI 助手**帮你润色，还能用一句话生成专属模板

***

## ✨ 核心功能

| 功能                 | 说明                                   |
| ------------------ | ------------------------------------ |
| 🎨 **10 套风格模板**    | 从东方水墨到 3D 粒子星球，总有一款适合你（见下方模板一览）      |
| ✏️ **可视化编辑**       | 左侧表单填写、右侧实时预览，所见即所得，编辑自动保存           |
| 📦 **单文件 HTML 导出** | 导出一个零依赖的 HTML 文件，可部署到任意静态托管平台        |
| 🚀 **一键部署上线**      | 集成腾讯云 EdgeOne Pages，填好配置点一下，即得公开访问链接 |
| 🤖 **AI 助手**       | 对话式润色简历文案；用一句自然语言生成专属模板              |
| 🖼️ **图片/视频上传**    | 作品支持封面图与视频展示，上传即用                    |
| 🔒 **完整账号体系**      | 邮箱验证码注册、手机号/邮箱登录、密码找回                |

***

## 🖼️ 模板一览

| 模板    | 风格                           |
| ----- | ---------------------------- |
| 墨韵    | 东方水墨 × 杂志风                   |
| 粉彩    | 柔和粉彩 × 轻盈现代                  |
| 极光    | 深空 × 玻璃拟态 × Bento Grid       |
| 晨曦    | 暖白编辑风 × 赭石橙                  |
| 野性    | 粗野主义 × 硬边框 × 原色块             |
| 报章    | 古典报刊 × 印刷美学 × 多栏排版           |
| 琉璃 3D | 粉彩渐变天空 × 玻璃拟态 × 真 3D 悬浮场景    |
| 立方档案  | 可交互 3D 信息立方，六个面承载不同内容        |
| 粒子星球  | 数千发光粒子组成的 3D 星球，点击触发变形叙事     |
| 病毒侵染  | 电影级 3D 病毒，整份简历按「感染生命周期」六阶段展开 |

> 后 4 套 3D 模板导出后同样 100% 离线可用，无任何外部依赖。

***

## 🚀 快速开始

### 环境要求

- Node.js ≥ 18、Java 17、Maven 3.8+
- MySQL 8.0+、Redis 6.0+
- 一个 SMTP 邮箱（用于发送验证码）

### 启动步骤

```bash
# 1. 克隆仓库
git clone https://github.com/WangYouLei/WorksShow.git
cd WorksShow

# 2. 初始化数据库
mysql -u root -p < backend/src/main/resources/sql/schema.sql
mysql -u root -p < backend/src/main/resources/sql/refresh_token.sql

# 3. 启动后端（数据库、JWT、邮箱等配置见技术文档「环境变量清单」）
cd backend
mvn spring-boot:run

# 4. 启动前端（新开终端）
cd frontend
npm install
npm run dev
```

打开 `http://localhost:5173` → 注册账号 → 选择模板 → 开始编辑 → 导出或部署。

***

## 📚 更多文档

- 📄 **[技术文档](docs/TECHNICAL.md)** —— 技术栈、系统架构、关键技术实现、数据库设计、API 接口、环境变量清单与部署细节，面向开发者与代码评审

***

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 开源。

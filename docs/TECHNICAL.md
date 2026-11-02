# WorksShow 技术文档

<div align="center">

[ 🇨🇳 中文 ](./TECHNICAL.md) · [ 🇬🇧 English ](./TECHNICAL.en.md)

</div>

> 本文档面向开发者与代码评审，涵盖技术栈、系统架构、关键技术实现、数据库设计与 API 概览。
> 项目功能介绍请参阅 [README.md](../README.md) | [中文 README](../README.zh-CN.md)

***

## 🛠️ 技术栈

### 前端

| 技术              | 版本    | 用途                                 |
| --------------- | ----- | ---------------------------------- |
| Vue 3           | 3.4   | Composition API + `<script setup>` |
| TypeScript      | 5.3   | 全量类型覆盖（`vue-tsc` 构建期校验）            |
| Vite            | 5.0   | 开发服务器 + 生产构建                       |
| Vue Router      | 4.6   | Hash 模式路由 + 全局守卫                   |
| Axios           | 1.18  | 请求/响应拦截器统一封装                       |
| Lucide Vue Next | 0.511 | 图标库                                |

### 后端

| 技术                     | 版本     | 用途                                             |
| ---------------------- | ------ | ---------------------------------------------- |
| Spring Boot            | 3.2.5  | Web + Validation + Mail                        |
| Java                   | 17     | record、var、switch 表达式等现代特性                     |
| MyBatis-Plus           | 3.5.7  | ORM + 逻辑删除 + Lambda 查询                         |
| MySQL                  | 8.0+   | 主数据存储（utf8mb4，JSON 列）                          |
| Redis                  | 6.0+   | token 黑名单、邮箱验证码、AI/OSS 每日限额，支持多实例部署           |
| Sa-Token               | 1.38.0 | 鉴权框架（JWT StateLess 无状态模式）                     |
| spring-security-crypto | -      | 仅引入 `BCryptPasswordEncoder`，不启用完整 Security 过滤链 |
| aliyun-sdk-oss         | 3.17.4 | OSS 预签名直传（Java 17 需额外引入 jaxb 相关依赖）            |
| Lombok                 | -      | 减少实体样板代码                                       |

***

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                         浏览器                              │
│  Vue 3 SPA (Vite dev / 静态部署)                            │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────┐    │
│  │ 模板画廊    │  │ 编辑器     │  │ 登录/注册/账号     │    │
│  │ Gallery    │  │ 左表单+右预览│  │ token 存 localStorage │    │
│  └────────────┘  └────────────┘  └────────────────────┘    │
└──────────────────────────┬──────────────────────────────────┘
                           │  Axios  baseURL=/api
                           │  请求拦截器: Bearer token
                           │  响应拦截器: 解包 Result / 401 跳登录
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot 3  (context-path: /api)           │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Sa-Token Interceptor                                │  │
│  │  JWT StateLess 校验 → 查库确认用户状态 → UserContext(ThreadLocal)│  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────┐ ┌─────────────┐ ┌──────────┐ ┌───────────┐  │
│  │UserCtrl  │ │UserProfile/ │ │Portfolio │ │ AI 对话/模板│  │
│  │注册/登录  │ │求职意向 Ctrl │ │Ctrl      │ │ SSE 流式  │  │
│  └──────────┘ └─────────────┘ └──────────┘ └───────────┘  │
│  ┌──────────┐ ┌─────────────┐ ┌──────────┐ ┌───────────┐  │
│  │EdgeOne   │ │Deployment   │ │OssCtrl   │ │LocalSite  │  │
│  │配置/域名  │ │部署/下线     │ │签名直传   │ │本地站点    │  │
│  └──────────┘ └─────────────┘ └──────────┘ └───────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Service 层 (@Transactional)  +  GlobalExceptionHandler│  │
│  └──────────────────────────────────────────────────────┘  │
└───────┬──────────────────────┬──────────────────────┬──────┘
        ▼                      ▼                      ▼
┌───────────────┐   ┌───────────────────┐   ┌─────────────────┐
│ MySQL 8.0     │   │ Redis             │   │ 外部服务         │
│ 13 张表       │   │ 黑名单/验证码/限额 │   │ SMTP / OSS /    │
│ (见数据库设计) │   │                   │   │ LLM(OpenAI 兼容)│
└───────────────┘   └───────────────────┘   │ / EdgeOne CLI   │
                                            └─────────────────┘
```

***

## 📁 项目结构

```
WorksShow/
├── frontend/                     # 前端源码
│   ├── src/
│   │   ├── api/                  # Axios 封装与各模块 API
│   │   │   ├── request.ts        #   实例 + 拦截器（token 携带 / Result 解包 / 401 跳登录）
│   │   │   ├── auth.ts           #   注册 / 登录 / 验证码 / 密码找回
│   │   │   ├── portfolio.ts      #   简历实例 CRUD
│   │   │   ├── userProfile.ts    #   用户档案读写
│   │   │   ├── career.ts         #   求职意向
│   │   │   ├── deployment.ts     #   EdgeOne 部署 API
│   │   │   ├── edgeoneConfig.ts  #   EdgeOne 配置 API
│   │   │   ├── oss.ts            #   OSS 上传凭证（扩展名白名单与后端保持同步）
│   │   │   └── ai.ts             #   AI 对话 / AI 模板生成（SSE 流式）
│   │   ├── components/           # 通用展示组件（Hero / About / Skills / Works / Modal 等）
│   │   │   ├── ai/               #   AI 助手相关组件
│   │   │   ├── DeployDialog.vue  #   EdgeOne 部署弹窗
│   │   │   └── EdgeOneConfigPanel.vue # EdgeOne 配置面板
│   │   ├── composables/          # 组合式函数
│   │   │   ├── usePortfolioStore.ts  # 全局共享简历数据 + 800ms 防抖自动保存
│   │   │   ├── useExporter.ts        # 导出单文件 HTML
│   │   │   └── ...
│   │   ├── editor/               # 可视化编辑器
│   │   │   ├── EditorView.vue    #   左表单 + 右预览主视图（含部署/AI 按钮）
│   │   │   └── panels/           #   6 个内容编辑面板
│   │   │       ├── ProfilePanel.vue          # 个人信息
│   │   │       ├── WorksPanel.vue            # 作品
│   │   │       ├── ExperiencesPanel.vue      # 经历
│   │   │       ├── SkillsPanel.vue           # 技能
│   │   │       ├── CareerIntentionPanel.vue  # 求职意向
│   │   │       └── ThemePanel.vue            # 主题
│   │   ├── templates/            # ⭐ 模板系统（10 套）
│   │   │   ├── mo-yun/           #   墨韵：东方水墨 × 杂志风
│   │   │   ├── pastel/           #   粉彩：柔和粉彩 × 轻盈现代
│   │   │   ├── aurora/           #   极光：深空 × 玻璃拟态 × Bento Grid
│   │   │   ├── dawn/             #   晨曦：暖白编辑风 × 赭石橙
│   │   │   ├── brutal/           #   野性：粗野主义 × 硬边框 × 原色块
│   │   │   ├── gazette/          #   报章：古典报刊 × 印刷美学 × 多栏排版
│   │   │   ├── glass-3d/         #   琉璃 3D：粉彩渐变 × 玻璃拟态 × 真 3D 悬浮场景
│   │   │   ├── cube-dossier/     #   立方档案：可交互 3D 信息立方（纯 CSS 3D）
│   │   │   ├── particle-planet/  #   粒子星球：发光粒子 3D 星球 × 六段变形叙事（Canvas 手写 3D）
│   │   │   └── virus-3d/         #   病毒侵染：电影级 3D 病毒 × 感染生命周期叙事（Canvas 手写 3D）
│   │   ├── views/                # 登录 / 注册 / 账号管理
│   │   ├── App.vue
│   │   ├── main.ts
│   │   └── router/               # Hash 路由 + 全局守卫
│   ├── index.html
│   ├── package.json
│   └── vite.config.ts
├── backend/                      # 后端源码
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/worksshow/
│       │   ├── WorksShowApplication.java
│       │   ├── common/
│       │   │   ├── Result.java                      # 统一响应封装
│       │   │   └── AesUtils.java                    # AES 加密工具（EdgeOne API Token 加密）
│       │   ├── config/
│       │   │   ├── WebMvcConfig.java               #   CORS 配置
│       │   │   ├── SecurityConfig.java             #   BCryptPasswordEncoder Bean
│       │   │   ├── MybatisPlusConfig.java          #   MyBatis-Plus 分页配置
│       │   │   ├── SaTokenConfig.java              #   Sa-Token JWT StateLess 配置 + 拦截器
│       │   │   ├── StpInterfaceImpl.java           #   Sa-Token 权限/角色查询接口
│       │   │   └── AiProperties.java               #   AI 服务配置（OpenAI 兼容接口）
│       │   ├── client/
│       │   │   ├── EdgeOnePagesClient.java         #   EdgeOne Pages CLI 客户端（部署执行）
│       │   │   └── AiChatClient.java               #   OpenAI 兼容对话客户端（流式）
│       │   ├── controller/                         # 11 个 REST 控制器
│       │   │   ├── UserController.java             #   注册/登录/验证码/密码
│       │   │   ├── UserProfileController.java      #   用户档案
│       │   │   ├── PortfolioController.java        #   简历实例
│       │   │   ├── CareerIntentionController.java  #   求职意向
│       │   │   ├── UserEdgeoneConfigController.java#   EdgeOne 配置
│       │   │   ├── CustomDomainController.java     #   自定义域名
│       │   │   ├── DeploymentController.java       #   部署/下线/删除
│       │   │   ├── AiChatController.java           #   AI 对话（SSE）
│       │   │   ├── AiTemplateController.java       #   AI 模板生成（SSE）
│       │   │   ├── OssController.java              #   OSS 上传凭证
│       │   │   └── LocalSiteController.java        #   本地站点公开访问
│       │   ├── dto/                                # 请求 / 响应 DTO
│       │   ├── entity/                             # 13 个实体（见数据库设计）
│       │   ├── exception/                          # 全局异常处理
│       │   ├── mapper/                             # MyBatis-Plus Mapper
│       │   ├── security/
│       │   │   └── UserContext.java                #   ThreadLocal 用户上下文
│       │   └── service/impl/
│       └── resources/
│           ├── application.yml
│           └── sql/
│               ├── schema.sql                       # 数据库初始化脚本（主表）
│               └── refresh_token.sql               # Refresh Token 表初始化脚本
└── docs/
    └── TECHNICAL.md                                # 本文档
```

***

## 🎯 关键技术实现

### 1. 模板注册表机制（前端模板引擎）

每套模板是一个独立目录，包含 5 个标准化文件：

| 文件                  | 职责                                      |
| ------------------- | --------------------------------------- |
| `meta.ts`           | 模板元信息（id / name / description / author） |
| `defaultData.ts`    | 默认填充数据                                  |
| `XxxPreview.vue`    | 编辑器内的预览渲染组件                             |
| `exportTemplate.ts` | 导出为单文件 HTML 的函数                         |
| `runtime.ts`        | 导出后 HTML 中运行的原生 JS（模态框切换、3D 交互等）       |

所有模板通过 `registry.ts` 统一注册，外部通过 4 个纯函数访问：

```ts
// src/templates/registry.ts
const registry: Record<string, TemplateEntry> = {
  'mo-yun':  { ...moYunMeta,  getDefaultData, PreviewComponent, exportHtml },
  'virus-3d': { ... },
  // 新增模板只需在此注册一项
}

export function getTemplateMeta(id) / getTemplateDefaultData(id)
export function getTemplatePreviewComponent(id) / getTemplateExporter(id)
```

新增一套模板 = 新建一个目录 + 在 registry 加一行，**零侵入**。当前共 **10 套**模板，其中 3 套为 Canvas/CSS 手写 3D（零依赖，导出后 100% 离线可用）。

### 2. 内容与模板解耦的数据模型

这是本项目最具代表性的架构决策。早期版本曾将简历内容（profile/work/experience/skill）挂在 `portfolio` 子表下，导致同一用户在多模板间重复填写。重构后：

```
portfolio 表        →  仅记录「用户启用了哪个模板」(user_id + template_id + name)
user_profile 表     →  用户级个人信息（1:1，所有模板共享）
user_work 表        →  用户级作品列表（所有模板共享，含视频 URL）
user_experience 表   →  用户级经历（工作 + 教育统一存储，type 字段区分）
user_skill 表       →  用户级技能（按分类，JSON 列）
```

**收益**：填一次内容，所有模板复用；切换模板零成本；数据一致性强。`schema.sql` 中保留了完整的旧库迁移语句（`portfolio_*` 子表数据迁入 `user_*`）。

### 3. 单文件 HTML 导出原理

导出按钮触发的 `exportTemplate.ts` 流程：

```
1. 创建离屏 <div>（position:fixed; left:-99999px）
2. createApp(PreviewComponent, { data }).mount(host)  ← Vue 渲染完整页面
3. 逐个 createApp(WorkModal).mount() 渲染作品模态框（初始 display:none）
4. host.innerHTML 取 DOM 序列化结果
5. collectStyles() 收集主文档所有 <style>（含 scoped 样式）
6. buildHtml() 拼装：<!DOCTYPE html> + <style> + body + <script>runtime</script>
7. Blob + a.click() 触发下载
```

最终产物是**一个零依赖的 HTML 文件**（3D 模板含手写 Canvas/CSS 3D runtime），可直接拖进浏览器或部署到 GitHub Pages / Vercel / 任意静态服务器。

### 4. Sa-Token 鉴权与安全拦截器

项目采用 **Sa-Token JWT StateLess（无状态）** 模式，核心配置在 [SaTokenConfig.java](../backend/src/main/java/com/worksshow/config/SaTokenConfig.java)：

```
请求 →  SaInterceptor.handle()
   ├─ StpUtil.checkLogin()         ← JWT 签名与过期校验，失败抛 NotLoginException
   ├─ StpUtil.getLoginIdAsLong()   ← 从 JWT payload 中提取 userId（无状态模式，无需查会话）
   ├─ userService.getById(userId)  ← 查库确认用户当前状态
   │    （覆盖逻辑删除 / 账号禁用的实时生效，因为 getById 受 @TableLogic 影响）
   └─ 用户不存在或 status=0 → StpUtil.logout() + 抛异常
```

**关键设计**：
- **JWT StateLess 模式**：token 自包含 userId 与 nickname，服务端不维护会话
- **实时状态校验**：token 有效期内若用户被禁用或逻辑删除，拦截器查库会返回 null，立即拒绝访问——不依赖 token 过期
- **权限扩展**：[StpInterfaceImpl.java](../backend/src/main/java/com/worksshow/config/StpInterfaceImpl.java) 预留权限/角色查询接口，后续可配合 `@SaCheckRole` / `@SaCheckPermission` 使用

### 5. Refresh Token 与 Token 黑名单机制

为提升安全性，项目引入 Refresh Token 机制，将 access token 有效期缩短，降低泄漏窗口：

```
登录流程：
POST /login → 返回 access_token + refresh_token(7天)
           → refresh_token 存入数据库 refresh_token 表

刷新流程：
POST /refresh-token → 校验 refresh_token 是否有效且未过期
              → 生成新的 access_token + 新的 refresh_token（滚动刷新）
              → 旧 refresh_token 加入黑名单，新 token 存入数据库

登出流程：
POST /logout → 当前 access_token 加入黑名单(TTL 与 access token 有效期一致)
             → 删除该用户所有 refresh_token(切断滚动刷新链路)
```

**关键设计**：
- **黑名单机制**：`TokenBlacklistServiceImpl` 使用 Redis 存储已失效的 token，拦截器在校验 JWT 后额外检查黑名单。注：无状态 JWT 无法枚举用户在其他设备已签发的 token，改密/重置密码时只能黑名单当前请求的 token 并删除全部 refresh token，其他设备的 access token 将在有效期内自然失效
- **滚动刷新**：每次刷新生成新的 refresh_token，旧 token 失效，防止 token 泄露后被持续滥用
- **定时清理**：过期的 refresh_token 记录通过定时任务清理

### 6. 邮箱验证码安全设计（`EmailCodeServiceImpl`）

| 安全点   | 实现                                                         |
| ----- | ---------------------------------------------------------- |
| 随机性   | `SecureRandom` 生成 6 位数字，防止预测                               |
| 有效期   | 5 分钟，Redis TTL 自动过期，无需定时清理任务                              |
| 频率限制  | 60 秒内不可重发，返回剩余秒数                                           |
| 一次性使用 | 验证成功时 `DEL` 原子删除；验证失败 `HINCRBY` 原子递增尝试次数，超限作废 |
| 防账号枚举 | 注册时已注册邮箱「静默跳过」；找回密码时未注册邮箱「静默跳过」                            |
| 事务一致性 | 注册方法 `@Transactional`，先 `save(user)` 后 `verifyCode`，验证失败回滚 |
| 基于 Redis | 验证码存于 Redis Hash，与 token 黑名单共用同一 Redis，支持多实例部署      |

### 7. 全局异常处理与统一响应

所有接口统一返回 `Result<T>`（`code` / `message` / `data`），`GlobalExceptionHandler` 统一捕获：

| 异常                                | 响应码 | 说明                        |
| --------------------------------- | --- | ------------------------- |
| `BusinessException`               | 自定义 | 可预期的业务错误                  |
| `MethodArgumentNotValidException` | 400 | `@Valid` 参数校验失败，返回字段级错误信息 |
| `DuplicateKeyException`           | 409 | 唯一索引兜底（并发注册竞态）            |
| `HttpMessageNotReadableException` | 400 | JSON 解析失败                 |
| `Exception`                       | 500 | 兜底，隐藏堆栈防信息泄露              |

### 8. 前端自动保存（`usePortfolioStore`）

- 全局单例 `reactive` data，所有模板共享同一份用户档案
- `watch(data, ..., { deep: true })` + 800ms 防抖触发 `saveToServer`
- **保存期间变更不丢失**：`saving=true` 时新变更置 `pendingSave=true`，`finally` 中检查并重新调度
- **加载失败保护**：`loadError=true` 时跳过保存，避免用默认数据覆盖服务端档案
- 手动保存按钮取消防抖立即触发

### 9. OSS 服务端签名直传（图片 / 视频）

作品封面图与视频采用**阿里云 OSS 后端签名 + 前端直传**（AK/SK 不出后端）：

```
前端                        后端                          OSS
 │ POST /oss/upload-token    │                            │
 │──────────────────────────▶│ 校验扩展名白名单 + 每日限额     │
 │                           │ 生成 objectKey              │
 │                           │ uploads/{userId}/{type}/{uuid}.{ext}
 │ ◀─────uploadUrl/publicUrl/contentType ─────────────────│
 │ PUT uploadUrl (原生 XHR, 带 ContentType, 进度条)          │
 │─────────────────────────────────────────────────────────▶│
```

**关键设计**：
- **objectKey 服务端生成**：UUID 防枚举 + userId 目录隔离
- **预签名必须签 Content-Type**：浏览器 XHR 发送 File 必带该头，后端按扩展名白名单推导 MIME 并返回 `contentType`，前端必须原样携带，否则 403 SignatureDoesNotMatch
- **扩展名白名单**：图片 jpg/jpeg/png/webp/gif ≤10MB；视频 mp4/webm ≤200MB（大小仅前端校验）；后端 `OssServiceImpl` 与前端 `oss.ts` 白名单保持同步
- **每日限额**：Redis key `oss:upload-limit:{userId}:{yyyyMMdd}`（默认 200 次/天）
- 作品有 `video_url` 时模板渲染 `<video>`，否则渲染封面 `<img>`（全部模板已适配）

### 10. AI 集成（对话润色 + 模板生成，SSE 流式）

后端对接**任意 OpenAI 兼容 chat/completions 接口**（默认阿里云百炼 DashScope 兼容模式，模型 qwen-plus），核心在 [AiChatClient.java](../backend/src/main/java/com/worksshow/client/AiChatClient.java)：

- **AI 对话助手**（`POST /ai/chat`）：后端将用户简历内容注入 system prompt，多轮对话上下文持久化于 `ai_message` 表（按简历实例关联），响应以 **SSE（text/event-stream）** 逐 token 转发给前端打字机效果
- **AI 模板生成**（`POST /ai/template/generate`）：用户用自然语言描述风格，AI 流式生成自定义模板代码，产物存 `ai_template` 表，可列表查看 / 详情 / 删除
- **每日限额**：Redis 计数（对话默认 30 次/天，模板生成默认 5 次/天），超限返回业务异常
- **超时控制**：LLM 请求超时可配（默认 120s），SSE 连接异常时优雅关闭

### 11. 本地站点与 EdgeOne 一键部署

**部署双通道**：

| 通道 | 实现 | 适用 |
| --- | --- | --- |
| EdgeOne Pages | 后端 `ProcessBuilder` 调用官方 CLI `edgeone pages deploy <dir> -t <token> -n <project>`，同步等待执行完成，解析 stdout 中的项目 ID 与访问 URL | 公网访问 |
| 本地站点 | 导出的 HTML 落盘 `./local-sites/{key}`，`GET /api/sites/{key}` 公开访问（无需鉴权） | 内网预览 |

**关键设计**：
- EdgeOne 采用「一项目 = 一站点 = 一域名」模型，每个作品集独立部署为 EdgeOne 项目
- API Token 以 **AES 对称加密**存储（加密密钥来自环境变量），永不返回前端（仅脱敏展示）
- EdgeOne Pages 无公开 HTTP REST 部署 API，CLI 为官方唯一部署方式，需服务器预装 Node.js 18+ 与 edgeone CLI ≥1.2.30
- 部署记录支持**下线**（`POST /deployment/{id}/offline`）与**删除**

***

## 🗄️ 数据库设计

13 张表，utf8mb4 字符集，使用 JSON 列存储嵌套数组（stats / socials / tags / items / 期望行业 / 期望城市）。

| 表名                    | 说明              | 关键设计                                                                   |
| --------------------- | --------------- | ---------------------------------------------------------------------- |
| `user`                | 用户表             | BCrypt 密码；复合唯一索引 `(email, deleted)` / `(phone, deleted)` 解决逻辑删除后无法重新注册 |
| `user_profile`        | 用户简历档案（1:1）     | 懒创建；JSON 列存 stats/socials                                              |
| `portfolio`           | 简历实例（模板实例）      | 仅 `user_id + template_id + name`，内容存于 user\_\*                         |
| `user_work`           | 用户作品            | JSON 列存 tags；highlight 标记旗舰作品；sort\_order 排序；video\_url 存视频           |
| `user_experience`     | 用户经历            | `type` 字段区分 work / education，统一存储                                      |
| `user_skill`          | 用户技能            | 按 category 分组，JSON 列存 items                                            |
| `career_intention`    | 求职意向（1:1）       | 期望行业/城市用 JSON 数组；薪资区间                                                  |
| `user_edgeone_config` | EdgeOne 配置（1:1） | API Token AES 加密存储；永不返回前端；项目名明文存储                                      |
| `custom_domain`       | 自定义域名（1:N）      | 可复用，一个域名关联多次部署                                                        |
| `deployment`          | 部署记录（1:N）       | 部署状态、访问 URL、错误信息；关联 portfolio 与自定义域名；支持同步等待 CLI 执行完成                  |
| `refresh_token`       | Refresh Token（1:N） | UUID 存储；过期时间 7 天；支持 access token 过期后刷新获取新 token             |
| `ai_message`          | AI 对话消息（1:N）    | 关联 portfolio，保存多轮对话上下文，支持拉取与清空                                        |
| `ai_template`         | AI 生成模板（1:N）    | 用户自然语言生成的自定义模板产物，支持列表 / 详情 / 删除                                       |

初始化脚本：
- [backend/src/main/resources/sql/schema.sql](../backend/src/main/resources/sql/schema.sql)（主表）
- [backend/src/main/resources/sql/refresh_token.sql](../backend/src/main/resources/sql/refresh_token.sql)（Refresh Token 表）

均使用 `CREATE TABLE IF NOT EXISTS`，可安全重复执行。

***

## 🔌 API 接口概览

所有接口前缀 `/api`，统一返回 `Result<T>`，鉴权基于 **Sa-Token JWT StateLess** 模式。

### 用户接口 `/api/user`

| 方法   | 路径                           | 鉴权  | 说明                   |
| ---- | ---------------------------- | --- | -------------------- |
| POST | `/sendCode`                  | 公开  | 发送邮箱验证码（已注册邮箱静默跳过）   |
| POST | `/register`                  | 公开  | 注册（事务：先存用户后验证码）      |
| POST | `/login`                     | 公开  | 登录，支持手机号 / 邮箱，返回 token |
| GET  | `/info`                      | 需登录 | 获取当前用户               |
| PUT  | `/info`                      | 需登录 | 更新昵称 / 性别 / 头像       |
| PUT  | `/password`                  | 需登录 | 修改密码                 |
| POST | `/forgot-password/send-code` | 公开  | 发送重置密码验证码            |
| POST | `/forgot-password/reset`     | 公开  | 邮箱验证码重置密码            |
| POST | `/refresh-token`             | 公开  | 刷新 token             |
| POST | `/logout`                    | 需登录 | 退出登录（token 加入黑名单，删除所有 refresh_token） |

### 用户档案接口 `/api/user-profile`

| 方法  | 路径  | 说明                         |
| --- | --- | -------------------------- |
| GET | `/` | 获取档案（不存在返回空对象，前端显示空表单）     |
| PUT | `/` | 保存档案（懒创建 + 整体替换，事务保证多表原子性） |

### 简历实例接口 `/api/portfolio`

| 方法     | 路径      | 说明                          |
| ------ | ------- | --------------------------- |
| GET    | `/list` | 当前用户简历实例列表                  |
| GET    | `/{id}` | 简历详情（实例元数据 + 用户档案内容）        |
| POST   | `/`     | 创建实例（同模板去重，已存在则返回已有）        |
| PUT    | `/{id}` | 更新（仅 name，template\_id 不可变） |
| DELETE | `/{id}` | 逻辑删除（用户档案不受影响）              |

### 求职意向接口 `/api/career-intention`

| 方法  | 路径  | 说明              |
| --- | --- | --------------- |
| GET | `/` | 获取求职意向          |
| PUT | `/` | 保存 / 更新（upsert） |

### AI 接口 `/api/ai`

| 方法     | 路径         | 说明                            |
| ------ | ---------- | ----------------------------- |
| POST   | `/chat`    | AI 对话（SSE 流式响应，注入简历上下文，每日限额） |
| GET    | `/messages` | 获取指定简历实例的对话历史（portfolioId）  |
| DELETE | `/messages` | 清空指定简历实例的对话历史                |

### AI 模板接口 `/api/ai/template`

| 方法     | 路径         | 说明                     |
| ------ | ---------- | ---------------------- |
| POST   | `/generate` | 自然语言生成自定义模板（SSE 流式，每日限额） |
| GET    | `/list`    | 当前用户 AI 模板列表           |
| GET    | `/{id}`    | AI 模板详情                |
| DELETE | `/{id}`    | 删除 AI 模板               |

### OSS 上传接口 `/api/oss`

| 方法   | 路径              | 说明                                      |
| ---- | --------------- | --------------------------------------- |
| POST | `/upload-token` | 获取预签名直传凭证（uploadUrl / objectKey / publicUrl / contentType，每日限额） |

### EdgeOne 配置接口 `/api/edgeone-config`

| 方法     | 路径  | 说明                              |
| ------ | --- | ------------------------------- |
| GET    | `/` | 获取当前用户配置（脱敏，不返回 API Token）      |
| POST   | `/` | 保存 / 更新配置（API Token 入库前 AES 加密） |
| DELETE | `/` | 删除配置（逻辑删除）                      |

### 自定义域名接口 `/api/custom-domain`

| 方法     | 路径      | 说明                  |
| ------ | ------- | ------------------- |
| GET    | `/list` | 获取当前用户域名列表          |
| POST   | `/`     | 新增域名                |
| PUT    | `/{id}` | 更新域名（修改备注名）         |
| DELETE | `/{id}` | 删除域名（逻辑删除，不影响已部署页面） |

### 部署接口 `/api/deployment`

| 方法     | 路径              | 说明                                             |
| ------ | --------------- | ---------------------------------------------- |
| POST   | `/`             | 部署简历到 EdgeOne Pages（传入渲染后的 HTML，同步等待 CLI 执行完成） |
| GET    | `/list`         | 获取当前用户部署记录列表                                   |
| GET    | `/{id}`         | 获取部署详情（状态、访问 URL、错误信息）                         |
| POST   | `/{id}/offline` | 下线部署站点                                         |
| DELETE | `/{id}`         | 删除部署记录                                         |

### 本地站点接口 `/api/sites`

| 方法  | 路径      | 说明                       |
| --- | ------- | ------------------------ |
| GET | `/{key}` | 公开访问本地部署的站点 HTML（无需鉴权） |

***

## 🔐 环境变量清单

生产环境务必通过环境变量注入敏感配置（本地开发可在 `application-dev.yml` 中提供回退值）：

| 环境变量 | 必填 | 说明 |
| --- | --- | --- |
| `DB_USERNAME` / `DB_PASSWORD` | ✅ | 数据库账号 |
| `JWT_SECRET` | ✅ | Sa-Token JWT 签名密钥（强随机 Base64） |
| `EDGEONE_ENCRYPT_KEY` | ✅ | EdgeOne API Token 的 AES 加密密钥 |
| `OSS_ACCESS_KEY_ID` / `OSS_ACCESS_KEY_SECRET` | ✅ | 阿里云 OSS AK/SK（空值会导致启动失败，fail-fast） |
| `AI_API_KEY` | ✅（启用 AI 时） | OpenAI 兼容接口的 API Key |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | - | Redis 连接（默认 localhost:6379） |
| `AI_BASE_URL` / `AI_MODEL` | - | LLM 接口地址与模型（默认百炼 qwen-plus） |
| `AI_DAILY_LIMIT` / `AI_TEMPLATE_DAILY_LIMIT` | - | AI 对话 / 模板生成每日限额（默认 30 / 5） |
| `OSS_ENDPOINT` / `OSS_BUCKET` / `OSS_DAILY_LIMIT` | - | OSS 端点 / 桶名（默认公共读）/ 每日上传限额（默认 200） |
| `LOCAL_DEPLOY_DIR` / `LOCAL_DEPLOY_BASE_URL` | - | 本地站点落盘目录（默认 ./local-sites）与访问基址 |
| 邮箱 SMTP | ✅ | host / username / 授权码（QQ 邮箱为例，见 application.yml 邮件配置注释） |

> 注：`application.yml` 已加入 `.gitignore`，防止敏感信息泄漏。

***

## 📦 构建与部署

### 前端

```bash
cd frontend
npm run build     # vue-tsc 类型检查 + Vite 打包，产物在 frontend/dist/
npm run preview   # 本地预览生产构建
npm run check     # 仅类型检查
```

部署：将 `frontend/dist/` 部署到 Nginx / Vercel / Netlify / GitHub Pages 等任意静态托管，反向代理 `/api` 到后端。

### 后端

```bash
cd backend
mvn clean package -DskipTests
java -jar target/worksshow-backend-0.0.1-SNAPSHOT.jar
```

### EdgeOne 一键部署的服务器要求

- Node.js ≥ 18
- edgeone CLI ≥ 1.2.30（`npm install -g edgeone`）
- 用户在「账号管理 → 部署配置」中配置 EdgeOne API Token 后，编辑器「部署」按钮即可一键发布

***

## 📝 开发命令速查

```bash
# 前端（进入 frontend 目录）
cd frontend
npm install          # 安装依赖
npm run dev          # 启动开发服务器 (http://localhost:5173)
npm run build        # 类型检查 + 生产构建（产物在 frontend/dist/）
npm run preview      # 预览生产构建
npm run check        # 仅类型检查

# 后端（进入 backend 目录）
cd backend
mvn spring-boot:run           # 启动开发服务器 (http://localhost:8080/api)
mvn clean package             # 打包
mvn test                      # 运行测试

# 数据库初始化
mysql -u root -p < backend/src/main/resources/sql/schema.sql
mysql -u root -p < backend/src/main/resources/sql/refresh_token.sql
```

***

## 📚 相关文档

- [README.md](../README.md)（项目介绍，英文）
- [README.zh-CN.md](../README.zh-CN.md)（项目介绍，中文）
- [TECHNICAL.en.md](./TECHNICAL.en.md)（技术文档，English）

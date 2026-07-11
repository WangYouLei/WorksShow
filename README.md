# WorksShow · 可视化作品集编辑器

> 一个「填一次内容，多模板切换，一键导出」的全栈作品集/简历编辑器。
> 前端 Vue 3 + TypeScript + Vite，后端 Spring Boot 3 + MyBatis-Plus + MySQL，JWT 鉴权 + 邮箱验证码注册。

<p align="center">
  <img alt="Vue" src="https://img.shields.io/badge/Vue-3.4-42b883?logo=vue.js&logoColor=white">
  <img alt="TypeScript" src="https://img.shields.io/badge/TypeScript-5.3-3178c6?logo=typescript&logoColor=white">
  <img alt="Vite" src="https://img.shields.io/badge/Vite-5-646cff?logo=vite&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.2.5-6db33f?logo=springboot&logoColor=white">
  <img alt="MySQL" src="https://img.shields.io/badge/MySQL-8.0-4479a1?logo=mysql&logoColor=white">
  <img alt="License" src="https://img.shields.io/badge/License-MIT-yellow">
</p>

***

## 📖 项目简介

**WorksShow** 是一个面向开发者与设计师的「个人品牌站点」构建工具。用户只需在可视化编辑器中填写一次个人信息、作品、经历与技能，即可在 **5 套风格迥异的模板** 间自由切换预览，并支持 **一键导出为单文件 HTML**（含内联 CSS + 原生 JS runtime，可直接部署到任意静态托管平台）。

项目采用前后端分离架构，后端提供完整的用户体系（注册、登录、邮箱验证码、密码找回）、用户档案管理与简历实例管理，所有接口均通过 JWT 拦截器鉴权。

### ✨ 核心亮点

| 维度                 | 说明                                                           |
| ------------------ | ------------------------------------------------------------ |
| 🎨 **多模板引擎**       | 自研模板注册表机制，5 套模板（墨韵 / 粉彩 / 极光 / 晨曦 / 野性）各自独立打包，新增模板只需注册一个对象   |
| 🧩 **内容与模板解耦**     | 简历内容与模板实例分离存储，填一次内容所有模板复用，切换模板零成本                            |
| 📦 **单文件 HTML 导出** | 利用 Vue 离屏渲染 + DOM 序列化，将运行时组件树导出为无依赖的独立 HTML 文件               |
| 🔒 **工程化安全实践**     | BCrypt 加密 / JWT 鉴权 / SecureRandom 验证码 / 防账号枚举 / 逻辑删除 / 事务一致性 |
| ⚡ **前端自动保存**       | 深度 watch + 800ms 防抖，编辑即持久化；保存期间变更通过 `pendingSave` 标志位避免丢失    |
| 🧱 **零 UI 框架**     | 前端纯手写组件 + scoped 样式，无 Element/Ant Design 等依赖，体积可控            |

***

## 🖼️ 截图预览

> 以下为运行截图（可替换为实际 GIF/图片）：

- **模板画廊**：5 套模板可视化选择
- **编辑器**：左侧表单 + 右侧 iframe 实时预览，5 个内容面板切换
- **登录/注册**：邮箱验证码注册流程
- **导出效果**：单文件 HTML 在浏览器中独立运行

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
| jjwt                   | 0.11.5 | JWT 生成与解析（HS256）                               |
| spring-security-crypto | -      | 仅引入 `BCryptPasswordEncoder`，不启用完整 Security 过滤链 |
| Lombok                 | -      | 减少实体样板代码                                       |

***

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                         浏览器                              │
│  Vue 3 SPA (Vite dev / 静态部署)                            │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────┐    │
│  │ 模板画廊    │  │ 编辑器     │  │ 登录/注册/账号     │    │
│  │ Gallery    │  │ 左表单+右预览│  │ JWT 存 localStorage │    │
│  └────────────┘  └────────────┘  └────────────────────┘    │
└──────────────────────────┬──────────────────────────────────┘
                           │  Axios  baseURL=/api
                           │  请求拦截器: Bearer token
                           │  响应拦截器: 解包 Result / 401 跳登录
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot 3  (context-path: /api)           │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  JwtAuthenticationInterceptor                        │  │
│  │  校验 token → 查库确认用户状态 → UserContext(ThreadLocal)│  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ UserController│ │UserProfileCtrl│ │PortfolioCtrl 等  │  │
│  │ 注册/登录/验证码│ │ 档案CRUD      │ │ 简历实例CRUD     │  │
│  └─────────────┘  └──────────────┘  └──────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Service 层 (@Transactional)  +  GlobalExceptionHandler│  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────────┬──────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              MySQL  (works_show, utf8mb4)                   │
│  user / user_profile / portfolio / user_work /             │
│  user_experience / user_skill / career_intention           │
└─────────────────────────────────────────────────────────────┘
```

***

## 📁 项目结构

```
WorksShow/
├── src/                          # 前端源码
│   ├── api/                      # Axios 封装与各模块 API
│   │   ├── request.ts            #   实例 + 拦截器（token 携带 / Result 解包 / 401 跳登录）
│   │   ├── auth.ts               #   注册 / 登录 / 验证码 / 密码找回
│   │   ├── portfolio.ts          #   简历实例 CRUD
│   │   ├── userProfile.ts        #   用户档案读写
│   │   └── career.ts             #   求职意向
│   ├── components/               # 通用展示组件（Hero / About / Skills / Works / Modal …）
│   ├── composables/              # 组合式函数
│   │   ├── usePortfolioStore.ts  #   全局共享简历数据 + 800ms 防抖自动保存
│   │   ├── useExporter.ts        #   导出单文件 HTML
│   │   ├── usePortfolio.ts       #   provide/inject 数据注入
│   │   └── useScrollReveal.ts    #   滚动揭示动画
│   ├── data/                     # 数据类型与默认数据
│   ├── editor/                   # 可视化编辑器
│   │   ├── EditorView.vue        #   左表单 + 右预览主视图
│   │   ├── PreviewFrame.vue      #   iframe 实时预览
│   │   ├── TemplateGallery.vue   #   模板选择页
│   │   └── panels/               #   5 个内容编辑面板
│   ├── templates/                # ⭐ 模板系统（5 套）
│   │   ├── registry.ts           #   统一注册表
│   │   ├── types.ts              #   Template / TemplateMeta 接口
│   │   ├── mo-yun/               #   墨韵：东方水墨 × 杂志风
│   │   ├── pastel/               #   粉彩：柔和粉彩 × 轻盈现代
│   │   ├── aurora/               #   极光：深空 × 玻璃拟态 × Bento Grid
│   │   ├── dawn/                 #   晨曦：暖白编辑风 × 赭石橙
│   │   └── brutal/               #   野性：粗野主义 × 硬边框 × 原色块
│   ├── views/                    # 登录 / 注册 / 账号管理
│   ├── App.vue
│   ├── main.ts
│   └── router.ts                 # Hash 路由 + 全局守卫
├── backend/                      # 后端源码
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/worksshow/
│       │   ├── WorksShowApplication.java
│       │   ├── common/Result.java                  # 统一响应封装
│       │   ├── config/
│       │   │   ├── WebMvcConfig.java               #   JWT 拦截器注册 + CORS
│       │   │   ├── SecurityConfig.java             #   BCryptPasswordEncoder Bean
│       │   │   └── MybatisPlusConfig.java
│       │   ├── controller/                         # 4 个 REST 控制器
│       │   ├── dto/                                # 请求 / 响应 DTO
│       │   ├── entity/                             # 7 个实体
│       │   ├── exception/                          # 全局异常处理
│       │   ├── mapper/                             # MyBatis-Plus Mapper
│       │   ├── security/
│       │   │   ├── JwtUtils.java                   #   JWT 工具
│       │   │   ├── JwtAuthenticationInterceptor.java
│       │   │   └── UserContext.java                #   ThreadLocal 上下文
│       │   └── service/impl/
│       └── resources/
│           ├── application.yml
│           └── sql/schema.sql                      # 数据库初始化脚本
├── vite.config.ts
├── tsconfig.json
├── package.json
└── README.md
```

***

## 🎯 关键技术实现

> 本节面向技术评审，详述几个有代表性的设计点。

### 1. 模板注册表机制（前端模板引擎）

每套模板是一个独立目录，包含 5 个标准化文件：

| 文件                  | 职责                                      |
| ------------------- | --------------------------------------- |
| `meta.ts`           | 模板元信息（id / name / description / author） |
| `defaultData.ts`    | 默认填充数据                                  |
| `XxxPreview.vue`    | 编辑器内的预览渲染组件                             |
| `exportTemplate.ts` | 导出为单文件 HTML 的函数                         |
| `runtime.ts`        | 导出后 HTML 中运行的原生 JS（模态框切换等）              |

所有模板通过 `registry.ts` 统一注册，外部通过 4 个纯函数访问：

```ts
// src/templates/registry.ts
const registry: Record<string, TemplateEntry> = {
  'mo-yun':  { ...moYunMeta,  getDefaultData, PreviewComponent, exportHtml },
  'pastel':  { ... },
  // 新增模板只需在此注册一项
}

export function getTemplateMeta(id) / getTemplateDefaultData(id)
export function getTemplatePreviewComponent(id) / getTemplateExporter(id)
```

新增一套模板 = 新建一个目录 + 在 registry 加一行，**零侵入**。

### 2. 内容与模板解耦的数据模型

这是本项目最具代表性的架构决策。早期版本曾将简历内容（profile/work/experience/skill）挂在 `portfolio` 子表下，导致同一用户在多模板间重复填写。重构后：

```
portfolio 表        →  仅记录「用户启用了哪个模板」(user_id + template_id + name)
user_profile 表     →  用户级个人信息（1:1，所有模板共享）
user_work 表        →  用户级作品列表（所有模板共享）
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

最终产物是**一个零依赖的 HTML 文件**，可直接拖进浏览器或部署到 GitHub Pages / Vercel / 任意静态服务器。

### 4. JWT 鉴权与安全拦截器

```
请求 →  JwtAuthenticationInterceptor.preHandle()
   ├─ OPTIONS 预检 → 直接放行
   ├─ resolveToken() 从 Authorization 头取 Bearer token
   ├─ jwtUtils.parseAndValidate()  ← 复用，避免重复 HMAC 验签
   ├─ userService.getById(userId)  ← 查库确认用户当前状态
   │    （覆盖逻辑删除 / 账号禁用的实时生效，因为 getById 受 @TableLogic 影响）
   ├─ UserContext.set(LoginUser)    ← ThreadLocal 注入
   └─ afterCompletion → UserContext.clear()  ← 防止线程池复用串号
```

**关键设计**：token 有效期内若用户被禁用或逻辑删除，拦截器查库会返回 null，立即拒绝访问——不依赖 token 过期。

### 5. 邮箱验证码安全设计（`EmailCodeServiceImpl`）

| 安全点   | 实现                                                         |
| ----- | ---------------------------------------------------------- |
| 随机性   | `SecureRandom` 生成 6 位数字，防止预测                               |
| 有效期   | 5 分钟，过期后 `cleanupExpiredCodes` 定时任务清理                      |
| 频率限制  | 60 秒内不可重发，返回剩余秒数                                           |
| 一次性使用 | `ConcurrentHashMap.remove(key, value)` 原子移除，防并发竞态          |
| 防账号枚举 | 注册时已注册邮箱「静默跳过」；找回密码时未注册邮箱「静默跳过」                            |
| 事务一致性 | 注册方法 `@Transactional`，先 `save(user)` 后 `verifyCode`，验证失败回滚 |
| 内存兜底  | 当前用 `ConcurrentHashMap`，注释说明生产环境应替换为 Redis                 |

### 6. 全局异常处理与统一响应

所有接口统一返回 `Result<T>`（`code` / `message` / `data`），`GlobalExceptionHandler` 统一捕获：

| 异常                                | 响应码 | 说明                        |
| --------------------------------- | --- | ------------------------- |
| `BusinessException`               | 自定义 | 可预期的业务错误                  |
| `MethodArgumentNotValidException` | 400 | `@Valid` 参数校验失败，返回字段级错误信息 |
| `DuplicateKeyException`           | 409 | 唯一索引兜底（并发注册竞态）            |
| `HttpMessageNotReadableException` | 400 | JSON 解析失败                 |
| `Exception`                       | 500 | 兜底，隐藏堆栈防信息泄露              |

### 7. 前端自动保存（`usePortfolioStore`）

- 全局单例 `reactive` data，所有模板共享同一份用户档案
- `watch(data, ..., { deep: true })` + 800ms 防抖触发 `saveToServer`
- **保存期间变更不丢失**：`saving=true` 时新变更置 `pendingSave=true`，`finally` 中检查并重新调度
- **加载失败保护**：`loadError=true` 时跳过保存，避免用默认数据覆盖服务端档案
- 手动保存按钮取消防抖立即触发

***

## 🗄️ 数据库设计

7 张表，utf8mb4 字符集，使用 JSON 列存储嵌套数组（stats / socials / tags / items / 期望行业 / 期望城市）。

| 表名                 | 说明          | 关键设计                                                                   |
| ------------------ | ----------- | ---------------------------------------------------------------------- |
| `user`             | 用户表         | BCrypt 密码；复合唯一索引 `(email, deleted)` / `(phone, deleted)` 解决逻辑删除后无法重新注册 |
| `user_profile`     | 用户简历档案（1:1） | 懒创建；JSON 列存 stats/socials                                              |
| `portfolio`        | 简历实例（模板实例）  | 仅 `user_id + template_id + name`，内容存于 user\_\*                         |
| `user_work`        | 用户作品        | JSON 列存 tags；highlight 标记旗舰作品；sort\_order 排序                           |
| `user_experience`  | 用户经历        | `type` 字段区分 work / education，统一存储                                      |
| `user_skill`       | 用户技能        | 按 category 分组，JSON 列存 items                                            |
| `career_intention` | 求职意向（1:1）   | 期望行业/城市用 JSON 数组；薪资区间                                                  |

初始化脚本：[backend/src/main/resources/sql/schema.sql](backend/src/main/resources/sql/schema.sql)（使用 `CREATE TABLE IF NOT EXISTS`，可安全重复执行）。

***

## 🔌 API 接口概览

所有接口前缀 `/api`，统一返回 `Result<T>`。

### 用户接口 `/api/user`

| 方法   | 路径                           | 鉴权  | 说明                   |
| ---- | ---------------------------- | --- | -------------------- |
| POST | `/sendCode`                  | 公开  | 发送邮箱验证码（已注册邮箱静默跳过）   |
| POST | `/register`                  | 公开  | 注册（事务：先存用户后验证码）      |
| POST | `/login`                     | 公开  | 登录，支持手机号 / 邮箱，返回 JWT |
| GET  | `/info`                      | 需登录 | 获取当前用户               |
| PUT  | `/info`                      | 需登录 | 更新昵称 / 性别            |
| PUT  | `/password`                  | 需登录 | 修改密码                 |
| POST | `/forgot-password/send-code` | 公开  | 发送重置密码验证码            |
| POST | `/forgot-password/reset`     | 公开  | 邮箱验证码重置密码            |
| POST | `/logout`                    | 需登录 | 退出登录（无状态，客户端清 token） |

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

***

## 🚀 快速开始

### 环境要求

- Node.js ≥ 18
- Java 17
- Maven 3.8+
- MySQL 8.0+（需支持 JSON 类型）
- 一个 SMTP 邮箱（用于发送验证码，以 QQ 邮箱为例）

### 1. 克隆仓库

```bash
git clone https://github.com/WangYouLei/WorksShow.git
cd WorksShow
```

### 2. 初始化数据库

```bash
mysql -u root -p < backend/src/main/resources/sql/schema.sql
```

### 3. 配置后端

编辑 `backend/src/main/resources/application.yml`，**生产环境务必通过环境变量注入敏感配置**：

```bash
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password
export JWT_SECRET=your_strong_random_base64_secret
export MAIL_HOST=smtp.qq.com
export MAIL_USERNAME=your_email@qq.com
export MAIL_PASSWORD=your_smtp_auth_code
```

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
# 或：mvn package && java -jar target/worksshow-backend-0.0.1-SNAPSHOT.jar
```

后端启动于 `http://localhost:8080/api`。

### 5. 启动前端

```bash
cd d:/java/WorksShow   # 项目根目录
npm install
npm run dev
```

前端启动于 `http://localhost:5173`（Vite 已配置 `/api` 代理到后端 8080）。

### 6. 注册账号并使用

打开 `http://localhost:5173` → 注册（需邮箱验证码）→ 选择模板 → 进入编辑器填写内容 → 导出 HTML。

***

## 📦 构建与部署

### 前端

```bash
npm run build     # vue-tsc 类型检查 + Vite 打包，产物在 dist/
npm run preview   # 本地预览生产构建
npm run check     # 仅类型检查
```

部署：将 `dist/` 部署到 Nginx / Vercel / Netlify / GitHub Pages 等任意静态托管，反向代理 `/api` 到后端。

### 后端

```bash
cd backend
mvn clean package -DskipTests
java -jar target/worksshow-backend-0.0.1-SNAPSHOT.jar
```

***

## 💡 项目亮点回顾

1. **自研多模板引擎**：注册表 + 标准化模板接口，扩展性极强，新增模板零侵入
2. **内容与模板解耦**：一份内容驱动多套模板，数据一致性高
3. **单文件 HTML 导出**：Vue 离屏渲染 + DOM 序列化，无运行时依赖
4. **完整的安全实践**：BCrypt / JWT / SecureRandom / 防账号枚举 / 逻辑删除 / 事务一致性 / 越权校验
5. **工程化规范**：统一响应封装、全局异常处理、DTO 类型化、环境变量配置、可重复执行的 SQL 脚本
6. **零 UI 框架**：纯手写组件，对 CSS 与组件设计有深度练习

***

## 📝 开发命令速查

```bash
# 前端
npm install          # 安装依赖
npm run dev          # 启动开发服务器 (5173)
npm run build        # 类型检查 + 生产构建
npm run preview      # 预览生产构建
npm run check        # 仅类型检查

# 后端
mvn spring-boot:run           # 启动开发服务器 (8080)
mvn clean package             # 打包
mvn test                      # 运行测试
```

***


# WorksShow Technical Documentation

<div align="center">

[ 🇨🇳 中文 ](./TECHNICAL.md) · [ 🇬🇧 English ](./TECHNICAL.en.md)

</div>

> This document is for developers and code reviewers. It covers the tech stack, system architecture, key implementations, database design, and API reference.
> For a product overview, see [README.md](../README.md) | [中文 README](../README.zh-CN.md)

***

## 🛠️ Tech Stack

### Frontend

| Technology        | Version | Purpose                                                                 |
| ----------------- | ------- | ----------------------------------------------------------------------- |
| Vue 3             | 3.4     | Composition API + `<script setup>`                                     |
| TypeScript        | 5.3     | Full type coverage (`vue-tsc` compile-time checks)                    |
| Vite              | 5.0     | Dev server + production build                                          |
| Vue Router        | 4.6     | Hash-mode routing + global guards                                      |
| Axios             | 1.18    | Unified request/response interceptors                                  |
| Lucide Vue Next   | 0.511   | Icon library                                                           |

### Backend

| Technology                 | Version | Purpose                                                                                 |
| -------------------------- | ------- | --------------------------------------------------------------------------------------- |
| Spring Boot                | 3.2.5   | Web + Validation + Mail                                                                 |
| Java                       | 17      | Modern features: `record`, `var`, switch expressions, etc.                             |
| MyBatis-Plus               | 3.5.7   | ORM + logical delete + Lambda queries                                                   |
| MySQL                      | 8.0+    | Primary data store (utf8mb4, JSON columns)                                              |
| Redis                      | 6.0+    | Token blacklist, email verification codes, AI/OSS daily quotas — multi-instance ready    |
| Sa-Token                   | 1.38.0  | Auth framework (JWT StateLess / stateless mode)                                         |
| spring-security-crypto     | -       | Only `BCryptPasswordEncoder` — no full Security filter chain                           |
| aliyun-sdk-oss             | 3.17.4  | OSS presigned direct upload (Java 17 needs additional jaxb deps)                       |
| Lombok                     | -       | Reduces entity boilerplate                                                              |

***

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Browser                             │
│  Vue 3 SPA (Vite dev / static deploy)                       │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────┐    │
│  │ Template   │  │ Editor     │  │ Login / Register   │    │
│  │ Gallery    │  │ Form +     │  │ Account (token in  │    │
│  │            │  │ Live Prev. │  │ localStorage)      │    │
│  └────────────┘  └────────────┘  └────────────────────┘    │
└──────────────────────────┬──────────────────────────────────┘
                           │  Axios  baseURL=/api
                           │  Request interceptor: Bearer token
                           │  Response interceptor: unwrap Result / 401 → login
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot 3  (context-path: /api)           │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Sa-Token Interceptor                                │  │
│  │  JWT StateLess → verify user state from DB →         │  │
│  │                 UserContext (ThreadLocal)            │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────┐ ┌─────────────┐ ┌──────────┐ ┌───────────┐  │
│  │UserCtrl  │ │UserProfile/ │ │Portfolio │ │ AI Chat / │  │
│  │Register/ │ │Career Ctrl  │ │Ctrl      │ │ Template  │  │
│  │Login     │ │             │ │          │ │ (SSE)     │  │
│  └──────────┘ └─────────────┘ └──────────┘ └───────────┘  │
│  ┌──────────┐ ┌─────────────┐ ┌──────────┐ ┌───────────┐  │
│  │EdgeOne   │ │Deployment   │ │OSS Ctrl  │ │ LocalSite │  │
│  │Config    │ │Deploy/Offline│ │          │ │ Public    │  │
│  └──────────┘ └─────────────┘ └──────────┘ └───────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Service layer (@Transactional) + GlobalException    │  │
│  └──────────────────────────────────────────────────────┘  │
└───────┬──────────────────────┬──────────────────────┬──────┘
        ▼                      ▼                      ▼
┌───────────────┐   ┌───────────────────┐   ┌─────────────────┐
│ MySQL 8.0     │   │ Redis             │   │ External Svcs   │
│ 13 tables     │   │ Blacklist / codes │   │ SMTP / OSS /    │
│ (see DB design)│   │ / quotas          │   │ LLM (OpenAI     │
└───────────────┘   └───────────────────┘   │  compatible)    │
                                            │ / EdgeOne CLI   │
                                            └─────────────────┘
```

***

## 📁 Project Structure

```
WorksShow/
├── frontend/                     # Frontend source
│   ├── src/
│   │   ├── api/                  # Axios wrapper + per-module APIs
│   │   │   ├── request.ts        #   Instance + interceptors (token / Result unwrap / 401)
│   │   │   ├── auth.ts           #   Register / login / verify code / password recovery
│   │   │   ├── portfolio.ts      #   Portfolio instance CRUD
│   │   │   ├── userProfile.ts    #   User profile read/write
│   │   │   ├── career.ts         #   Career intention
│   │   │   ├── deployment.ts     #   EdgeOne deployment API
│   │   │   ├── edgeoneConfig.ts  #   EdgeOne config API
│   │   │   ├── oss.ts            #   OSS upload token (extension whitelist mirrored with backend)
│   │   │   └── ai.ts             #   AI chat / AI template generation (SSE streaming)
│   │   ├── components/           #   Shared display components (Hero / About / Skills / Works / Modal, etc.)
│   │   │   ├── ai/               #   AI assistant components
│   │   │   ├── DeployDialog.vue  #   EdgeOne deploy dialog
│   │   │   └── EdgeOneConfigPanel.vue  #   EdgeOne config panel
│   │   ├── composables/          #   Composables
│   │   │   ├── usePortfolioStore.ts  #  Global shared portfolio data + 800ms debounced auto-save
│   │   │   ├── useExporter.ts        #  Export to single-file HTML
│   │   │   └── ...
│   │   ├── editor/               #   Visual editor
│   │   │   ├── EditorView.vue    #   Left form + right preview (with deploy / AI buttons)
│   │   │   └── panels/           #   6 content edit panels
│   │   │       ├── ProfilePanel.vue
│   │   │       ├── WorksPanel.vue
│   │   │       ├── ExperiencesPanel.vue
│   │   │       ├── SkillsPanel.vue
│   │   │       ├── CareerIntentionPanel.vue
│   │   │       └── ThemePanel.vue
│   │   ├── templates/            # ⭐ Template system (10 templates)
│   │   │   ├── mo-yun/           #   Mo Yun: ink-wash oriental × magazine
│   │   │   ├── pastel/           #   Pastel: soft pastels × light modern
│   │   │   ├── aurora/           #   Aurora: deep space × glassmorphism × Bento Grid
│   │   │   ├── dawn/             #   Dawn: warm editorial × ochre orange
│   │   │   ├── brutal/           #   Brutal: brutalism × hard borders × primary colors
│   │   │   ├── gazette/          #   Gazette: classic newspaper × print aesthetics
│   │   │   ├── glass-3d/         #   Glass 3D: pastel sky × glassmorphism × real 3D scene
│   │   │   ├── cube-dossier/     #   Cube Dossier: interactive 3D info cube (pure CSS 3D)
│   │   │   ├── particle-planet/  #   Particle Planet: glowing-particle 3D planet (hand-written Canvas 3D)
│   │   │   └── virus-3d/         #   Virus Infection: cinematic 3D virus lifecycle (hand-written Canvas 3D)
│   │   ├── views/                #   Login / Register / Account management
│   │   ├── App.vue
│   │   ├── main.ts
│   │   └── router/               #   Hash routing + global guards
│   ├── index.html
│   ├── package.json
│   └── vite.config.ts
├── backend/                      # Backend source
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/worksshow/
│       │   ├── WorksShowApplication.java
│       │   ├── common/
│       │   │   ├── Result.java                      # Unified response wrapper
│       │   │   └── AesUtils.java                    # AES utility (EdgeOne API Token encryption)
│       │   ├── config/
│       │   │   ├── WebMvcConfig.java               #   CORS
│       │   │   ├── SecurityConfig.java             #   BCryptPasswordEncoder bean
│       │   │   ├── MybatisPlusConfig.java          #   MyBatis-Plus pagination
│       │   │   ├── SaTokenConfig.java              #   Sa-Token JWT StateLess config + interceptor
│       │   │   ├── StpInterfaceImpl.java           #   Sa-Token permission / role lookup
│       │   │   └── AiProperties.java               #   AI service config (OpenAI-compatible endpoint)
│       │   ├── client/
│       │   │   ├── EdgeOnePagesClient.java         #   EdgeOne Pages CLI client (deployment execution)
│       │   │   └── AiChatClient.java               #   OpenAI-compatible chat client (streaming)
│       │   ├── controller/                         # 11 REST controllers
│       │   │   ├── UserController.java             #   Register / Login / Verify / Password
│       │   │   ├── UserProfileController.java      #   User profile
│       │   │   ├── PortfolioController.java        #   Portfolio instances
│       │   │   ├── CareerIntentionController.java  #   Career intention
│       │   │   ├── UserEdgeoneConfigController.java#   EdgeOne config
│       │   │   ├── CustomDomainController.java     #   Custom domains
│       │   │   ├── DeploymentController.java       #   Deploy / Offline / Delete
│       │   │   ├── AiChatController.java           #   AI chat (SSE)
│       │   │   ├── AiTemplateController.java       #   AI template generation (SSE)
│       │   │   ├── OssController.java              #   OSS upload token
│       │   │   └── LocalSiteController.java        #   Local site public access
│       │   ├── dto/                                # Request / response DTOs
│       │   ├── entity/                             # 13 entities (see DB design)
│       │   ├── exception/                          # Global exception handler
│       │   ├── mapper/                             # MyBatis-Plus mappers
│       │   ├── security/
│       │   │   └── UserContext.java                #   ThreadLocal user context
│       │   └── service/impl/
│       └── resources/
│           ├── application.yml
│           └── sql/
│               ├── schema.sql                       # Main tables
│               └── refresh_token.sql               # Refresh Token table
└── docs/
    ├── TECHNICAL.md                                # Chinese version (this file mirrored)
    └── TECHNICAL.en.md                             # This file
```

***

## 🎯 Key Technical Implementations

### 1. Template Registry (Frontend Template Engine)

Each template lives in its own directory with five standard files:

| File                 | Responsibility                                                |
| -------------------- | ------------------------------------------------------------- |
| `meta.ts`            | Template metadata (id / name / description / author)          |
| `defaultData.ts`     | Default fill data                                             |
| `XxxPreview.vue`     | Preview component rendered inside the editor                  |
| `exportTemplate.ts`  | Function that exports a single-file HTML                      |
| `runtime.ts`         | Native JS that runs inside the exported HTML (modals, 3D, etc.)|

All templates are registered uniformly via `registry.ts`, exposed through four pure functions:

```ts
// src/templates/registry.ts
const registry: Record<string, TemplateEntry> = {
  'mo-yun':  { ...moYunMeta,  getDefaultData, PreviewComponent, exportHtml },
  'virus-3d': { ... },
  // Adding a new template = one line here
}

export function getTemplateMeta(id) / getTemplateDefaultData(id)
export function getTemplatePreviewComponent(id) / getTemplateExporter(id)
```

Adding a template = new directory + one line in the registry. **Zero intrusiveness**. There are currently **10 templates**; three of them are hand-written 3D with Canvas/CSS only (zero deps, 100% offline after export).

### 2. Content / Template Decoupled Data Model

This is the most significant architectural decision in the project. An early version stored resume content (profile / work / experience / skill) under child tables of `portfolio`, forcing the same user to re-enter content when switching templates. After refactoring:

```
portfolio table      →  only records "which template is active for a user"
                        (user_id + template_id + name)
user_profile table   →  user-level personal info (1:1, shared across all templates)
user_work table      →  user-level works (shared, including video URLs)
user_experience table →  user-level experiences (work + education unified, `type` field discriminates)
user_skill table     →  user-level skills (grouped by category, JSON column)
```

**Benefits**: fill once, reuse everywhere; zero-cost template switching; strong data consistency. The original migration SQL (from `portfolio_*` child tables into `user_*`) is preserved in `schema.sql`.

### 3. Single-File HTML Export

The `exportTemplate.ts` flow triggered by the export button:

```
1. Create an offscreen <div> (position:fixed; left:-99999px)
2. createApp(PreviewComponent, { data }).mount(host)   ← Vue renders the full page
3. createApp(WorkModal).mount() for each work modal   (initially display:none)
4. host.innerHTML → serialized DOM
5. collectStyles()  → gather every <style> in the main doc (incl. scoped styles)
6. buildHtml()      → <!DOCTYPE html> + <style> + body + <script>runtime</script>
7. Blob + a.click() → trigger download
```

The final artifact is **a single zero-dependency HTML file** (3D templates include hand-written Canvas/CSS 3D runtime). Drop it into any browser, or deploy it to GitHub Pages / Vercel / any static host.

### 4. Sa-Token Auth and Security Interceptor

The project uses **Sa-Token JWT StateLess** (stateless) mode. The core config is in [SaTokenConfig.java](../backend/src/main/java/com/worksshow/config/SaTokenConfig.java):

```
Request →  SaInterceptor.handle()
   ├─ StpUtil.checkLogin()          ← JWT signature + expiry check; throws NotLoginException on failure
   ├─ StpUtil.getLoginIdAsLong()    ← Extract userId from JWT payload (stateless — no session lookup)
   ├─ userService.getById(userId)   ← Hit DB to confirm current user state
   │    (logical delete / account-disable take effect in real time, because
   │     getById is filtered by @TableLogic)
   └─ User missing or status=0 → StpUtil.logout() + throw exception
```

**Key design points**:
- **JWT StateLess mode**: token is self-contained with userId and nickname; server keeps no session
- **Real-time status check**: within a token's lifetime, if the user is disabled or logically deleted, the DB check returns null and access is denied immediately — no need to wait for token expiry
- **Permission extension**: [StpInterfaceImpl.java](../backend/src/main/java/com/worksshow/config/StpInterfaceImpl.java) is a stub for permission / role lookup, ready to pair with `@SaCheckRole` / `@SaCheckPermission`

### 5. Refresh Token and Token Blacklist

To improve security, a Refresh Token mechanism shortens the access-token lifetime and narrows the leakage window:

```
Login flow:
POST /login → returns access_token + refresh_token (7-day validity)
            → refresh_token persisted to the refresh_token DB table

Refresh flow:
POST /refresh-token → validate refresh_token (not expired, not blacklisted)
              → issue new access_token + new refresh_token (rolling refresh)
              → old refresh_token blacklisted; new one saved

Logout flow:
POST /logout → current access_token blacklisted (TTL matches access-token lifetime)
             → delete all refresh_tokens for this user (breaks the rolling-refresh chain)
```

**Key design points**:
- **Blacklist**: `TokenBlacklistServiceImpl` stores invalidated tokens in Redis; the interceptor additionally checks the blacklist after JWT validation. Note: a stateless JWT cannot enumerate tokens issued on other devices for the same user — on password reset we can only blacklist the current request's token and delete all refresh tokens. Other devices' access tokens will naturally expire within their lifetime.
- **Rolling refresh**: each refresh issues a new refresh_token and invalidates the old one, preventing long-lived abuse if a token leaks.
- **Scheduled cleanup**: expired refresh_token rows are purged periodically.

### 6. Email Verification Code Security (`EmailCodeServiceImpl`)

| Aspect            | Implementation                                                                 |
| ----------------- | ------------------------------------------------------------------------------ |
| Randomness        | `SecureRandom` → 6-digit number; not predictable                               |
| Validity          | 5 minutes; Redis TTL auto-expires — no scheduled cleanup needed                |
| Rate limit        | 60-second resend throttle; returns remaining seconds                           |
| One-time use      | `DEL` atomic delete on success; `HINCRBY` atomic attempt counter on failure   |
| Anti-enumeration  | Silently skip if email already registered (sign-up) or not registered (recovery)|
| Transactionality  | Register method is `@Transactional`: `save(user)` then `verifyCode`, rollback on failure |
| Redis-backed      | Verification codes stored in Redis Hash, sharing the same Redis with the token blacklist — multi-instance deploy friendly |

### 7. Global Exception Handling and Unified Response

Every endpoint returns `Result<T>` (`code` / `message` / `data`). `GlobalExceptionHandler` catches:

| Exception                         | Code | Meaning                                             |
| --------------------------------- | ---- | --------------------------------------------------- |
| `BusinessException`               | custom | Anticipated business error                        |
| `MethodArgumentNotValidException` | 400  | `@Valid` parameter failure; returns field-level errors |
| `DuplicateKeyException`           | 409  | Unique-index fallback (concurrent registration race)|
| `HttpMessageNotReadableException` | 400  | JSON parse failure                                 |
| `Exception`                       | 500  | Catch-all; hides the stack trace                    |

### 8. Frontend Auto-Save (`usePortfolioStore`)

- Global singleton `reactive` data — all templates share the same profile
- `watch(data, ..., { deep: true })` + 800ms debounce → `saveToServer`
- **Changes during save are not lost**: when `saving=true`, new mutations flip `pendingSave=true`; the `finally` block checks and reschedules
- **Load-failure guard**: when `loadError=true`, saving is skipped — we never overwrite server data with defaults
- Manual save button bypasses the debounce

### 9. OSS Presigned Direct Upload (Images / Videos)

Work cover images and videos use **Alibaba Cloud OSS with a backend-signed presigned PUT URL** (AK/SK never leaves the backend):

```
Frontend                     Backend                          OSS
 │ POST /oss/upload-token     │                                │
 │───────────────────────────▶│ validate extension whitelist   │
 │                            │ + daily quota                  │
 │                            │ generate objectKey             │
 │                            │ uploads/{userId}/{type}/{uuid}.{ext}
 │ ◀── uploadUrl / publicUrl / contentType ───────────────────│
 │ PUT uploadUrl (raw XHR, with ContentType, progress bar)     │
 │────────────────────────────────────────────────────────────▶│
```

**Key design points**:
- **objectKey generated server-side**: UUID prevents enumeration; `userId/` isolates per-user directories
- **Presigned URL MUST include Content-Type**: browsers always send `Content-Type` on File PUTs; the backend derives MIME from the extension whitelist and returns `contentType` — the frontend must send it verbatim, otherwise 403 `SignatureDoesNotMatch`
- **Extension whitelist**: images jpg/jpeg/png/webp/gif ≤10MB; videos mp4/webm ≤200MB (size is frontend-only). Whitelist is kept in sync between backend `OssServiceImpl` and frontend `oss.ts`
- **Daily quota**: Redis key `oss:upload-limit:{userId}:{yyyyMMdd}` (default 200 / day)
- Templates render `<video>` when a work has `video_url`, otherwise `<img>` (all 10 templates adapted)

### 10. AI Integration (Chat Polishing + Template Generation, SSE Streaming)

The backend connects to **any OpenAI-compatible chat/completions endpoint** (default: Alibaba Bailian DashScope compatible mode, model qwen-plus). See [AiChatClient.java](../backend/src/main/java/com/worksshow/client/AiChatClient.java):

- **AI Chat assistant** (`POST /ai/chat`): backend injects the user's resume content into a system prompt; multi-turn conversation context is persisted to the `ai_message` table (linked per portfolio). The response is forwarded token-by-token via **SSE (`text/event-stream`)** for a typewriter effect in the frontend
- **AI Template generation** (`POST /ai/template/generate`): users describe a style in natural language; the AI streams a custom template implementation to the frontend. Artifacts are stored in the `ai_template` table — list, detail, delete are all supported
- **Daily quota**: Redis counters (default 30 chat requests / 5 template generations per day); over-quota returns a business exception
- **Timeout**: LLM request timeout is configurable (default 120s); SSE connections close gracefully on error

### 11. Local Sites and One-Click EdgeOne Deployment

**Two deployment channels**:

| Channel        | Implementation                                                                                                        | Use case     |
| -------------- | --------------------------------------------------------------------------------------------------------------------- | ------------ |
| EdgeOne Pages  | Backend `ProcessBuilder` invokes the official CLI: `edgeone pages deploy <dir> -t <token> -n <project>`. CLI runs synchronously; stdout is parsed for the project ID and public URL | Public site  |
| Local site     | Exported HTML is written to `./local-sites/{key}`; `GET /api/sites/{key}` serves it publicly (no auth)                  | Intranet preview |

**Key design points**:
- EdgeOne model: **one project = one site = one domain**. Each portfolio deploys as its own EdgeOne project
- API Token is stored with **AES symmetric encryption** (key from env var); never returned to the frontend (only a masked display string)
- EdgeOne Pages does not expose a public HTTP REST deploy API — the CLI is the only official deployment method. The server must have Node.js ≥ 18 and edgeone CLI ≥ 1.2.30 pre-installed
- Deployments support **offline** (`POST /deployment/{id}/offline`) and deletion

***

## 🗄️ Database Design

13 tables, utf8mb4 charset. JSON columns store nested arrays (stats / socials / tags / items / desired industries / desired cities).

| Table                  | Purpose                | Key design notes                                                                          |
| ---------------------- | ---------------------- | ----------------------------------------------------------------------------------------- |
| `user`                 | Users                  | BCrypt password; composite unique index `(email, deleted)` / `(phone, deleted)` to re-register after logical delete |
| `user_profile`         | User resume profile (1:1) | Lazy-created; JSON columns for stats/socials                                           |
| `portfolio`            | Portfolio instances    | Only `user_id + template_id + name`. Content lives in `user_*` tables                      |
| `user_work`            | User works             | JSON tags column; `highlight` flag for flagship works; `sort_order`; `video_url`          |
| `user_experience`      | User experiences       | `type` column discriminates work / education, unified storage                              |
| `user_skill`           | User skills            | Grouped by category, JSON column for items                                                |
| `career_intention`     | Career intention (1:1) | Desired industries / cities as JSON arrays; salary range                                   |
| `user_edgeone_config`  | EdgeOne config (1:1)   | API Token stored AES-encrypted; never returned to frontend; project name stored plaintext |
| `custom_domain`        | Custom domains (1:N)   | Reusable — one domain can be linked to multiple deployments                               |
| `deployment`           | Deployment records (1:N) | Status, public URL, error; linked to portfolio + custom domain; waits for CLI completion |
| `refresh_token`        | Refresh tokens (1:N)   | UUID; 7-day expiry; enables rolling access-token refresh                                   |
| `ai_message`           | AI chat messages (1:N) | Linked to portfolio; persists multi-turn context; supports fetch and clear                 |
| `ai_template`          | AI-generated templates (1:N) | Custom template artifacts generated from natural language; list / detail / delete        |

Initialization scripts:
- [backend/src/main/resources/sql/schema.sql](../backend/src/main/resources/sql/schema.sql) (main tables)
- [backend/src/main/resources/sql/refresh_token.sql](../backend/src/main/resources/sql/refresh_token.sql) (Refresh Token table)

Both use `CREATE TABLE IF NOT EXISTS`, so they are idempotent.

***

## 🔌 API Reference

All endpoints are prefixed with `/api`. Every response wraps in `Result<T>`. Auth is **Sa-Token JWT StateLess**.

### User `/api/user`

| Method | Path                          | Auth   | Description                                                    |
| ------ | ----------------------------- | ------ | -------------------------------------------------------------- |
| POST   | `/sendCode`                   | Public | Send email verification code (silently skip if already registered) |
| POST   | `/register`                   | Public | Register (transaction: save user → verify code; rollback on fail) |
| POST   | `/login`                      | Public | Login with phone or email; returns access_token + refresh_token |
| GET    | `/info`                       | Login  | Current user info                                              |
| PUT    | `/info`                       | Login  | Update nickname / gender / avatar                              |
| PUT    | `/password`                   | Login  | Change password                                                |
| POST   | `/forgot-password/send-code`  | Public | Send password-reset verification code                          |
| POST   | `/forgot-password/reset`      | Public | Reset password via email code                                  |
| POST   | `/refresh-token`              | Public | Exchange refresh_token for a new access_token (+ new refresh_token, rolling) |
| POST   | `/logout`                     | Login  | Logout — blacklist access_token; delete all refresh_tokens      |

### User Profile `/api/user-profile`

| Method | Path | Description                                                       |
| ------ | ---- | ----------------------------------------------------------------- |
| GET    | `/`  | Get profile (returns empty object if none exists — frontend shows empty form) |
| PUT    | `/`  | Save profile (lazy create + full replace; transaction-guaranteed atomic across tables) |

### Portfolio Instance `/api/portfolio`

| Method | Path      | Description                                                                  |
| ------ | --------- | ---------------------------------------------------------------------------- |
| GET    | `/list`   | Current user's portfolio instance list                                       |
| GET    | `/{id}`   | Portfolio detail (instance metadata + user profile content)                   |
| POST   | `/`       | Create instance. Dedup by template_id; returns existing one if present        |
| PUT    | `/{id}`   | Update — only `name` is editable; `template_id` is immutable                  |
| DELETE | `/{id}`   | Logical delete. User profile data is NOT affected                             |

### Career Intention `/api/career-intention`

| Method | Path | Description                  |
| ------ | ---- | ---------------------------- |
| GET    | `/`  | Get career intention         |
| PUT    | `/`  | Save / update (upsert)       |

### AI Chat `/api/ai`

| Method | Path       | Description                                              |
| ------ | ---------- | -------------------------------------------------------- |
| POST   | `/chat`    | AI chat (SSE stream; resume context injected; daily quota)|
| GET    | `/messages` | Fetch chat history for a given portfolioId               |
| DELETE | `/messages` | Clear chat history for a given portfolioId               |

### AI Template `/api/ai/template`

| Method | Path       | Description                                                  |
| ------ | ---------- | ------------------------------------------------------------ |
| POST   | `/generate` | Generate a custom template from natural language (SSE, daily quota) |
| GET    | `/list`    | Current user's AI template list                              |
| GET    | `/{id}`    | AI template detail                                           |
| DELETE | `/{id}`    | Delete an AI template                                        |

### OSS Upload `/api/oss`

| Method | Path             | Description                                                                 |
| ------ | ---------------- | --------------------------------------------------------------------------- |
| POST   | `/upload-token`  | Get presigned PUT credentials: uploadUrl / objectKey / publicUrl / contentType; daily quota |

### EdgeOne Config `/api/edgeone-config`

| Method | Path  | Description                                                       |
| ------ | ----- | ----------------------------------------------------------------- |
| GET    | `/`   | Get current user's config (masked; API Token never returned)      |
| POST   | `/`   | Save / update config (API Token is AES-encrypted before storage)  |
| DELETE | `/`   | Delete config (logical delete)                                    |

### Custom Domain `/api/custom-domain`

| Method | Path      | Description                                                   |
| ------ | --------- | ------------------------------------------------------------- |
| GET    | `/list`   | Current user's custom domain list                             |
| POST   | `/`       | Add domain                                                    |
| PUT    | `/{id}`   | Update domain (notes only)                                    |
| DELETE | `/{id}`   | Delete domain (logical; deployed pages are not affected)      |

### Deployment `/api/deployment`

| Method | Path              | Description                                                                         |
| ------ | ----------------- | ----------------------------------------------------------------------------------- |
| POST   | `/`               | Deploy portfolio to EdgeOne Pages. Takes rendered HTML; waits synchronously for CLI |
| GET    | `/list`           | Current user's deployment list                                                       |
| GET    | `/{id}`           | Deployment detail (status, public URL, error message)                                |
| POST   | `/{id}/offline`   | Offline the deployed site                                                            |
| DELETE | `/{id}`           | Delete deployment record                                                            |

### Local Site `/api/sites`

| Method | Path      | Description                                                               |
| ------ | --------- | ------------------------------------------------------------------------- |
| GET    | `/{key}`  | Publicly access a locally deployed HTML site (no auth required)          |

***

## 🔐 Environment Variables

**Production must inject sensitive config through environment variables** (local dev can use fallback values in `application-dev.yml`):

| Variable                          | Required | Description                                                                               |
| --------------------------------- | -------- | ----------------------------------------------------------------------------------------- |
| `DB_USERNAME` / `DB_PASSWORD`     | ✅       | Database credentials                                                                      |
| `JWT_SECRET`                      | ✅       | Sa-Token JWT signing secret (strong random Base64)                                        |
| `EDGEONE_ENCRYPT_KEY`             | ✅       | AES encryption key for EdgeOne API Token                                                  |
| `OSS_ACCESS_KEY_ID` / `OSS_ACCESS_KEY_SECRET` | ✅ | Alibaba OSS AK/SK — empty value causes startup failure (fail-fast, no partial startup)    |
| `AI_API_KEY`                      | ✅ (when AI is used) | API Key for the OpenAI-compatible endpoint                                          |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | - | Redis connection (defaults to `localhost:6379`)                                    |
| `AI_BASE_URL` / `AI_MODEL`        | -        | LLM endpoint URL and model (default: Bailian qwen-plus)                                  |
| `AI_DAILY_LIMIT` / `AI_TEMPLATE_DAILY_LIMIT` | - | Daily quotas for AI chat / template generation (defaults: 30 / 5)                   |
| `OSS_ENDPOINT` / `OSS_BUCKET` / `OSS_DAILY_LIMIT` | - | OSS endpoint / bucket (public-read default) / daily upload quota (default 200)         |
| `LOCAL_DEPLOY_DIR` / `LOCAL_DEPLOY_BASE_URL` | - | Local-site disk directory (default `./local-sites`) and public base URL                      |
| SMTP                              | ✅       | host / username / auth code (see `application.yml` comments for QQ Mail example)          |

> Note: `application.yml` is already in `.gitignore` to prevent credential leakage.

***

## 📦 Build & Deploy

### Frontend

```bash
cd frontend
npm run build     # vue-tsc type check + Vite bundle → frontend/dist/
npm run preview   # Preview production build locally
npm run check     # Type check only
```

Deployment: upload `frontend/dist/` to any static host (Nginx / Vercel / Netlify / GitHub Pages) and reverse-proxy `/api` to the backend.

### Backend

```bash
cd backend
mvn clean package -DskipTests
java -jar target/worksshow-backend-0.0.1-SNAPSHOT.jar
```

### Server Prerequisites for EdgeOne One-Click Deploy

- Node.js ≥ 18
- edgeone CLI ≥ 1.2.30 (`npm install -g edgeone`)
- Once the user configures their EdgeOne API Token in "Account → Deployment Config", the "Deploy" button in the editor publishes with a single click

***

## 📝 Quick Command Reference

```bash
# Frontend (inside frontend/)
cd frontend
npm install          # Install deps
npm run dev          # Dev server (http://localhost:5173)
npm run build        # Type check + production build → frontend/dist/
npm run preview      # Preview production build
npm run check        # Type check only

# Backend (inside backend/)
cd backend
mvn spring-boot:run           # Dev server (http://localhost:8080/api)
mvn clean package             # Package
mvn test                      # Run tests

# Database initialization
mysql -u root -p < backend/src/main/resources/sql/schema.sql
mysql -u root -p < backend/src/main/resources/sql/refresh_token.sql
```

***

## 📚 Related Documentation

- [README.md](../README.md) (Product overview, English — default)
- [README.zh-CN.md](../README.zh-CN.md) (Product overview, 中文)
- [TECHNICAL.md](./TECHNICAL.md) (Technical docs, 中文)

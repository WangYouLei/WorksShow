---
name: resume-site-deployer
description: 将用户简历生成为静态个人网站（单文件 HTML）并部署到 EdgeOne Pages。当用户提供简历（PDF/DOCX/TXT/MD）并希望制作个人主页、简历网站、作品集页面，或要求"把简历变成网页并上线"时使用。
---

# 简历 → 个人网站 → EdgeOne 部署

把一份简历转换成一个自包含的静态个人网站（单文件 HTML），用户确认后部署到 EdgeOne Pages。

## 工作流程

严格按以下 6 步顺序执行，标粗的环节必须有用户参与，不可跳过。

### 第 1 步：收集简历内容

- 用户提供文件时先读取内容；PDF/DOCX 需先提取文本（可借助命令行工具，提取失败时请用户直接粘贴文本）。
- 扫描件（无文本层）：明确告知无法提取，请用户粘贴简历文字内容。
- 没有文件：请用户口述或粘贴。
- MVP 阶段忽略简历中的照片/证书图片等非文字内容，只处理文字信息。

### 第 2 步：结构化提取（必须参照 assets/data-schema.md）

- 严格按 `assets/data-schema.md` 的字段规范输出 JSON，字段名不可增改。
- 提取完成后把结果整理成可读形式展示给用户，请用户确认或修正。
- **未获用户确认不得进入下一步。**
- 字段填写示例见 `assets/sample-data.json`。

### 第 3 步：选择模板

- 读取 `templates/manifest.json`，向用户介绍全部模板（名称 + 风格 + 适用场景）。
- 用户未明确选择时，根据简历内容与目标岗位推荐一套并说明理由，仍需用户点头。

### 第 4 步：生成站点

1. 完整读取 `templates/<模板id>/index.html`。
2. 只替换其中 `<script id="portfolio-data" type="application/json">...</script>` 标签内的 JSON 为用户确认后的数据。**不要改动模板的 CSS、HTML 结构与 runtime 脚本。**
3. 写入 `output/<项目名>/index.html`。项目名用英文小写加连字符（如 `zhang-san-portfolio`），同时它也是默认的 EdgeOne 项目名。
4. 替换后的 JSON 必须合法：无注释、无尾逗号、字符串内的双引号已转义。

### 第 5 步：本地预览确认

- 在 `output/` 目录启动本地静态服务（如 `python -m http.server 8080` 或 `npx serve`），让用户在浏览器打开预览。
- 用户要求修改时：内容问题只改 JSON 数据块后重新生成；风格问题回到第 3 步换模板。
- **用户明确表示满意后才可部署。**

### 第 6 步：部署到 EdgeOne Pages

按目标选路径：**长期稳定的正式站点 → 路径 A（CLI）**；**快速分享预览、用户暂无 API Token → 路径 B（MCP）**。两者都不可用才提示用户补环境。

**路径 A（正式站点推荐）：edgeone CLI + 部署脚本**

前置条件（缺失先处理）：

- Node.js ≥ 18（`node -v` 检查）
- edgeone CLI ≥ 1.2.30（未安装则 `npm install -g edgeone`）
- API Token：优先 `-Token` 参数，其次环境变量 `EDGEONE_API_TOKEN`，都无则提示用户提供（腾讯云 EdgeOne 控制台创建，只需要 API Token 单个凭证，不是 SecretId/SecretKey 对）

执行（Windows PowerShell）：

```powershell
./scripts/deploy.ps1 -SiteDir ./output/<项目名> -ProjectName <项目名>
```

macOS/Linux 使用 `./scripts/deploy.sh`，参数一致。

**路径 B：EdgeOne Pages MCP（免 Token，出临时分享链接）**

1. 检测当前环境是否可用 MCP 工具 `deploy_html` / `deploy_folder_or_zip`（TRAE 等已接入 EdgeOne Pages MCP 的 Agent 中存在）。
2. 可用则调用 `deploy_folder_or_zip`，入参 `builtFolderPath` 填生成站点的**绝对路径**（`output/<项目名>`），沿用 agent 环境的已授权账号，无需用户提供 Token。
3. 返回结果解读：`type: "temporary"` 的 URL 是**带 token 的临时分享链接**（有时效，用于预览/转发）；`consoleUrl` 指向该项目在腾讯云控制台的页面。
4. 局限：MCP 不支持指定项目名，每次部署会新建随机命名项目；`deploy_html` 仅适合分享单段 HTML，勿用于正式产物。

**部署后的说明（需告知用户）**

- EdgeOne Pages 模型：一个项目 = 一个站点 = 一个域名；CLI 同名项目重复部署即为更新该站点——**更新正式站点 = 修改数据后重新执行第 4~6 步（路径 A 同名重部署）**。
- MCP 产物是临时链接 + 随机项目；要固定域名走路径 A，或到 `consoleUrl` 控制台为项目绑定域名。
- 删除站点与 CDN 缓存刷新不在 MCP/脚本能力范围内，需用户在腾讯云 EdgeOne 控制台操作或走 OpenAPI。
- 成功后把 URL 返回给用户（CLI 输出中的 `EDGEONE_DEPLOY_URL` 或 MCP 临时链接）。

## 硬性约束

- 生成物必须是单文件 HTML：无外部本地依赖、无网络字体请求，CSS/JS 全部内联。
- 不得修改模板的 runtime 逻辑；用户数据只写入 JSON 数据块。
- 简历信息仅用于生成用户本人的个人站点，不得用于其他用途。
- 两个强制确认点：数据确认（第 2 步）与部署确认（第 5 步），缺一不可。

# 简历站点部署到 EdgeOne Pages（Windows PowerShell）
# 用法: ./deploy.ps1 -SiteDir ./output/zhang-san-portfolio -ProjectName zhang-san-portfolio [-Token <api-token>]
param(
    [Parameter(Mandatory = $true)][string]$SiteDir,
    [Parameter(Mandatory = $true)][string]$ProjectName,
    [string]$Token
)

$ErrorActionPreference = 'Stop'

# 1. 解析 Token：参数 > 环境变量
if (-not $Token) { $Token = $env:EDGEONE_API_TOKEN }
if (-not $Token) {
    Write-Error "缺少 EdgeOne API Token。请通过 -Token 参数提供，或设置环境变量 EDGEONE_API_TOKEN。（腾讯云 EdgeOne 控制台创建，仅需 API Token）"
    exit 1
}

# 2. 检查站点目录
if (-not (Test-Path (Join-Path $SiteDir "index.html"))) {
    Write-Error "目录 $SiteDir 下未找到 index.html，请先生成站点。"
    exit 1
}

# 3. 检查 Node.js >= 18
try { $nodeVer = (node -v) } catch {
    Write-Error "未检测到 Node.js，请先安装 Node.js 18+：https://nodejs.org/"
    exit 1
}
$major = [int]($nodeVer.TrimStart('v').Split('.')[0])
if ($major -lt 18) {
    Write-Error "Node.js 版本过低（$nodeVer），EdgeOne CLI 需要 18+。"
    exit 1
}

# 4. 检查 edgeone CLI
try { $cliVer = (edgeone -v) 2>$null } catch {
    Write-Error "未检测到 edgeone CLI，请先执行: npm install -g edgeone"
    exit 1
}
Write-Host "edgeone CLI 版本: $cliVer"

# 5. 部署（同名项目重复部署即为更新）
Write-Host "正在部署 $SiteDir 到 EdgeOne 项目 [$ProjectName] ..."
$raw = edgeone pages deploy $SiteDir -t $Token -n $ProjectName 2>&1 | Out-String
Write-Host $raw

# 6. 提取部署 URL
if ($raw -match 'https?://\S+') {
    Write-Host "`n部署成功！站点地址: $($Matches[0])" -ForegroundColor Green
} else {
    Write-Error "部署输出中未找到 URL，请检查上方日志。"
    exit 1
}

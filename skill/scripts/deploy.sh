#!/usr/bin/env bash
# 简历站点部署到 EdgeOne Pages（macOS/Linux）
# 用法: ./deploy.sh -d ./output/zhang-san-portfolio -p zhang-san-portfolio [-t <api-token>]
set -euo pipefail

SITE_DIR="" PROJECT_NAME="" TOKEN=""
while getopts "d:p:t:" opt; do
  case $opt in
    d) SITE_DIR="$OPTARG" ;;
    p) PROJECT_NAME="$OPTARG" ;;
    t) TOKEN="$OPTARG" ;;
    *) echo "用法: $0 -d <站点目录> -p <项目名> [-t <api-token>]"; exit 1 ;;
  esac
done
[ -z "$SITE_DIR" ] || [ -z "$PROJECT_NAME" ] && { echo "缺少 -d 或 -p 参数"; exit 1; }

# 1. 解析 Token：参数 > 环境变量
[ -z "$TOKEN" ] && TOKEN="${EDGEONE_API_TOKEN:-}"
[ -z "$TOKEN" ] && { echo "缺少 EdgeOne API Token：请通过 -t 参数提供，或设置环境变量 EDGEONE_API_TOKEN。"; exit 1; }

# 2. 检查站点目录
[ -f "$SITE_DIR/index.html" ] || { echo "目录 $SITE_DIR 下未找到 index.html，请先生成站点。"; exit 1; }

# 3. 检查 Node.js >= 18
command -v node >/dev/null || { echo "未检测到 Node.js，请先安装 18+：https://nodejs.org/"; exit 1; }
MAJOR=$(node -v | sed 's/v\([0-9]*\).*/\1/')
[ "$MAJOR" -lt 18 ] && { echo "Node.js 版本过低（$(node -v)），EdgeOne CLI 需要 18+。"; exit 1; }

# 4. 检查 edgeone CLI
command -v edgeone >/dev/null || { echo "未检测到 edgeone CLI，请先执行: npm install -g edgeone"; exit 1; }
echo "edgeone CLI 版本: $(edgeone -v)"

# 5. 部署（同名项目重复部署即为更新）
echo "正在部署 $SITE_DIR 到 EdgeOne 项目 [$PROJECT_NAME] ..."
OUTPUT=$(edgeone pages deploy "$SITE_DIR" -t "$TOKEN" -n "$PROJECT_NAME" 2>&1) || { echo "$OUTPUT"; exit 1; }
echo "$OUTPUT"

# 6. 提取部署 URL
URL=$(echo "$OUTPUT" | grep -oE 'https?://[^[:space:]"'"'"']+' | head -1)
if [ -n "$URL" ]; then
  echo ""
  echo "部署成功！站点地址: $URL"
else
  echo "部署输出中未找到 URL，请检查上方日志。"
  exit 1
fi

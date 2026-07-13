import { createApp, nextTick } from 'vue'
import GazettePreview from './GazettePreview.vue'
import type { PortfolioData } from '../types'
import { RUNTIME_JS } from './runtime'

function escapeAttr(s: string): string {
  return String(s).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;')
}

/** 收集主文档所有 <style>（全局样式 + 各组件 scoped 样式） */
function collectStyles(): string {
  return Array.from(document.querySelectorAll('style'))
    .map((s) => s.textContent || '')
    .join('\n')
}

function buildHtml(data: PortfolioData, mainHtml: string, styles: string): string {
  const title = `${data.profile.name} · 个人作品集`
  // 报章风字体：Playfair Display（衬线大标题）+ Noto Serif SC（中文衬线正文）+ JetBrains Mono（等宽元数据）
  const fontsHref =
    'https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,700;0,900;1,400;1,700&family=Noto+Serif+SC:wght@400;500;700;900&family=JetBrains+Mono:wght@400;500;700&display=swap'
  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>${escapeAttr(title)}</title>
<meta name="description" content="${escapeAttr(data.profile.bio)}">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link rel="stylesheet" href="${fontsHref}">
<style>
${styles}
</style>
</head>
<body>
${mainHtml}
<script>${RUNTIME_JS}<\/script>
</body>
</html>`
}

/**
 * 把报章模板的当前数据导出为单文件 HTML 字符串。
 * GazettePreview 已内联全部作品弹窗（初始隐藏,由 runtime 切换 is-open）,
 * 故只需渲染一次组件,取 DOM 序列化,配合主文档样式表与原生 JS runtime 拼装成独立 HTML。
 */
export async function exportGazetteHtml(data: PortfolioData): Promise<string> {
  const host = document.createElement('div')
  host.style.cssText = 'position:fixed;left:-99999px;top:0;width:1200px;visibility:hidden;'
  document.body.appendChild(host)
  const app = createApp(GazettePreview, { data })
  app.mount(host)
  await nextTick()
  await nextTick()
  const mainHtml = host.innerHTML

  app.unmount()
  host.remove()

  return buildHtml(data, mainHtml, collectStyles())
}

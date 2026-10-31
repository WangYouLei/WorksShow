import { createApp, nextTick } from 'vue'
import PastelPreview from './PastelPreview.vue'
import type { PortfolioData, ThemeConfig } from '../types'
import { RUNTIME_JS } from './runtime'
import { themeToCssText, FONT_LINKS } from '@/composables/useThemeVars'

function escapeAttr(s: string): string {
  return String(s).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;')
}

/** 收集主文档所有 <style>（全局样式 + 各组件 scoped 样式） */
function collectStyles(): string {
  return Array.from(document.querySelectorAll('style'))
    .map((s) => s.textContent || '')
    .join('\n')
}

function buildHtml(data: PortfolioData, mainHtml: string, styles: string, theme?: ThemeConfig): string {
  const title = `${data.profile.name} · 个人作品集`
  const fontsHref = FONT_LINKS[theme?.fontFamily ?? 'sans']
  const themeCss = themeToCssText(theme)
  const themeBlock = themeCss ? `:root {\n${themeCss}\n}\n` : ''
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
${themeBlock}${styles}
</style>
</head>
<body>
${mainHtml}
<script>${RUNTIME_JS}<\/script>
</body>
</html>`
}

/**
 * 把粉彩模板的当前数据导出为单文件 HTML 字符串。
 * PastelPreview 已内联全部作品弹窗（初始隐藏，由 runtime 切换 is-open），
 * 故只需渲染一次组件，取 DOM 序列化，配合主文档样式表与原生 JS runtime 拼装成独立 HTML。
 * 主题通过 :root 注入 --theme-* 变量，模板样式中的 var(..., 默认值) fallback 链自动生效。
 */
export async function exportPastelHtml(data: PortfolioData, theme?: ThemeConfig): Promise<string> {
  const host = document.createElement('div')
  host.style.cssText = 'position:fixed;left:-99999px;top:0;width:1200px;visibility:hidden;'
  document.body.appendChild(host)
  const app = createApp(PastelPreview, { data })
  app.mount(host)
  await nextTick()
  await nextTick()
  const mainHtml = host.innerHTML

  app.unmount()
  host.remove()

  return buildHtml(data, mainHtml, collectStyles(), theme)
}

import { createApp, nextTick } from 'vue'
import ParticlePlanetPreview from './ParticlePlanetPreview.vue'
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
  const fontsHref =
    'https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@300;400;500;700;800;900&family=Manrope:wght@400;500;600;700;800&display=swap'
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
 * 把粒子星球模板的当前数据导出为单文件 HTML 字符串。
 * 画布与面板为静态 DOM，粒子渲染与全部交互由 RUNTIME_JS 内嵌的共享引擎驱动，
 * 与 Vue 预览组件使用同一份引擎源码，保证两端行为一致。
 */
export async function exportParticlePlanetHtml(data: PortfolioData): Promise<string> {
  const host = document.createElement('div')
  host.style.cssText = 'position:fixed;left:-99999px;top:0;width:1200px;height:900px;visibility:hidden;'
  document.body.appendChild(host)
  const app = createApp(ParticlePlanetPreview, { data })
  app.mount(host)
  await nextTick()
  await nextTick()
  const mainHtml = host.innerHTML

  app.unmount()
  host.remove()

  return buildHtml(data, mainHtml, collectStyles())
}

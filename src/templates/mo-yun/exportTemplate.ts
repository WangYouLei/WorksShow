import { createApp, nextTick } from 'vue'
import MoYunPreview from './MoYunPreview.vue'
import WorkModal from '@/components/WorkModal.vue'
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

function buildHtml(data: PortfolioData, mainHtml: string, modalsHtml: string, styles: string): string {
  const title = `${data.profile.name} · 个人作品集`
  const fontsHref =
    'https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;500;700;900&family=Noto+Sans+SC:wght@300;400;500;700&family=Cormorant+Garamond:ital,wght@0,400;0,500;1,400&family=JetBrains+Mono:wght@400;500&display=swap'
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
${modalsHtml}
<script>${RUNTIME_JS}<\/script>
</body>
</html>`
}

/**
 * 把墨韵模板的当前数据导出为单文件 HTML 字符串。
 * 做法：在隐藏容器里用 Vue 渲染完整页面 + 逐个作品模态框，
 * 取 DOM 序列化，配合主文档样式表与一段原生 JS runtime 拼装成独立 HTML。
 */
export async function exportMoYunHtml(data: PortfolioData): Promise<string> {
  // 1. 渲染主页面到隐藏容器
  const host = document.createElement('div')
  host.style.cssText = 'position:fixed;left:-99999px;top:0;width:1200px;visibility:hidden;'
  document.body.appendChild(host)
  const app = createApp(MoYunPreview, { data })
  app.mount(host)
  await nextTick()
  await nextTick()
  const mainHtml = host.innerHTML

  // 2. 逐个渲染作品模态框（每个 work 一个，初始隐藏，由 runtime 切换）
  const modalsHtml: string[] = []
  for (const w of data.works) {
    const mHost = document.createElement('div')
    host.appendChild(mHost)
    const mApp = createApp(WorkModal, { work: w })
    mApp.mount(mHost)
    await nextTick()
    const html = mHost.innerHTML
    mApp.unmount()
    mHost.remove()
    modalsHtml.push(
      `<div class="export-modal" data-modal-id="${escapeAttr(w.id)}" style="display:none">${html}</div>`,
    )
  }

  app.unmount()
  host.remove()

  return buildHtml(data, mainHtml, modalsHtml.join('\n'), collectStyles())
}

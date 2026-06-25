import { exportMoYunHtml } from '@/templates/mo-yun/exportTemplate'
import type { PortfolioData } from '@/templates/types'

/** 把当前作品集数据导出为单文件 HTML 并触发浏览器下载 */
export async function downloadPortfolioHtml(data: PortfolioData, filename?: string): Promise<void> {
  const html = await exportMoYunHtml(data)
  const blob = new Blob([html], { type: 'text/html;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename || `${data.profile.name || '个人作品集'}.html`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

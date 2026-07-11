import { getTemplateExporter } from '@/templates/registry'
import type { PortfolioData } from '@/templates/types'

/** 把当前作品集数据导出为单文件 HTML 并触发浏览器下载 */
export async function downloadPortfolioHtml(
  data: PortfolioData,
  templateId: string,
  filename?: string,
): Promise<void> {
  const exporter = getTemplateExporter(templateId)
  if (!exporter) throw new Error(`模板 ${templateId} 未注册导出函数`)
  const html = await exporter(data)
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

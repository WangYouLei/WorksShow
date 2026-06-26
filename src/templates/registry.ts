import type { Component } from 'vue'
import type { PortfolioData, TemplateMeta } from './types'
import { moYunMeta } from './mo-yun/meta'
import { getMoYunDefaultData } from './mo-yun/defaultData'
import { exportMoYunHtml } from './mo-yun/exportTemplate'
import MoYunPreview from './mo-yun/MoYunPreview.vue'
import { pastelMeta } from './pastel/meta'
import { getPastelDefaultData } from './pastel/defaultData'
import { exportPastelHtml } from './pastel/exportTemplate'
import PastelPreview from './pastel/PastelPreview.vue'
import { auroraMeta } from './aurora/meta'
import { getAuroraDefaultData } from './aurora/defaultData'
import { exportAuroraHtml } from './aurora/exportTemplate'
import AuroraPreview from './aurora/AuroraPreview.vue'

/** 模板完整注册项（含默认数据获取器、预览组件、导出函数） */
interface TemplateEntry extends TemplateMeta {
  getDefaultData: () => PortfolioData
  PreviewComponent: Component
  exportHtml: (data: PortfolioData) => Promise<string>
}

const registry: Record<string, TemplateEntry> = {
  'mo-yun': {
    ...moYunMeta,
    getDefaultData: getMoYunDefaultData,
    PreviewComponent: MoYunPreview,
    exportHtml: exportMoYunHtml,
  },
  'pastel': {
    ...pastelMeta,
    getDefaultData: getPastelDefaultData,
    PreviewComponent: PastelPreview,
    exportHtml: exportPastelHtml,
  },
  'aurora': {
    ...auroraMeta,
    getDefaultData: getAuroraDefaultData,
    PreviewComponent: AuroraPreview,
    exportHtml: exportAuroraHtml,
  },
}

/** 模板元信息列表（模板选择页使用） */
export const templates: TemplateMeta[] = Object.values(registry).map(
  ({ getDefaultData: _g, PreviewComponent: _p, exportHtml: _e, ...meta }) => meta,
)

export function getTemplateMeta(id: string): TemplateMeta | undefined {
  const entry = registry[id]
  return entry
    ? { id: entry.id, name: entry.name, description: entry.description, cover: entry.cover, author: entry.author }
    : undefined
}

export function getTemplateDefaultData(id: string): PortfolioData | undefined {
  return registry[id]?.getDefaultData()
}

export function getTemplatePreviewComponent(id: string): Component | undefined {
  return registry[id]?.PreviewComponent
}

export function getTemplateExporter(id: string): ((data: PortfolioData) => Promise<string>) | undefined {
  return registry[id]?.exportHtml
}

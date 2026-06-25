import type { PortfolioData, TemplateMeta } from './types'
import { moYunMeta } from './mo-yun/meta'
import { getMoYunDefaultData } from './mo-yun/defaultData'

/** 模板完整注册项（含默认数据获取器） */
interface TemplateEntry extends TemplateMeta {
  getDefaultData: () => PortfolioData
}

const registry: Record<string, TemplateEntry> = {
  'mo-yun': { ...moYunMeta, getDefaultData: getMoYunDefaultData },
}

/** 模板元信息列表（模板选择页使用） */
export const templates: TemplateMeta[] = Object.values(registry).map(
  ({ getDefaultData: _, ...meta }) => meta,
)

export function getTemplateMeta(id: string): TemplateMeta | undefined {
  const entry = registry[id]
  return entry ? { id: entry.id, name: entry.name, description: entry.description, cover: entry.cover, author: entry.author } : undefined
}

export function getTemplateDefaultData(id: string): PortfolioData | undefined {
  return registry[id]?.getDefaultData()
}

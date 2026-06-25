import { reactive, watch } from 'vue'
import type { PortfolioData } from '@/templates/types'
import { getTemplateDefaultData } from '@/templates/registry'

const STORAGE_PREFIX = 'works-show:'
const cache = new Map<string, PortfolioData>()

function clone<T>(v: T): T {
  return JSON.parse(JSON.stringify(v))
}

function load(templateId: string): PortfolioData {
  const key = STORAGE_PREFIX + templateId
  try {
    const raw = localStorage.getItem(key)
    if (raw) return JSON.parse(raw) as PortfolioData
  } catch (e) {
    console.warn('[store] 读取本地数据失败，使用默认数据', e)
  }
  const def = getTemplateDefaultData(templateId)
  if (!def) throw new Error(`模板 ${templateId} 未注册默认数据`)
  return clone(def)
}

/**
 * 获取指定模板的可编辑数据（响应式 + localStorage 自动持久化）。
 * 同一 templateId 多次调用返回同一个 reactive 对象。
 */
export function usePortfolioStore(templateId: string): PortfolioData {
  let data = cache.get(templateId)
  if (!data) {
    data = reactive(load(templateId))
    cache.set(templateId, data)
    watch(
      data,
      (val) => {
        try {
          localStorage.setItem(STORAGE_PREFIX + templateId, JSON.stringify(val))
        } catch (e) {
          console.warn('[store] 保存本地数据失败', e)
        }
      },
      { deep: true },
    )
  }
  return data
}

/** 重置为模板默认数据（保留响应式对象引用） */
export function resetPortfolioStore(templateId: string): PortfolioData {
  const def = getTemplateDefaultData(templateId)
  if (!def) throw new Error(`模板 ${templateId} 未注册默认数据`)
  const fresh = clone(def)
  const existing = cache.get(templateId)
  if (existing) {
    existing.profile = fresh.profile
    existing.works = fresh.works
    existing.experiences = fresh.experiences
    existing.skills = fresh.skills
    return existing
  }
  return usePortfolioStore(templateId)
}

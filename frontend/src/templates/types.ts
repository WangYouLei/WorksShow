import type { Component } from 'vue'
import type { Profile, Work, Experience, Skill } from '@/data/types'

/** 可编辑的作品集数据（编辑器的数据模型） */
export interface PortfolioData {
  profile: Profile
  works: Work[]
  experiences: Experience[]
  skills: Skill[]
}

/** 深拷贝工具函数：避免修改原始数据 */
export function clone<T>(v: T): T {
  return JSON.parse(JSON.stringify(v))
}

/** 默认空数据兜底：当数据源缺失时使用 */
const EMPTY_PROFILE: Profile = {
  name: '',
  nameEn: '',
  title: '',
  tagline: '',
  bio: '',
  email: '',
  location: '',
  availability: '',
  stats: [],
  socials: [],
}

/**
 * 数据校验与兜底：确保 PortfolioData 的所有字段存在且类型正确
 * 防止空 works/experiences/skills 数组或缺失 highlight 字段导致组件异常
 */
export function validatePortfolioData(data: Partial<PortfolioData>): PortfolioData {
  const profile: Profile = { ...EMPTY_PROFILE, ...(data.profile || {}) }
  
  // 确保数组存在，且每个 work 有默认 highlight: false
  const works: Work[] = (data.works || []).map((w) => ({
    id: w?.id || '',
    title: w?.title || '',
    category: w?.category || '',
    cover: w?.cover || '',
    description: w?.description || '',
    year: w?.year || '',
    link: w?.link,
    tags: w?.tags || [],
    highlight: w?.highlight || false,
  }))
  
  // 确保数组存在
  const experiences: Experience[] = (data.experiences || []).map((e) => ({
    id: e?.id || '',
    period: e?.period || '',
    title: e?.title || '',
    organization: e?.organization || '',
    description: e?.description || '',
    type: e?.type || 'work',
  }))
  
  // 确保数组存在
  const skills: Skill[] = (data.skills || []).map((s) => ({
    id: s?.id || '',
    category: s?.category || '',
    categoryEn: s?.categoryEn || '',
    items: s?.items || [],
  }))
  
  return { profile, works, experiences, skills }
}

/** 模板元信息（用于模板选择页展示） */
export interface TemplateMeta {
  id: string
  name: string
  description: string
  cover: string
  author: string
}

/** 完整模板定义：选择、预览、导出均依赖此接口 */
export interface Template extends TemplateMeta {
  /** 编辑器内 iframe 预览用的渲染组件，接收 PortfolioData 作为 prop */
  PreviewComponent: Component
  /** 用户首次进入编辑器时填充的默认数据 */
  defaultData: PortfolioData
  /** 导出为单文件 HTML 字符串（含内联 CSS + 原生 JS runtime） */
  exportHtml: (data: PortfolioData) => string
}

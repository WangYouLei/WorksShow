import type { Component } from 'vue'
import type { Profile, Work, Experience, Skill } from '@/data/types'

/** 可编辑的作品集数据（编辑器的数据模型） */
export interface PortfolioData {
  profile: Profile
  works: Work[]
  experiences: Experience[]
  skills: Skill[]
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

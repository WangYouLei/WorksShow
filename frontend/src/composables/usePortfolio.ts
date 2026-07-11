import { inject, type InjectionKey } from 'vue'
import type { PortfolioData } from '@/templates/types'

/** 可编辑作品集数据的 provide/inject key */
export const PORTFOLIO_KEY: InjectionKey<PortfolioData> = Symbol('portfolio')

/** 在模板渲染组件内取当前可编辑数据；未 provide 时返回 null（fallback 到静态数据） */
export function usePortfolio(): PortfolioData | null {
  return inject(PORTFOLIO_KEY, null)
}

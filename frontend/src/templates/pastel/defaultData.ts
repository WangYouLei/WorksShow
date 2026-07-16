import type { PortfolioData } from '../types'
import { clone, validatePortfolioData } from '../types'
import { profile } from '@/data/profile'
import { works } from '@/data/works'
import { experiences } from '@/data/experiences'
import { skills } from '@/data/skills'

/** 粉彩模板的默认数据：复用 src/data/* 的静态数据，深拷贝避免污染源数据 */
export function getPastelDefaultData(): PortfolioData {
  return validatePortfolioData({
    profile: clone(profile),
    works: clone(works),
    experiences: clone(experiences),
    skills: clone(skills),
  })
}

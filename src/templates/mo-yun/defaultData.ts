import type { PortfolioData } from '../types'
import { profile } from '@/data/profile'
import { works } from '@/data/works'
import { experiences } from '@/data/experiences'
import { skills } from '@/data/skills'

/** 墨韵模板的默认数据：来自 src/data/* 的静态数据，深拷贝避免污染源数据 */
export function getMoYunDefaultData(): PortfolioData {
  return {
    profile: clone(profile),
    works: clone(works),
    experiences: clone(experiences),
    skills: clone(skills),
  }
}

function clone<T>(v: T): T {
  return JSON.parse(JSON.stringify(v))
}

import type { PortfolioData } from '../types'
import { profile } from '@/data/profile'
import { works } from '@/data/works'
import { experiences } from '@/data/experiences'
import { skills } from '@/data/skills'

/** 晨曦模板的默认数据：复用 src/data/* 的静态数据，深拷贝避免污染源数据 */
export function getDawnDefaultData(): PortfolioData {
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

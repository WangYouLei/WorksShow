import request from './request'
import type { Profile, Work, Experience, Skill } from '@/data/types'

/** 简历列表项(主表,不含内容) */
export interface PortfolioListItem {
  id: number
  userId: number
  templateId: string
  name: string
  updateTime: string
  createTime: string
}

/** 简历实例(模板实例,仅含元数据,内容存于用户档案) */
export interface PortfolioInstance {
  id: number
  templateId: string
  name: string
}

/**
 * 简历详情(实例元数据 + 用户档案内容,后端 PortfolioDataDTO)。
 * <p>
 * works/experiences/skills 的 id 为 number(后端主键),
 * 前端使用时需转换为 string(供 v-for key)。
 */
export interface PortfolioDetail extends PortfolioInstance {
  profile: Profile
  works: (Omit<Work, 'id'> & { id: number | null })[]
  experiences: (Omit<Experience, 'id'> & { id: number | null })[]
  skills: (Omit<Skill, 'id'> & { id: number | null })[]
}

/** 创建/更新简历实例的请求体(仅 templateId + name,内容由 /api/user-profile 管理) */
export interface PortfolioRequest {
  templateId: string
  name: string
}

/** 获取当前用户简历实例列表 */
export function listPortfolios() {
  return request.get<PortfolioListItem[]>('/portfolio/list') as unknown as Promise<PortfolioListItem[]>
}

/** 获取简历详情(实例元数据 + 用户档案内容,用于渲染) */
export function getPortfolio(id: number) {
  return request.get<PortfolioDetail>(`/portfolio/${id}`) as unknown as Promise<PortfolioDetail>
}

/** 创建简历实例(仅 templateId + name) */
export function createPortfolio(data: PortfolioRequest) {
  return request.post<PortfolioInstance>('/portfolio', data) as unknown as Promise<PortfolioInstance>
}

/** 更新简历实例(仅 name,templateId 不可变) */
export function updatePortfolio(id: number, data: PortfolioRequest) {
  return request.put<PortfolioInstance>(`/portfolio/${id}`, data) as unknown as Promise<PortfolioInstance>
}

/** 删除简历实例(逻辑删除,用户档案不受影响) */
export function deletePortfolio(id: number) {
  return request.delete<void>(`/portfolio/${id}`) as unknown as Promise<void>
}

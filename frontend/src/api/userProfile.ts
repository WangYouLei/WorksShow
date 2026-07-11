import request from './request'
import type { Profile, Work, Experience, Skill } from '@/data/types'

/**
 * 后端返回的用户档案原始结构(id 为 number)。
 * <p>
 * 对应后端 UserProfileDTO。前端 store 层会将其 normalize 为
 * PortfolioData(id 转为 string,供 v-for key 使用)。
 */
export interface BackendUserProfile {
  profile: Profile
  works: (Omit<Work, 'id'> & { id: number | null })[]
  experiences: (Omit<Experience, 'id'> & { id: number | null })[]
  skills: (Omit<Skill, 'id'> & { id: number | null })[]
}

/**
 * 保存用户档案时的请求体(不含 id,后端整体替换时会重新分配 id)。
 */
export interface UserProfileSaveRequest {
  profile: Profile
  works: Omit<Work, 'id'>[]
  experiences: Omit<Experience, 'id'>[]
  skills: Omit<Skill, 'id'>[]
}

/** 获取当前用户档案(所有简历模板共享) */
export function getUserProfile() {
  return request.get<BackendUserProfile>('/user-profile') as unknown as Promise<BackendUserProfile>
}

/**
 * 保存当前用户档案(整体替换)。
 * <p>
 * works/experiences/skills 不传 id,后端采用"逻辑删除旧 + 批量插入新"
 * 的整体替换策略,会重新分配 id。
 */
export function saveUserProfile(data: UserProfileSaveRequest) {
  return request.put<BackendUserProfile>('/user-profile', data) as unknown as Promise<BackendUserProfile>
}

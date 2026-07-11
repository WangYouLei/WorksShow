import request from './request'

/** 求职意向数据 */
export interface CareerIntention {
  id?: number
  jobStatus: number
  expectedPosition: string
  positionType: string
  expectedIndustry: string[]
  expectedCity: string[]
  workType: number
  salaryMin: number | null
  salaryMax: number | null
  arrivalTime: string
  selfEvaluation: string
}

/** 获取当前用户求职意向 */
export function getCareerIntention() {
  return request.get<CareerIntention>('/career-intention') as unknown as Promise<CareerIntention | null>
}

/** 保存/更新求职意向(upsert) */
export function saveCareerIntention(data: CareerIntention) {
  return request.put<CareerIntention>('/career-intention', data) as unknown as Promise<CareerIntention>
}

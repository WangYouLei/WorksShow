import request from './request'

/** 部署状态:0-部署中 1-成功 2-失败 */
export const DEPLOY_STATUS = {
  DEPLOYING: 0,
  SUCCESS: 1,
  FAILED: 2,
} as const

/** 部署请求(前端渲染好的 HTML + 简历ID + 项目名) */
export interface DeploymentRequest {
  portfolioId: number
  html: string
  projectName?: string
  description?: string
}

/** 部署记录响应 */
export interface DeploymentVO {
  id: number
  portfolioId: number
  templateId: string
  projectName: string
  description: string | null
  status: number
  deployUrl: string | null
  errorMessage: string | null
  createTime: string
  updateTime: string
}

/**
 * 部署简历模板到 EdgeOne Pages
 * <p>
 * 同步部署:后端调用 EdgeOne CLI 完成,可能耗时数十秒,
 * 此请求单独设置 200s 超时(全局默认 15s 不够)。
 */
export function deploy(data: DeploymentRequest) {
  return request.post<DeploymentVO>('/deployment', data, {
    timeout: 200000,
  }) as unknown as Promise<DeploymentVO>
}

/** 获取当前用户部署记录列表 */
export function listDeployments() {
  return request.get<DeploymentVO[]>('/deployment/list') as unknown as Promise<DeploymentVO[]>
}

/** 获取部署详情 */
export function getDeployment(id: number) {
  return request.get<DeploymentVO>(`/deployment/${id}`) as unknown as Promise<DeploymentVO>
}

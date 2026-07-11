import request from './request'

/** EdgeOne 配置响应(脱敏) */
export interface EdgeOneConfigVO {
  id: number
  /** 脱敏后的 API Token(如 c+KH****5678) */
  apiTokenMasked: string
  /** EdgeOne 项目名(CLI -n 参数用) */
  projectName: string | null
  createTime: string
  updateTime: string
}

/** 保存/更新 EdgeOne 配置请求(明文 token,入库前加密) */
export interface EdgeOneConfigRequest {
  apiToken: string
  projectName?: string
}

/** 获取当前用户的 EdgeOne 配置(脱敏),未配置时返回 null */
export function getEdgeOneConfig() {
  return request.get<EdgeOneConfigVO | null>('/edgeone-config') as unknown as Promise<EdgeOneConfigVO | null>
}

/** 保存或更新 EdgeOne 配置(upsert) */
export function saveEdgeOneConfig(data: EdgeOneConfigRequest) {
  return request.post<void>('/edgeone-config', data) as unknown as Promise<void>
}

/** 删除当前用户的 EdgeOne 配置(逻辑删除) */
export function deleteEdgeOneConfig() {
  return request.delete<void>('/edgeone-config') as unknown as Promise<void>
}

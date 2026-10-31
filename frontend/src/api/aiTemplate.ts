import request from './request'

/** AI 生成模板(列表项,不含 html) */
export interface AiTemplateItem {
  id: number
  name: string
  requirement: string | null
  portfolioId: number | null
  createTime: string
}

/** 模板详情(含 html) */
export interface AiTemplateDetail extends AiTemplateItem {
  html: string
}

/** 我的 AI 模板列表 */
export function listAiTemplates(): Promise<AiTemplateItem[]> {
  return request.get('/ai/template/list') as unknown as Promise<AiTemplateItem[]>
}

/** 模板详情(含完整 HTML) */
export function getAiTemplate(id: number): Promise<AiTemplateDetail> {
  return request.get(`/ai/template/${id}`) as unknown as Promise<AiTemplateDetail>
}

/** 删除模板 */
export function deleteAiTemplate(id: number): Promise<void> {
  return request.delete(`/ai/template/${id}`) as unknown as Promise<void>
}

/** 获取部署用简历实例ID(后端懒创建) */
export function ensureAiTemplatePortfolio(id: number): Promise<number> {
  return request.get(`/ai/template/${id}/portfolio`) as unknown as Promise<number>
}

/** 生成回调 */
export interface AiGenerateCallbacks {
  /** HTML 增量(用于进度展示;清洗后的完整产物在结束后经详情接口获取) */
  onDelta: (text: string) => void
}

/**
 * 发起模板生成(SSE 流式)。
 * <p>
 * 事件序列:delta(多次) → done({id,name});失败时服务端发 error 事件后关闭流,
 * 与对话接口一致,业务错误以异常形式抛给调用方。
 */
export async function generateAiTemplate(
  requirement: string,
  name: string | undefined,
  cb: AiGenerateCallbacks,
): Promise<{ id: number; name: string }> {
  const token = localStorage.getItem('works-show:token')
  const resp = await fetch('/api/ai/template/generate', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify({ requirement, name: name || undefined }),
  })
  if (!resp.ok || !resp.body) {
    if (resp.status === 401) {
      throw new Error('登录已失效,请重新登录')
    }
    throw new Error(`请求失败(${resp.status})`)
  }

  const reader = resp.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let done: { id: number; name: string } | null = null
  for (;;) {
    const { done: finished, value } = await reader.read()
    if (finished) break
    buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n')
    let sep: number
    while ((sep = buffer.indexOf('\n\n')) >= 0) {
      const block = buffer.slice(0, sep)
      buffer = buffer.slice(sep + 2)
      const result = handleEvent(block, cb)
      if (result) done = result
    }
  }
  if (!done) {
    throw new Error('生成被中断,请重试')
  }
  return done
}

/** 业务错误异常(error 事件),区别于单事件解析失败 */
class AiGenerateStreamError extends Error {}

/** 解析单个 SSE 事件块;返回 done 事件的负载 */
function handleEvent(block: string, cb: AiGenerateCallbacks): { id: number; name: string } | null {
  let event = 'message'
  const dataLines: string[] = []
  for (const line of block.split('\n')) {
    if (line.startsWith('event:')) event = line.slice(6).trim()
    else if (line.startsWith('data:')) dataLines.push(line.slice(5).trim())
  }
  if (!dataLines.length) return null
  const raw = dataLines.join('\n')
  try {
    if (event === 'delta') {
      const payload = JSON.parse(raw) as { text: string }
      if (payload.text) cb.onDelta(payload.text)
      return null
    }
    if (event === 'done') {
      return JSON.parse(raw) as { id: number; name: string }
    }
    if (event === 'error') {
      const payload = JSON.parse(raw) as { message: string }
      throw new AiGenerateStreamError(payload.message || 'AI 模板生成失败,请稍后重试')
    }
    return null
  } catch (e) {
    // 业务错误原样上抛;仅吞掉单个事件的 JSON 解析失败
    if (e instanceof AiGenerateStreamError) throw e
    return null
  }
}

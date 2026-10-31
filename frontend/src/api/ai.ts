import request from './request'
import type { ThemeConfig } from '@/templates/types'

/** AI 建议的修改动作(后端已做白名单校验) */
export interface AiAction {
  type: 'update_data' | 'update_theme'
  patch: Record<string, unknown>
}

/** 对话消息 */
export interface AiMessage {
  id?: number
  role: 'user' | 'assistant'
  content: string
  actions?: AiAction[]
  createTime?: string
}

/** 获取某份简历的对话历史 */
export function getAiHistory(portfolioId: number): Promise<AiMessage[]> {
  return request.get('/ai/messages', { params: { portfolioId } }) as unknown as Promise<AiMessage[]>
}

/** 清空某份简历的对话历史 */
export function clearAiHistory(portfolioId: number): Promise<void> {
  return request.delete('/ai/messages', { params: { portfolioId } }) as unknown as Promise<void>
}

/** 流式对话回调 */
export interface AiChatCallbacks {
  /** 正文增量 */
  onDelta: (text: string) => void
  /** 动作数组(回复结束时一次性下发) */
  onActions: (actions: AiAction[]) => void
}

/** SSE error 事件对应的业务错误:必须上抛给调用方,区别于单事件解析失败 */
class AiStreamError extends Error {}

/**
 * 发起流式对话(POST /ai/chat,SSE)。
 * <p>
 * 用原生 fetch 而非 axios:axios 在浏览器端不支持流式读取响应体。
 * 事件序列:delta(多次) → actions(一次) → done;
 * 失败时服务端发 error 事件后关闭流。
 */
export async function streamAiChat(
  portfolioId: number,
  message: string,
  theme: ThemeConfig | undefined,
  cb: AiChatCallbacks,
): Promise<void> {
  const token = localStorage.getItem('works-show:token')
  const resp = await fetch('/api/ai/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify({
      portfolioId,
      message,
      theme: theme && Object.keys(theme).length ? theme : undefined,
    }),
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
  for (;;) {
    const { done, value } = await reader.read()
    if (done) break
    // 统一换行符(SSE 事件以空行分隔,Spring 输出 \n,个别代理会转成 \r\n)
    buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n')
    let sep: number
    while ((sep = buffer.indexOf('\n\n')) >= 0) {
      const block = buffer.slice(0, sep)
      buffer = buffer.slice(sep + 2)
      try {
        handleEvent(block, cb)
      } catch (e) {
        // 业务错误(error 事件)必须上抛,仅吞掉单个事件的解析失败
        if (e instanceof AiStreamError) throw e
      }
    }
  }
}

/** 解析单个 SSE 事件块(event: 行 + data: 行) */
function handleEvent(block: string, cb: AiChatCallbacks): void {
  let event = 'message'
  const dataLines: string[] = []
  for (const line of block.split('\n')) {
    if (line.startsWith('event:')) event = line.slice(6).trim()
    else if (line.startsWith('data:')) dataLines.push(line.slice(5).trim())
  }
  if (!dataLines.length) return
  const raw = dataLines.join('\n')
  if (event === 'delta') {
    const payload = JSON.parse(raw) as { text: string }
    if (payload.text) cb.onDelta(payload.text)
  } else if (event === 'actions') {
    cb.onActions(JSON.parse(raw) as AiAction[])
  } else if (event === 'error') {
    const payload = JSON.parse(raw) as { message: string }
    throw new AiStreamError(payload.message || 'AI 服务繁忙,请稍后重试')
  }
  // done 事件无需回调:reader.read() 返回 done 后 Promise 自然结束
}

import axios from 'axios'
import router from '@/router'

/**
 * Axios 请求实例
 * <p>
 * 统一配置 baseURL、超时、JWT 自动携带。
 * 响应拦截器自动解包 Result,业务层直接拿到 data。
 * access token 过期(401)时自动用 refresh token 换取新 token 并重发原请求,
 * refresh 失败才清除登录态跳登录(滚动刷新,用户无感续期)。
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器:自动携带 token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('works-show:token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// refresh 状态:并发 401 时只触发一次刷新,其余请求排队等待新 token
let isRefreshing = false
let pendingRequests: Array<{
  resolve: (token: string) => void
  reject: (error: Error) => void
}> = []
// 记录已重试过的请求 config,避免刷新成功后仍 401 时无限重试
const retriedConfigs = new WeakSet<object>()

/** 清除本地登录态并跳转登录页 */
function clearAuthAndRedirect() {
  localStorage.removeItem('works-show:token')
  localStorage.removeItem('works-show:refreshToken')
  localStorage.removeItem('works-show:user')
  // 会话失效:重置全局简历 store 单例,换账号登录后避免读到上一用户的档案
  // (动态 import 避免 request <- usePortfolioStore 的静态循环依赖)
  import('@/composables/usePortfolioStore').then((m) => m.destroyPortfolioStore()).catch(() => {})
  const currentPath = router.currentRoute.value.fullPath
  if (router.currentRoute.value.name !== 'login') {
    router.push({ path: '/login', query: { redirect: currentPath } }).catch(() => {})
  }
}

/**
 * 用 refresh token 换取新的 access token + refresh token(滚动刷新)。
 * 使用裸 axios 调用,避免走本实例的 401 拦截造成死循环。
 * @return 新的 access token
 */
async function doRefresh(): Promise<string> {
  const refreshToken = localStorage.getItem('works-show:refreshToken')
  if (!refreshToken) {
    throw new Error('无 refresh token')
  }
  const resp = await axios.post('/api/user/refresh-token', { refreshToken }, { timeout: 15000 })
  const result = resp.data
  if (!result || result.code !== 200) {
    throw new Error(result?.message || '刷新失败')
  }
  const { token, refreshToken: newRefreshToken } = result.data
  localStorage.setItem('works-show:token', token)
  localStorage.setItem('works-show:refreshToken', newRefreshToken)
  return token
}

// 响应拦截器:自动解包 Result;401 时尝试 refresh 后重发,刷新失败才跳登录
request.interceptors.response.use(
  (response) => {
    const result = response.data
    // 后端统一返回 { code, message, data }
    if (result && typeof result.code === 'number') {
      if (result.code === 200) {
        return result.data
      }
      // 业务错误,抛出可读消息
      return Promise.reject(new Error(result.message || '请求失败'))
    }
    // 非标准 Result(如文件下载),原样返回
    return response.data
  },
  async (error) => {
    const originalConfig = error.config
    // 401 且该请求未重试过:尝试用 refresh token 换新 token 后重发
    if (error.response?.status === 401 && originalConfig && !retriedConfigs.has(originalConfig)) {
      retriedConfigs.add(originalConfig)

      // 已有刷新在进行中:排队等待新 token,避免并发触发多次刷新
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingRequests.push({
            resolve: (newToken) => {
              try {
                originalConfig.headers.Authorization = `Bearer ${newToken}`
                resolve(request(originalConfig))
              } catch (e) {
                reject(e as Error)
              }
            },
            // 刷新失败时逐个 reject,避免排队请求永久挂起
            reject,
          })
        })
      }

      isRefreshing = true
      try {
        const newToken = await doRefresh()
        // 重发排队请求
        pendingRequests.forEach(({ resolve }) => resolve(newToken))
        pendingRequests = []
        // 重发原请求
        originalConfig.headers.Authorization = `Bearer ${newToken}`
        return request(originalConfig)
      } catch (e) {
        // 刷新失败:拒绝所有排队请求(否则其 Promise 永不 settle,调用方永久卡死),跳登录
        const err = new Error((e as Error).message || '登录已失效,请重新登录')
        pendingRequests.forEach(({ reject }) => reject(err))
        pendingRequests = []
        clearAuthAndRedirect()
        return Promise.reject(err)
      } finally {
        isRefreshing = false
      }
    }

    // 非 401,或重试后仍 401(refresh 成功但 token 仍被拒,如已加入黑名单):跳登录
    if (error.response?.status === 401) {
      clearAuthAndRedirect()
    }
    const msg = error.response?.data?.message || error.message || '网络错误'
    return Promise.reject(new Error(msg))
  },
)

export default request

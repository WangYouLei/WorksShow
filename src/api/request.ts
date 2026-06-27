import axios from 'axios'

/**
 * Axios 请求实例
 * <p>
 * 统一配置 baseURL、超时、JWT 自动携带、401 自动跳登录。
 * 响应拦截器自动解包 Result,业务层直接拿到 data。
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

// 响应拦截器:自动解包 Result,401 跳登录
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
  (error) => {
    if (error.response?.status === 401) {
      // token 失效,清除并跳登录
      localStorage.removeItem('works-show:token')
      localStorage.removeItem('works-show:user')
      if (window.location.hash !== '#/login') {
        window.location.hash = '#/login'
      }
    }
    const msg = error.response?.data?.message || error.message || '网络错误'
    return Promise.reject(new Error(msg))
  },
)

export default request

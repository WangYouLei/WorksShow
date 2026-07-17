import { createRouter, createWebHashHistory } from 'vue-router'
import TemplateGallery from '@/editor/TemplateGallery.vue'
import EditorView from '@/editor/EditorView.vue'
import TemplatePreview from '@/pages/TemplatePreview.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', name: 'gallery', component: TemplateGallery },
    {
      path: '/preview/:templateId',
      name: 'preview',
      component: TemplatePreview,
      props: true,
    },
    {
      path: '/editor/:templateId',
      name: 'editor',
      component: EditorView,
      props: true,
      meta: { requiresAuth: true },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register.vue'),
    },
    {
      path: '/account',
      name: 'account',
      component: () => import('@/views/AccountView.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

// 解析 JWT payload,检查 token 是否过期(避免仅凭"存在"放行已过期 token)
function isTokenExpired(token: string): boolean {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return true
    // Base64Url -> Base64
    let payload = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    while (payload.length % 4) payload += '='
    const decoded = JSON.parse(atob(payload))
    if (!decoded.exp) return false
    return decoded.exp * 1000 < Date.now()
  } catch {
    return true
  }
}

// 全局前置守卫:需要登录的页面若未登录或 token 过期则跳转登录;已登录用户访问登录/注册页跳回首页
router.beforeEach((to) => {
  const token = localStorage.getItem('works-show:token')
  const expired = token ? isTokenExpired(token) : true
  if (to.meta.requiresAuth && (!token || expired)) {
    // 清理过期 token,避免后续请求携带无效 token
    if (token && expired) {
      localStorage.removeItem('works-show:token')
      localStorage.removeItem('works-show:user')
    }
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if ((to.name === 'login' || to.name === 'register') && token && !expired) {
    return { name: 'gallery' }
  }
  return true
})

export default router

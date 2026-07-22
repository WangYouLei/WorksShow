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

// 全局前置守卫:
// - 需要登录的页面:本地无 access token 且无 refresh token 时跳登录
//   (access token 过期不在此强制踢出,由请求层 401 拦截器自动用 refresh token 刷新)
// - 已登录用户(access 或 refresh token 任一存在)访问登录/注册页跳回首页
router.beforeEach((to) => {
  const token = localStorage.getItem('works-show:token')
  const refreshToken = localStorage.getItem('works-show:refreshToken')
  const hasAuth = !!(token || refreshToken)
  if (to.meta.requiresAuth && !hasAuth) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if ((to.name === 'login' || to.name === 'register') && hasAuth) {
    return { name: 'gallery' }
  }
  return true
})

export default router

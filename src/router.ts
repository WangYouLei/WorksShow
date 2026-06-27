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

// 全局前置守卫:需要登录的页面若未登录则跳转登录;已登录用户访问登录/注册页跳回首页
router.beforeEach((to) => {
  const token = localStorage.getItem('works-show:token')
  if (to.meta.requiresAuth && !token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if ((to.name === 'login' || to.name === 'register') && token) {
    return { name: 'gallery' }
  }
  return true
})

export default router

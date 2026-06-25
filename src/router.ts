import { createRouter, createWebHashHistory } from 'vue-router'
import TemplateGallery from '@/editor/TemplateGallery.vue'
import EditorView from '@/editor/EditorView.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', name: 'gallery', component: TemplateGallery },
    {
      path: '/editor/:templateId',
      name: 'editor',
      component: EditorView,
      props: true,
    },
  ],
})

export default router

<script setup lang="ts">
import { computed, ref } from 'vue'
import { getTemplatePreviewComponent } from '@/templates/registry'
import type { PortfolioData, ThemeConfig } from '@/templates/types'
import { themeToCssVars } from '@/composables/useThemeVars'
import { usePreviewAnchors } from '@/composables/usePreviewAnchors'

const props = defineProps<{ data: PortfolioData; templateId: string; theme?: ThemeConfig }>()
const Comp = computed(() => getTemplatePreviewComponent(props.templateId))
/** 主题变量注入预览容器：CSS 自定义属性穿透 scoped 样式继承到模板根元素 */
const themeVars = computed(() => themeToCssVars(props.theme))
/** 拦截预览内 # 锚点点击改为容器内滚动，避免改动应用 hash 路由导致页面跳走 */
const shellRef = ref<HTMLElement | null>(null)
usePreviewAnchors(shellRef)
</script>

<template>
  <div ref="shellRef" class="preview-shell" :style="themeVars">
    <component :is="Comp" v-if="Comp" :data="data" />
    <div v-else class="preview-empty">模板未找到：{{ templateId }}</div>
  </div>
</template>

<style scoped>
.preview-shell {
  height: 100%;
  width: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  background: #0a0a0f;
  /* 创建新包含块：子组件 position:fixed 会相对此容器定位，而非浏览器视口 */
  transform: translateZ(0);
  position: relative;
}
/* 编辑预览里强制显示渐入元素，避免 IntersectionObserver 在容器内不触发导致内容不可见 */
.preview-shell :deep(.reveal) {
  opacity: 1 !important;
  transform: none !important;
  transition: none !important;
}
.preview-empty {
  display: grid;
  place-items: center;
  height: 100%;
  color: rgba(245, 241, 232, 0.5);
  font-size: 14px;
}
</style>

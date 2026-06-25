<script setup lang="ts">
import MoYunPreview from '@/templates/mo-yun/MoYunPreview.vue'
import type { PortfolioData } from '@/templates/types'

defineProps<{ data: PortfolioData }>()
</script>

<template>
  <div class="preview-shell">
    <MoYunPreview :data="data" />
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
</style>

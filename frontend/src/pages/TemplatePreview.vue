<script setup lang="ts">
import { computed } from 'vue'
import { getTemplatePreviewComponent, getTemplateDefaultData } from '@/templates/registry'
import { usePreviewAnchors } from '@/composables/usePreviewAnchors'

const props = defineProps<{ templateId: string }>()
const data = computed(() => getTemplateDefaultData(props.templateId))
const Comp = computed(() => getTemplatePreviewComponent(props.templateId))
/** 拦截预览内 # 锚点点击改为页面内滚动，避免改动应用 hash 路由导致页面跳走 */
usePreviewAnchors()
</script>

<template>
  <component :is="Comp" v-if="Comp && data" :data="data" />
  <div v-else class="preview-empty">模板未找到：{{ templateId }}</div>
</template>

<style scoped>
.preview-empty {
  display: grid;
  place-items: center;
  min-height: 100vh;
  color: rgba(245, 241, 232, 0.5);
  font-size: 14px;
  background: #0a0a0f;
}
</style>

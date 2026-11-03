<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'
import type { Work } from '@/data/types'

const props = defineProps<{ work: Work }>()

type MediaItem = { kind: 'video' | 'image'; src: string }

/** 视频作为首帧,图集随后(无图集回落单张封面);全部渲染,靠 is-active 切换(导出静态化后 runtime 接管) */
const items = computed<MediaItem[]>(() => {
  const list: MediaItem[] = []
  if (props.work.videoUrl) list.push({ kind: 'video', src: props.work.videoUrl })
  const imgs = props.work.images?.filter(Boolean) ?? []
  const imgSrcs = imgs.length ? imgs : props.work.cover ? [props.work.cover] : []
  for (const src of imgSrcs) list.push({ kind: 'image', src })
  return list
})

const index = ref(0)
// 编辑中增删图片时回到第一帧,避免下标越界
watch(() => items.value.length, () => {
  index.value = 0
})

const current = computed(() => items.value[index.value])
const step = (dir: 1 | -1) => {
  const n = items.value.length
  if (n > 1) index.value = (index.value + dir + n) % n
}

// 图片帧自动轮播;视频帧与悬停时暂停(视频留给用户看,箭头随时可切)
const INTERVAL = 3500
const hovering = ref(false)
let timer: number | undefined
onMounted(() => {
  timer = window.setInterval(() => {
    if (!hovering.value && current.value?.kind === 'image') step(1)
  }, INTERVAL)
})
onBeforeUnmount(() => clearInterval(timer))
</script>

<template>
  <div v-if="current" class="wm" data-media @mouseenter="hovering = true" @mouseleave="hovering = false">
    <template v-for="(m, i) in items" :key="m.kind + m.src + i">
      <video
        v-if="m.kind === 'video'"
        v-show="i === index"
        class="wm-item"
        :src="m.src"
        controls
        playsinline
        preload="metadata"
        @click.stop
      ></video>
      <img
        v-else
        v-show="i === index"
        class="wm-item"
        :src="m.src"
        :alt="work.title"
        :loading="i === 0 ? 'eager' : 'lazy'"
      />
    </template>

    <template v-if="items.length > 1">
      <button class="wm-arrow wm-prev" type="button" aria-label="上一张" @click.stop="step(-1)">
        <ChevronLeft :size="16" :stroke-width="1.5" />
      </button>
      <button class="wm-arrow wm-next" type="button" aria-label="下一张" @click.stop="step(1)">
        <ChevronRight :size="16" :stroke-width="1.5" />
      </button>
      <span class="wm-counter font-mono">{{ index + 1 }} / {{ items.length }}</span>
    </template>
  </div>
</template>

<style scoped>
.wm {
  position: relative;
  width: 100%;
  height: 100%;
}
.wm-item {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.8s var(--ease);
}
.wm:hover img.wm-item {
  transform: scale(1.06);
}
.wm-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
  display: grid;
  place-items: center;
  width: 2rem;
  height: 2rem;
  color: var(--text, #f5f1e8);
  background: rgba(10, 10, 15, 0.6);
  border: 1px solid var(--border, rgba(245, 241, 232, 0.2));
  border-radius: 50%;
  cursor: pointer;
  opacity: 0;
  transition: all 0.25s ease;
}
.wm:hover .wm-arrow {
  opacity: 1;
}
.wm-prev {
  left: 0.6rem;
}
.wm-next {
  right: 0.6rem;
}
.wm-counter {
  position: absolute;
  bottom: 0.6rem;
  left: 0.6rem;
  z-index: 2;
  font-size: var(--fs-tiny, 11px);
  letter-spacing: 0.1em;
  color: var(--moon-white, #f5f1e8);
  background: rgba(10, 10, 15, 0.7);
  padding: 0.25rem 0.6rem;
  border-radius: var(--radius-sm, 4px);
  backdrop-filter: blur(4px);
}
</style>

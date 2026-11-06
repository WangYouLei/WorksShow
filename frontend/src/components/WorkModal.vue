<script setup lang="ts">
import { computed, watch, ref, nextTick, onUnmounted } from 'vue'
import { X, ArrowUpRight, ChevronLeft, ChevronRight, Maximize2 } from 'lucide-vue-next'
import type { Work } from '@/data/types'
import { safeUrl } from '@/utils/safeUrl'
import { openLightbox } from '@/utils/lightbox'

const props = defineProps<{ work: Work | null }>()
const emit = defineEmits<{ close: [] }>()

const onClose = () => emit('close')

const onKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape') onClose()
}

watch(
  () => props.work,
  (val) => {
    if (val) {
      document.body.style.overflow = 'hidden'
      window.addEventListener('keydown', onKeydown)
    } else {
      document.body.style.overflow = ''
      window.removeEventListener('keydown', onKeydown)
    }
    // 切换作品时图集回到第一张
    activeIndex.value = 0
  },
)

onUnmounted(() => {
  document.body.style.overflow = ''
  window.removeEventListener('keydown', onKeydown)
})

// —— 图集浏览:优先 images,无图集回落单张封面;视频保持单个,置于图集上方 ——
/** 可浏览的图片列表(过滤空值) */
const gallery = computed(() => {
  const imgs = props.work?.images?.filter(Boolean) ?? []
  if (imgs.length) return imgs
  return props.work?.cover ? [props.work.cover] : []
})
/** 当前展示的图片下标 */
const activeIndex = ref(0)
/** 缩略图元素引用(激活项滚动到可见区) */
const thumbRefs = ref<HTMLElement[]>([])

const setActive = async (i: number) => {
  activeIndex.value = i
  await nextTick()
  thumbRefs.value[i]?.scrollIntoView({ block: 'nearest', inline: 'nearest' })
}

/** 循环切换上一张/下一张 */
const step = (dir: 1 | -1) => {
  const n = gallery.value.length
  if (n) setActive((activeIndex.value + dir + n) % n)
}

/** 放大查看当前图片(导出页由 runtime 委托接管,类名 wm-zoom 不可改) */
const zoom = () => {
  const src = gallery.value[activeIndex.value]
  if (src) openLightbox(src)
}

// 触屏左右滑动切换(位移超过 40px 判定为滑动)
let touchX = 0
const onTouchStart = (e: TouchEvent) => {
  touchX = e.touches[0].clientX
}
const onTouchEnd = (e: TouchEvent) => {
  const dx = e.changedTouches[0].clientX - touchX
  if (Math.abs(dx) > 40) step(dx < 0 ? 1 : -1)
}
</script>

<template>
  <Transition name="modal">
    <div v-if="work" class="modal-overlay" @click.self="onClose">
      <article class="modal-card" role="dialog" aria-modal="true">
        <button class="modal-close" aria-label="关闭" @click="onClose">
          <X :size="20" :stroke-width="1.5" />
        </button>

        <div
          v-if="work.videoUrl || gallery.length"
          class="modal-cover"
          :class="{ 'has-gallery': gallery.length }"
        >
          <video v-if="work.videoUrl" :src="work.videoUrl" controls playsinline preload="metadata"></video>
          <div v-if="gallery.length" class="modal-gallery" data-gallery>
            <div
              class="gallery-stage"
              @touchstart.passive="onTouchStart"
              @touchend.passive="onTouchEnd"
            >
              <img class="gallery-main" :src="gallery[activeIndex]" :alt="work.title" />
              <template v-if="gallery.length > 1">
                <button class="gallery-arrow gallery-prev" type="button" aria-label="上一张" @click="step(-1)">
                  <ChevronLeft :size="18" :stroke-width="1.5" />
                </button>
                <button class="gallery-arrow gallery-next" type="button" aria-label="下一张" @click="step(1)">
                  <ChevronRight :size="18" :stroke-width="1.5" />
                </button>
                <span class="gallery-counter font-mono">{{ activeIndex + 1 }} / {{ gallery.length }}</span>
              </template>
              <button class="wm-zoom gallery-zoom" type="button" aria-label="放大查看" @click="zoom">
                <Maximize2 :size="15" :stroke-width="1.5" />
              </button>
            </div>
            <div v-if="gallery.length > 1" class="gallery-thumbs">
              <button
                v-for="(img, gi) in gallery"
                :key="img + gi"
                :ref="(el) => { if (el) thumbRefs[gi] = el as HTMLElement }"
                class="gallery-thumb"
                :class="{ 'is-active': gi === activeIndex }"
                type="button"
                :data-index="gi"
                @click="setActive(gi)"
              >
                <img :src="img" alt="" loading="lazy" />
              </button>
            </div>
          </div>
          <span v-if="work.year" class="modal-year">{{ work.year }}</span>
        </div>

        <div class="modal-body">
          <span class="modal-category">{{ work.category }}</span>
          <h3 class="modal-title">{{ work.title }}</h3>
          <p class="modal-desc">{{ work.description }}</p>

          <div class="modal-tags">
            <span v-for="tag in work.tags" :key="tag" class="tag">{{ tag }}</span>
          </div>

          <a
            v-if="safeUrl(work.link)"
            :href="safeUrl(work.link)"
            target="_blank"
            rel="noopener noreferrer"
            class="btn btn-primary modal-link"
          >
            查看项目
            <ArrowUpRight :size="16" :stroke-width="1.5" />
          </a>
        </div>
      </article>
    </div>
  </Transition>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: grid;
  place-items: center;
  padding: 1.5rem;
  background: rgba(5, 5, 8, 0.82);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.modal-card {
  position: relative;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  max-width: 960px;
  width: 100%;
  max-height: 88vh;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: 0 40px 80px -20px rgba(0, 0, 0, 0.6);
}

.modal-close {
  position: absolute;
  top: 1rem;
  right: 1rem;
  z-index: 2;
  display: grid;
  place-items: center;
  width: 2.5rem;
  height: 2.5rem;
  color: var(--text);
  background: rgba(10, 10, 15, 0.6);
  border: 1px solid var(--border);
  border-radius: 50%;
  transition: all var(--dur) var(--ease);
}

.modal-close:hover {
  background: var(--accent);
  color: var(--ink-black);
  border-color: var(--accent);
  transform: rotate(90deg);
}

.modal-cover {
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 320px;
  background: var(--ink-soft);
}

/* 仅视频时独占整列;与图集并存时限制高度把剩余空间留给图集 */
.modal-cover > video {
  width: 100%;
  flex: 1;
  min-height: 0;
  object-fit: cover;
}
.modal-cover.has-gallery > video {
  flex: 0 0 auto;
  max-height: 45%;
}

/* —— 图集:主图舞台 + 可滑动缩略图条 —— */
.modal-gallery {
  position: relative;
  display: flex;
  flex: 1;
  min-height: 0;
  flex-direction: column;
}

.gallery-stage {
  position: relative;
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.25);
}

.gallery-main {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.gallery-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
  display: grid;
  place-items: center;
  width: 2.25rem;
  height: 2.25rem;
  color: var(--text);
  background: rgba(10, 10, 15, 0.6);
  border: 1px solid var(--border);
  border-radius: 50%;
  cursor: pointer;
  transition: all var(--dur) var(--ease);
}

.gallery-arrow:hover {
  color: var(--ink-black);
  background: var(--accent);
  border-color: var(--accent);
}

.gallery-prev {
  left: 0.75rem;
}

.gallery-next {
  right: 0.75rem;
}

.gallery-counter {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  font-size: var(--fs-tiny);
  letter-spacing: 0.1em;
  color: var(--moon-white);
  background: rgba(10, 10, 15, 0.7);
  padding: 0.25rem 0.6rem;
  border-radius: var(--radius-sm);
  backdrop-filter: blur(4px);
}

.gallery-thumbs {
  display: flex;
  gap: 8px;
  padding: 10px;
  overflow-x: auto;
  scroll-snap-type: x proximity;
  flex-shrink: 0;
  border-top: 1px solid var(--border);
}

.gallery-thumb {
  flex: 0 0 auto;
  width: 64px;
  height: 44px;
  padding: 0;
  overflow: hidden;
  cursor: pointer;
  opacity: 0.55;
  background: none;
  border: 2px solid transparent;
  border-radius: var(--radius-sm);
  scroll-snap-align: start;
  transition: all var(--dur) var(--ease);
}

.gallery-thumb:hover {
  opacity: 0.85;
}

.gallery-thumb.is-active {
  opacity: 1;
  border-color: var(--accent);
}

.gallery-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 放大按钮:与图集缩略图条贴合,位于主图右下角 */
.gallery-zoom {
  position: absolute;
  bottom: 0.75rem;
  right: 0.75rem;
  z-index: 2;
  display: grid;
  place-items: center;
  width: 2.25rem;
  height: 2.25rem;
  color: var(--text);
  background: rgba(10, 10, 15, 0.6);
  border: 1px solid var(--border);
  border-radius: 50%;
  cursor: zoom-in;
  opacity: 0.8;
  transition: all var(--dur) var(--ease);
}

.gallery-zoom:hover {
  opacity: 1;
  color: var(--ink-black);
  background: var(--accent);
  border-color: var(--accent);
}

.modal-year {
  position: absolute;
  top: 1rem;
  left: 1rem;
  z-index: 1;
  font-family: var(--font-mono);
  font-size: var(--fs-tiny);
  letter-spacing: 0.15em;
  color: var(--moon-white);
  background: rgba(10, 10, 15, 0.7);
  padding: 0.375rem 0.75rem;
  border-radius: var(--radius-sm);
  backdrop-filter: blur(4px);
}

.modal-body {
  padding: 2.5rem;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.modal-category {
  font-family: var(--font-mono);
  font-size: var(--fs-tiny);
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--accent);
  margin-bottom: 0.75rem;
}

.modal-title {
  font-family: var(--font-serif);
  font-size: var(--fs-h3);
  font-weight: 500;
  margin-bottom: 1.25rem;
  line-height: 1.3;
}

.modal-desc {
  font-size: var(--fs-small);
  color: var(--text-dim);
  line-height: 1.9;
  margin-bottom: 1.75rem;
}

.modal-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 2rem;
}

.tag {
  padding: 0.375rem 0.875rem;
  font-size: var(--fs-tiny);
  font-family: var(--font-mono);
  color: var(--celadon);
  background: rgba(123, 168, 156, 0.08);
  border: 1px solid rgba(123, 168, 156, 0.2);
  border-radius: var(--radius-sm);
}

.modal-link {
  margin-top: auto;
  align-self: flex-start;
}

/* —— 过渡 —— */
.modal-enter-active,
.modal-leave-active {
  transition: opacity var(--dur) var(--ease);
}

.modal-enter-active .modal-card,
.modal-leave-active .modal-card {
  transition: transform 0.5s var(--ease-out), opacity 0.4s var(--ease);
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-card,
.modal-leave-to .modal-card {
  transform: translateY(24px) scale(0.97);
  opacity: 0;
}

@media (max-width: 768px) {
  .modal-card {
    grid-template-columns: 1fr;
    max-height: 90vh;
    overflow-y: auto;
  }

  .modal-cover {
    min-height: 220px;
    max-height: 320px;
  }

  .modal-cover > video {
    flex: 0 0 auto;
    max-height: 45%;
  }

  .gallery-thumb {
    width: 52px;
    height: 38px;
  }

  .modal-body {
    padding: 1.75rem;
  }
}
</style>

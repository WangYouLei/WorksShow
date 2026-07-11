<script setup lang="ts">
import { watch, onUnmounted } from 'vue'
import { X, ArrowUpRight } from 'lucide-vue-next'
import type { Work } from '@/data/types'

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
  },
)

onUnmounted(() => {
  document.body.style.overflow = ''
  window.removeEventListener('keydown', onKeydown)
})
</script>

<template>
  <Transition name="modal">
    <div v-if="work" class="modal-overlay" @click.self="onClose">
      <article class="modal-card" role="dialog" aria-modal="true">
        <button class="modal-close" aria-label="关闭" @click="onClose">
          <X :size="20" :stroke-width="1.5" />
        </button>

        <div class="modal-cover">
          <img :src="work.cover" :alt="work.title" loading="lazy" />
          <span class="modal-year">{{ work.year }}</span>
        </div>

        <div class="modal-body">
          <span class="modal-category">{{ work.category }}</span>
          <h3 class="modal-title">{{ work.title }}</h3>
          <p class="modal-desc">{{ work.description }}</p>

          <div class="modal-tags">
            <span v-for="tag in work.tags" :key="tag" class="tag">{{ tag }}</span>
          </div>

          <a
            v-if="work.link"
            :href="work.link"
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
  overflow: hidden;
  min-height: 320px;
  background: var(--ink-soft);
}

.modal-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.modal-year {
  position: absolute;
  bottom: 1rem;
  left: 1rem;
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
    max-height: 240px;
  }

  .modal-body {
    padding: 1.75rem;
  }
}
</style>

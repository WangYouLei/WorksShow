<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { X, ArrowUpRight, Mail, MapPin, Clock, Github } from 'lucide-vue-next'
import type { PortfolioData } from '../types'
import { PP_ENGINE } from './engine'

const props = defineProps<{ data: PortfolioData }>()

const rootEl = ref<HTMLElement | null>(null)
let dispose: (() => void) | null = null

const initials = computed(() => props.data.profile.name.slice(0, 1))
const stats = computed(() => props.data.profile.stats)

onMounted(() => {
  if (!rootEl.value) return
  // 与导出 HTML 共用同一份引擎源码，保证预览与导出交互完全一致
  const create = new Function('return ' + PP_ENGINE)() as (root: HTMLElement) => () => void
  dispose = create(rootEl.value)
})

onBeforeUnmount(() => {
  dispose?.()
  dispose = null
})
</script>

<template>
  <div ref="rootEl" class="pp-root" :data-name="data.profile.name">
    <canvas class="pp-canvas"></canvas>
    <div class="pp-vignette" aria-hidden="true"></div>

    <header class="pp-top">
      <a class="pp-brand" href="#top">
        <span class="pp-brand-mark">{{ initials }}</span>
        <span class="pp-brand-name">{{ data.profile.name }}</span>
      </a>
      <span class="pp-avail"><i></i>{{ data.profile.availability || '随时可用' }}</span>
    </header>

    <main id="top" class="pp-panel">
      <!-- 星球 -->
      <section class="pp-view" data-panel="home">
        <span class="pp-eyebrow">{{ data.profile.title }}</span>
        <h1 class="pp-name">{{ data.profile.name }}</h1>
        <p class="pp-nameen">{{ data.profile.nameEn }}</p>
        <p class="pp-tagline">{{ data.profile.tagline }}</p>
        <div class="pp-stats">
          <div v-for="s in stats" :key="s.label" class="pp-stat">
            <strong>{{ s.value }}</strong>
            <span>{{ s.label }}</span>
          </div>
        </div>
        <p class="pp-hint">拖拽旋转星球 · 点击星球或底部导航切换板块</p>
      </section>

      <!-- 关于 -->
      <section class="pp-view" data-panel="about">
        <h2 class="pp-view-h">关于我</h2>
        <p class="pp-view-text">{{ data.profile.bio }}</p>
        <ul class="pp-view-meta">
          <li><MapPin :size="15" :stroke-width="1.75" /><span>{{ data.profile.location }}</span></li>
          <li><Clock :size="15" :stroke-width="1.75" /><span>{{ data.profile.availability }}</span></li>
        </ul>
        <p class="pp-hint">拖拽旋转你的名字 · 点击星球切换板块</p>
      </section>

      <!-- 数据 -->
      <section class="pp-view" data-panel="stats">
        <h2 class="pp-view-h">数据轨迹</h2>
        <div class="pp-stat-grid">
          <div v-for="s in stats" :key="s.label" class="pp-statcard">
            <strong>{{ s.value }}</strong>
            <span>{{ s.label }}</span>
          </div>
        </div>
        <p class="pp-hint">三重数据环正在旋转 · 点击星球继续</p>
      </section>

      <!-- 作品 -->
      <section class="pp-view" data-panel="works">
        <h2 class="pp-view-h">作品星系</h2>
        <ul class="pp-works">
          <li
            v-for="w in data.works"
            :key="w.id"
            :data-work-id="w.id"
            role="button"
            tabindex="0"
            :aria-label="w.title"
          >
            <span class="pp-work-cover">
              <img :src="w.cover" :alt="w.title" loading="lazy" />
            </span>
            <span class="pp-work-info">
              <strong>{{ w.title }}</strong>
              <span>{{ w.category }} · {{ w.year }}</span>
            </span>
            <ArrowUpRight :size="15" :stroke-width="2" />
          </li>
        </ul>
      </section>

      <!-- 技能 -->
      <section class="pp-view" data-panel="skills">
        <h2 class="pp-view-h">技能螺旋</h2>
        <div v-for="g in data.skills" :key="g.id" class="pp-skill-group">
          <span class="pp-skill-cat">{{ g.category }}<i>{{ g.categoryEn }}</i></span>
          <div class="pp-skill-items">
            <span v-for="item in g.items" :key="item">{{ item }}</span>
          </div>
        </div>
      </section>

      <!-- 联系 -->
      <section class="pp-view" data-panel="contact">
        <h2 class="pp-view-h">联系漩涡</h2>
        <p class="pp-view-text">有想法？随时聊聊，通常 24 小时内回复。</p>
        <ul class="pp-view-meta">
          <li><Mail :size="15" :stroke-width="1.75" /><a :href="'mailto:' + data.profile.email">{{ data.profile.email }}</a></li>
          <li><MapPin :size="15" :stroke-width="1.75" /><span>{{ data.profile.location }}</span></li>
        </ul>
        <a class="pp-mailto" :href="'mailto:' + data.profile.email">
          发邮件给我 <ArrowUpRight :size="16" :stroke-width="2" />
        </a>
        <div class="pp-socials">
          <a
            v-for="s in data.profile.socials"
            :key="s.name"
            :href="s.url"
            target="_blank"
            rel="noopener noreferrer"
          >
            <component :is="s.icon === 'github' ? Github : Mail" :size="14" :stroke-width="1.75" />
            {{ s.name }}
          </a>
        </div>
      </section>
    </main>

    <nav class="pp-nav" aria-label="信息板块">
      <button type="button" data-nav="0">星球</button>
      <button type="button" data-nav="1">关于</button>
      <button type="button" data-nav="2">数据</button>
      <button type="button" data-nav="3">作品</button>
      <button type="button" data-nav="4">技能</button>
      <button type="button" data-nav="5">联系</button>
    </nav>

    <footer class="pp-footer">
      © {{ new Date().getFullYear() }} {{ data.profile.name }} · 基于 WorksShow 构建 · 粒子星球
    </footer>

    <!-- 作品弹窗 -->
    <div v-for="w in data.works" :key="w.id" class="pp-modal" :data-modal-id="w.id">
      <div class="modal-overlay"></div>
      <article class="pp-modal-card">
        <button class="modal-close" aria-label="关闭" type="button">
          <X :size="20" :stroke-width="1.75" />
        </button>
        <div class="pp-modal-cover">
          <img :src="w.cover" :alt="w.title" loading="lazy" />
        </div>
        <div class="pp-modal-body">
          <span class="pp-modal-cat">{{ w.category }} · {{ w.year }}</span>
          <h3 class="pp-modal-title">{{ w.title }}</h3>
          <p class="pp-modal-desc">{{ w.description }}</p>
          <div class="pp-modal-tags"><i v-for="t in w.tags" :key="t">{{ t }}</i></div>
          <a
            v-if="w.link"
            class="pp-mailto pp-modal-link"
            :href="w.link"
            target="_blank"
            rel="noopener noreferrer"
          >
            查看项目 <ArrowUpRight :size="15" :stroke-width="2" />
          </a>
        </div>
      </article>
    </div>
  </div>
</template>

<style scoped>
/* —— 设计令牌：深空暗色 + 青紫粉粒子光 —— */
.pp-root {
  --pp-ink: #eef1ff;
  --pp-ink-soft: #b9c0e2;
  --pp-ink-mute: #8a92b8;
  --pp-ink-faint: #5d6588;
  --pp-cyan: #38bdf8;
  --pp-violet: #8b7cf6;
  --pp-pink: #ec5fa8;
  --pp-glass: rgba(13, 16, 34, 0.66);
  --pp-line: rgba(255, 255, 255, 0.14);
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background: #06081a;
  background: radial-gradient(ellipse 120% 90% at 50% 42%, #0b0e26 0%, #06081a 55%, #04050f 100%);
  color: var(--pp-ink);
  font-family: 'Noto Sans SC', 'Manrope', system-ui, -apple-system, sans-serif;
  font-size: 15px;
  line-height: 1.7;
  -webkit-font-smoothing: antialiased;
}
.pp-root * {
  box-sizing: border-box;
}
.pp-canvas {
  position: absolute;
  inset: 0;
  z-index: 0;
  cursor: grab;
  touch-action: none;
  display: block;
}
.pp-canvas:active {
  cursor: grabbing;
}
.pp-vignette {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(ellipse 90% 80% at 50% 46%, transparent 55%, rgba(4, 5, 15, 0.55) 100%);
}
.pp-top,
.pp-panel,
.pp-nav,
.pp-footer,
.pp-modal {
  position: relative;
  z-index: 2;
}

/* —— 顶栏 —— */
.pp-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px clamp(20px, 4vw, 44px) 0;
}
.pp-brand {
  display: inline-flex;
  align-items: center;
  gap: 11px;
  text-decoration: none;
  color: var(--pp-ink);
}
.pp-brand-mark {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  font-weight: 800;
  font-size: 14px;
  color: #fff;
  background: linear-gradient(120deg, #38bdf8, #8b7cf6);
  border-radius: 11px;
  box-shadow: 0 0 24px rgba(56, 189, 248, 0.45);
}
.pp-brand-name {
  font-weight: 700;
  font-size: 15px;
}
.pp-avail {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  font-size: 12.5px;
  font-weight: 600;
  color: #6ee7b7;
  background: rgba(52, 211, 153, 0.1);
  border: 1px solid rgba(52, 211, 153, 0.28);
  border-radius: 999px;
}
.pp-avail i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 10px #34d399;
  animation: pp-pulse 2s ease-in-out infinite;
}
@keyframes pp-pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

/* —— 右侧信息面板 —— */
.pp-panel {
  position: fixed;
  z-index: 30;
  top: 50%;
  right: clamp(14px, 3vw, 36px);
  width: min(400px, calc(100vw - 28px));
  max-height: min(78vh, 640px);
  overflow-y: auto;
  padding: 28px 28px 24px;
  background: var(--pp-glass);
  border: 1px solid var(--pp-line);
  border-radius: 26px;
  backdrop-filter: blur(20px) saturate(150%);
  -webkit-backdrop-filter: blur(20px) saturate(150%);
  box-shadow: 0 30px 80px -24px rgba(0, 0, 0, 0.7);
  transform: translateY(-50%);
}
.pp-view {
  display: none;
}
.pp-view.is-active {
  display: block;
  animation: pp-view-in 0.5s cubic-bezier(0.22, 1, 0.36, 1) both;
}
@keyframes pp-view-in {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: none; }
}
.pp-eyebrow {
  display: inline-block;
  padding: 5px 13px;
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.12em;
  color: var(--pp-cyan);
  background: rgba(56, 189, 248, 0.1);
  border: 1px solid rgba(56, 189, 248, 0.28);
  border-radius: 999px;
  margin-bottom: 14px;
}
.pp-name {
  font-size: clamp(30px, 4vw, 42px);
  font-weight: 900;
  letter-spacing: 0.04em;
  line-height: 1.2;
  margin: 0 0 6px;
  background: linear-gradient(120deg, #7dd3fc, #a78bfa 55%, #f0abfc);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.pp-nameen {
  font-family: 'Manrope', sans-serif;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.32em;
  text-transform: uppercase;
  color: var(--pp-ink-faint);
  margin: 0 0 14px;
}
.pp-tagline {
  font-size: 13.5px;
  color: var(--pp-ink-soft);
  line-height: 1.85;
  margin: 0 0 16px;
}
.pp-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 9px;
  margin-bottom: 16px;
}
.pp-stat {
  padding: 11px 8px;
  text-align: center;
  background: rgba(139, 124, 246, 0.08);
  border: 1px solid rgba(139, 124, 246, 0.22);
  border-radius: 13px;
}
.pp-stat strong {
  display: block;
  font-family: 'Manrope', sans-serif;
  font-size: 18px;
  font-weight: 800;
  background: linear-gradient(120deg, #7dd3fc, #c4b5fd);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.pp-stat span {
  font-size: 11px;
  color: var(--pp-ink-mute);
}
.pp-hint {
  font-size: 12px;
  color: var(--pp-ink-faint);
  letter-spacing: 0.05em;
  border-top: 1px dashed rgba(255, 255, 255, 0.12);
  padding-top: 12px;
  margin: 4px 0 0;
}
.pp-view-h {
  font-size: 19px;
  font-weight: 800;
  margin: 0 0 14px;
  letter-spacing: 0.02em;
}
.pp-view-text {
  font-size: 13.5px;
  color: var(--pp-ink-soft);
  line-height: 1.9;
  margin: 0 0 14px;
}
.pp-view-meta {
  list-style: none;
  margin: 0 0 16px;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 9px;
}
.pp-view-meta li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13.5px;
  color: var(--pp-ink-soft);
}
.pp-view-meta li :deep(svg) {
  color: var(--pp-cyan);
  flex-shrink: 0;
}
.pp-view-meta a {
  color: var(--pp-ink-soft);
  text-decoration: none;
  font-family: 'Manrope', 'Noto Sans SC', sans-serif;
}
.pp-view-meta a:hover {
  color: var(--pp-cyan);
}
.pp-stat-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
.pp-statcard {
  padding: 16px 12px;
  text-align: center;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--pp-line);
  border-radius: 16px;
}
.pp-statcard strong {
  display: block;
  font-family: 'Manrope', sans-serif;
  font-size: 26px;
  font-weight: 800;
  background: linear-gradient(120deg, #7dd3fc, #ec5fa8);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.pp-statcard span {
  font-size: 12px;
  color: var(--pp-ink-mute);
}

/* —— 作品列表 —— */
.pp-works {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 9px;
}
.pp-works li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.045);
  border: 1px solid var(--pp-line);
  border-radius: 15px;
  cursor: pointer;
  transition: transform 0.25s ease, border-color 0.25s ease, background 0.25s ease;
}
.pp-works li:hover {
  transform: translateY(-2px);
  border-color: rgba(56, 189, 248, 0.45);
  background: rgba(56, 189, 248, 0.07);
}
.pp-works li:focus-visible {
  outline: 2px solid var(--pp-cyan);
  outline-offset: 2px;
}
.pp-work-cover {
  flex-shrink: 0;
  width: 50px;
  height: 50px;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.06);
}
.pp-work-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.pp-work-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.pp-work-info strong {
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pp-work-info span {
  font-size: 12px;
  color: var(--pp-ink-mute);
}
.pp-works li :deep(.lucide) {
  color: var(--pp-ink-faint);
  flex-shrink: 0;
}

/* —— 技能 —— */
.pp-skill-group {
  margin-bottom: 14px;
}
.pp-skill-group:last-child {
  margin-bottom: 0;
}
.pp-skill-cat {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 7px;
}
.pp-skill-cat i {
  font-style: normal;
  font-family: 'Manrope', sans-serif;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--pp-ink-faint);
}
.pp-skill-items {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.pp-skill-items span {
  padding: 5px 11px;
  font-size: 12px;
  color: var(--pp-ink-soft);
  background: rgba(56, 189, 248, 0.07);
  border: 1px solid rgba(56, 189, 248, 0.22);
  border-radius: 999px;
  font-family: 'Manrope', 'Noto Sans SC', sans-serif;
}

/* —— 联系 —— */
.pp-mailto {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 12px 22px;
  font-size: 14px;
  font-weight: 600;
  color: #0a0c1c;
  text-decoration: none;
  background: linear-gradient(120deg, #7dd3fc, #a78bfa);
  border-radius: 999px;
  box-shadow: 0 14px 34px -10px rgba(125, 211, 252, 0.5);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.pp-mailto:hover {
  transform: translateY(-2px);
}
.pp-socials {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}
.pp-socials a {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 8px 14px;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--pp-ink-soft);
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--pp-line);
  border-radius: 999px;
  text-decoration: none;
  transition: color 0.2s ease, border-color 0.2s ease;
}
.pp-socials a:hover {
  color: var(--pp-cyan);
  border-color: rgba(56, 189, 248, 0.45);
}
.pp-socials a :deep(svg) {
  color: var(--pp-cyan);
}

/* —— 底部导航 —— */
.pp-nav {
  position: fixed;
  z-index: 30;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 4px;
  padding: 6px;
  background: var(--pp-glass);
  border: 1px solid var(--pp-line);
  border-radius: 999px;
  backdrop-filter: blur(18px) saturate(150%);
  -webkit-backdrop-filter: blur(18px) saturate(150%);
}
.pp-nav button {
  padding: 8px 15px;
  font-size: 12.5px;
  font-weight: 600;
  font-family: inherit;
  color: var(--pp-ink-mute);
  background: transparent;
  border: 0;
  border-radius: 999px;
  cursor: pointer;
  transition: color 0.2s ease, background 0.2s ease;
}
.pp-nav button:hover {
  color: var(--pp-ink);
}
.pp-nav button.is-active {
  color: #0a0c1c;
  background: linear-gradient(120deg, #7dd3fc, #a78bfa);
}

/* —— 页脚 —— */
.pp-footer {
  position: fixed;
  z-index: 20;
  bottom: 20px;
  left: 24px;
  font-size: 11.5px;
  color: var(--pp-ink-faint);
  letter-spacing: 0.04em;
}

/* —— 作品弹窗 —— */
.pp-modal {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: none;
  align-items: center;
  justify-content: center;
  padding: 18px;
}
.pp-modal.is-open {
  display: flex;
}
.modal-overlay {
  position: absolute;
  inset: 0;
  background: rgba(3, 4, 12, 0.62);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}
.pp-modal-card {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1.1fr;
  max-width: 780px;
  width: 100%;
  max-height: 86vh;
  background: rgba(16, 19, 40, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 40px 90px -30px rgba(0, 0, 0, 0.8);
}
.modal-close {
  position: absolute;
  top: 14px;
  right: 14px;
  z-index: 2;
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  color: var(--pp-ink-soft);
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid var(--pp-line);
  border-radius: 50%;
  cursor: pointer;
  transition: transform 0.2s ease, color 0.2s ease, background 0.2s ease;
}
.modal-close:hover {
  color: #7dd3fc;
  background: rgba(56, 189, 248, 0.18);
  transform: rotate(90deg);
}
.pp-modal-cover {
  min-height: 260px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.04);
}
.pp-modal-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.pp-modal-body {
  padding: 32px 32px 28px;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.pp-modal-cat {
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--pp-cyan);
  margin-bottom: 10px;
}
.pp-modal-title {
  font-size: 22px;
  font-weight: 800;
  margin: 0 0 12px;
  line-height: 1.3;
}
.pp-modal-desc {
  font-size: 13.5px;
  color: var(--pp-ink-soft);
  line-height: 1.85;
  margin: 0 0 18px;
}
.pp-modal-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 20px;
}
.pp-modal-tags i {
  font-style: normal;
  padding: 3px 10px;
  font-size: 11px;
  color: var(--pp-ink-soft);
  background: rgba(56, 189, 248, 0.08);
  border: 1px solid rgba(56, 189, 248, 0.24);
  border-radius: 999px;
}
.pp-modal-link {
  align-self: flex-start;
  margin-top: auto;
}

/* —— 减少动效 —— */
@media (prefers-reduced-motion: reduce) {
  .pp-root *,
  .pp-root *::before,
  .pp-root *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* —— 响应式 —— */
@media (max-width: 720px) {
  .pp-panel {
    top: auto;
    bottom: 74px;
    left: 12px;
    right: 12px;
    width: auto;
    max-height: 46vh;
    padding: 20px;
    transform: none;
  }
  .pp-nav {
    bottom: 12px;
  }
  .pp-nav button {
    padding: 8px 11px;
    font-size: 12px;
  }
  .pp-footer {
    display: none;
  }
  .pp-modal-card {
    grid-template-columns: 1fr;
    max-height: 88vh;
    overflow-y: auto;
  }
  .pp-modal-cover {
    min-height: 160px;
    max-height: 200px;
  }
}
</style>

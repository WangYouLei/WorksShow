<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { X, ArrowUpRight, Mail, MapPin, Clock, Github } from 'lucide-vue-next'
import type { PortfolioData } from '../types'
import { CUBE_ENGINE } from './engine'

const props = defineProps<{ data: PortfolioData }>()

const rootEl = ref<HTMLElement | null>(null)
let dispose: (() => void) | null = null

const initials = computed(() => props.data.profile.name.slice(0, 1))
const stats = computed(() => props.data.profile.stats)
const worksTop = computed(() => props.data.works.slice(0, 4))
const skillsBrief = computed(() =>
  props.data.skills.map((s) => ({ id: s.id, name: s.category, count: s.items.length })),
)
const recentExp = computed(() => [...props.data.experiences].slice(-3).reverse())

onMounted(() => {
  if (!rootEl.value) return
  // 与导出 HTML 共用同一份引擎源码，保证预览与导出交互完全一致
  const create = new Function('return ' + CUBE_ENGINE)() as (root: HTMLElement) => () => void
  dispose = create(rootEl.value)
})

onBeforeUnmount(() => {
  dispose?.()
  dispose = null
})
</script>

<template>
  <div ref="rootEl" class="cd-root">
    <!-- 背景：瓷白渐变 + 柔和色斑 -->
    <div class="cd-bg" aria-hidden="true">
      <span class="cd-blob cd-blob-1"></span>
      <span class="cd-blob cd-blob-2"></span>
      <span class="cd-blob cd-blob-3"></span>
    </div>

    <header class="cd-top">
      <a class="cd-brand" href="#top">
        <span class="cd-brand-mark">{{ initials }}</span>
        <span class="cd-brand-name">{{ data.profile.name }}</span>
      </a>
      <span class="cd-avail"><i></i>{{ data.profile.availability || '随时可用' }}</span>
    </header>

    <main id="top" class="cd-main">
      <!-- 3D 舞台 -->
      <div class="cd-stage">
        <div class="cd-cube-scale">
          <div class="cd-cube">
            <!-- front 封面 -->
            <section class="cd-face cd-face-front" data-face="front" style="--acc: #7c6cf6">
              <span class="cd-face-tag">01 · 封面</span>
              <div class="cd-face-body cd-face-cover">
                <span class="cd-face-eyebrow">{{ data.profile.title }}</span>
                <strong class="cd-face-name">{{ data.profile.name }}</strong>
                <span class="cd-face-nameen">{{ data.profile.nameEn }}</span>
                <p class="cd-face-tagline">{{ data.profile.tagline }}</p>
              </div>
              <span class="cd-face-tip">点击展开</span>
            </section>

            <!-- right 关于 -->
            <section class="cd-face cd-face-right" data-face="right" style="--acc: #ec5fa8">
              <span class="cd-face-tag">02 · 关于</span>
              <div class="cd-face-body">
                <p class="cd-face-bio">{{ data.profile.bio }}</p>
                <span class="cd-face-meta">
                  <MapPin :size="13" :stroke-width="1.75" />{{ data.profile.location }}
                </span>
              </div>
              <span class="cd-face-tip">点击展开</span>
            </section>

            <!-- back 作品 -->
            <section class="cd-face cd-face-back" data-face="back" style="--acc: #4d8dff">
              <span class="cd-face-tag">03 · 作品</span>
              <ul class="cd-face-body cd-face-list">
                <li v-for="w in worksTop" :key="w.id">
                  <span class="cd-li-year">{{ w.year }}</span>
                  <span class="cd-li-title">{{ w.title }}</span>
                </li>
              </ul>
              <span class="cd-face-tip">点击展开全部</span>
            </section>

            <!-- left 技能 -->
            <section class="cd-face cd-face-left" data-face="left" style="--acc: #1fb6d4">
              <span class="cd-face-tag">04 · 技能</span>
              <ul class="cd-face-body cd-face-list cd-face-skills">
                <li v-for="s in skillsBrief" :key="s.id">
                  <span class="cd-li-title">{{ s.name }}</span>
                  <span class="cd-li-count">{{ s.count }} 项</span>
                </li>
              </ul>
              <span class="cd-face-tip">点击展开</span>
            </section>

            <!-- top 历程 -->
            <section class="cd-face cd-face-top" data-face="top" style="--acc: #f5a623">
              <span class="cd-face-tag">05 · 历程</span>
              <ul class="cd-face-body cd-face-list">
                <li v-for="e in recentExp" :key="e.id">
                  <span class="cd-li-year">{{ e.period }}</span>
                  <span class="cd-li-title">{{ e.title }}</span>
                </li>
              </ul>
              <span class="cd-face-tip">点击展开</span>
            </section>

            <!-- bottom 联系 -->
            <section class="cd-face cd-face-bottom" data-face="bottom" style="--acc: #10b981">
              <span class="cd-face-tag">06 · 联系</span>
              <div class="cd-face-body cd-face-contact">
                <Mail :size="22" :stroke-width="1.5" />
                <span class="cd-face-email">{{ data.profile.email }}</span>
                <span class="cd-face-loc">{{ data.profile.location }}</span>
              </div>
              <span class="cd-face-tip">点击展开</span>
            </section>
          </div>
        </div>
      </div>

      <p class="cd-hint">拖拽旋转立方 · 点击任意面或下方导航展开详情</p>

      <!-- 底部导航 -->
      <nav class="cd-nav" aria-label="信息板块">
        <button type="button" data-nav="front"><i style="--acc: #7c6cf6"></i>封面</button>
        <button type="button" data-nav="right"><i style="--acc: #ec5fa8"></i>关于</button>
        <button type="button" data-nav="back"><i style="--acc: #4d8dff"></i>作品</button>
        <button type="button" data-nav="left"><i style="--acc: #1fb6d4"></i>技能</button>
        <button type="button" data-nav="top"><i style="--acc: #f5a623"></i>历程</button>
        <button type="button" data-nav="bottom"><i style="--acc: #10b981"></i>联系</button>
      </nav>
    </main>

    <!-- 展开面板 -->
    <aside class="cd-panel" aria-label="详情面板">
      <button class="cd-close" type="button" aria-label="关闭详情">
        <X :size="20" :stroke-width="1.75" />
      </button>

      <!-- 封面 -->
      <div class="cd-view" data-panel="front">
        <div class="cd-view-head cd-view-head-cover">
          <span class="cd-avatar">{{ initials }}</span>
          <div>
            <strong class="cd-view-name">{{ data.profile.name }}</strong>
            <span class="cd-view-nameen">{{ data.profile.nameEn }}</span>
          </div>
        </div>
        <p class="cd-view-title">{{ data.profile.title }}</p>
        <p class="cd-view-tagline">{{ data.profile.tagline }}</p>
        <div class="cd-stats">
          <div v-for="s in stats" :key="s.label" class="cd-stat">
            <strong>{{ s.value }}</strong>
            <span>{{ s.label }}</span>
          </div>
        </div>
        <p class="cd-copyright">© {{ new Date().getFullYear() }} {{ data.profile.name }} · 基于 WorksShow 构建</p>
      </div>

      <!-- 关于 -->
      <div class="cd-view" data-panel="right">
        <h3 class="cd-view-h">关于我</h3>
        <p class="cd-view-text">{{ data.profile.bio }}</p>
        <ul class="cd-view-meta">
          <li><MapPin :size="15" :stroke-width="1.75" /><span>{{ data.profile.location }}</span></li>
          <li><Clock :size="15" :stroke-width="1.75" /><span>{{ data.profile.availability }}</span></li>
        </ul>
      </div>

      <!-- 作品 -->
      <div class="cd-view" data-panel="back">
        <h3 class="cd-view-h">项目作品</h3>
        <ul class="cd-works">
          <li
            v-for="w in data.works"
            :key="w.id"
            :data-work-id="w.id"
            role="button"
            tabindex="0"
            :aria-label="w.title"
          >
            <span class="cd-work-cover">
              <img :src="w.cover" :alt="w.title" loading="lazy" />
            </span>
            <span class="cd-work-info">
              <strong class="cd-work-title">{{ w.title }}</strong>
              <span class="cd-work-sub">{{ w.category }} · {{ w.year }}</span>
              <span class="cd-work-tags">
                <i v-for="t in w.tags.slice(0, 3)" :key="t">{{ t }}</i>
              </span>
            </span>
            <ArrowUpRight :size="16" :stroke-width="2" class="cd-work-arrow" />
          </li>
        </ul>
      </div>

      <!-- 技能 -->
      <div class="cd-view" data-panel="left">
        <h3 class="cd-view-h">技能矩阵</h3>
        <div v-for="g in data.skills" :key="g.id" class="cd-skill-group">
          <span class="cd-skill-cat">{{ g.category }}<i>{{ g.categoryEn }}</i></span>
          <div class="cd-skill-items">
            <span v-for="item in g.items" :key="item">{{ item }}</span>
          </div>
        </div>
      </div>

      <!-- 历程 -->
      <div class="cd-view" data-panel="top">
        <h3 class="cd-view-h">成长历程</h3>
        <ol class="cd-timeline">
          <li v-for="e in data.experiences" :key="e.id">
            <span class="cd-tl-period">{{ e.period }}</span>
            <strong class="cd-tl-title">{{ e.title }}</strong>
            <span class="cd-tl-org">{{ e.organization }}</span>
            <p class="cd-tl-desc">{{ e.description }}</p>
          </li>
        </ol>
      </div>

      <!-- 联系 -->
      <div class="cd-view" data-panel="bottom">
        <h3 class="cd-view-h">联系方式</h3>
        <p class="cd-view-text">有想法？随时聊聊，通常 24 小时内回复。</p>
        <ul class="cd-view-meta">
          <li><Mail :size="15" :stroke-width="1.75" /><a :href="'mailto:' + data.profile.email">{{ data.profile.email }}</a></li>
          <li><MapPin :size="15" :stroke-width="1.75" /><span>{{ data.profile.location }}</span></li>
          <li><Clock :size="15" :stroke-width="1.75" /><span>{{ data.profile.availability }}</span></li>
        </ul>
        <a class="cd-mailto" :href="'mailto:' + data.profile.email">
          发邮件给我 <ArrowUpRight :size="16" :stroke-width="2" />
        </a>
        <div class="cd-socials">
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
      </div>
    </aside>

    <!-- 作品弹窗 -->
    <div v-for="w in data.works" :key="w.id" class="cd-modal" :data-modal-id="w.id">
      <div class="modal-overlay"></div>
      <article class="cd-modal-card">
        <button class="modal-close" aria-label="关闭" type="button">
          <X :size="20" :stroke-width="1.75" />
        </button>
        <div class="cd-modal-cover">
          <img :src="w.cover" :alt="w.title" loading="lazy" />
        </div>
        <div class="cd-modal-body">
          <span class="cd-modal-cat">{{ w.category }} · {{ w.year }}</span>
          <h3 class="cd-modal-title">{{ w.title }}</h3>
          <p class="cd-modal-desc">{{ w.description }}</p>
          <div class="cd-modal-tags"><i v-for="t in w.tags" :key="t">{{ t }}</i></div>
          <a
            v-if="w.link"
            class="cd-mailto cd-modal-link"
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
/* —— 设计令牌：瓷白浅色 + 六面六色 —— */
.cd-root {
  --cd-ink: #232a4d;
  --cd-ink-soft: #4a5378;
  --cd-ink-mute: #6d7699;
  --cd-ink-faint: #98a1c0;
  --cd-line: rgba(255, 255, 255, 0.85);
  --cd-glass: rgba(255, 255, 255, 0.72);
  --cd-cube: clamp(240px, 48vmin, 380px);
  background: linear-gradient(160deg, #f4f5fb 0%, #eef0ff 45%, #fbf0f7 100%);
  color: var(--cd-ink);
  font-family: 'Noto Sans SC', 'Manrope', system-ui, -apple-system, sans-serif;
  font-size: 15px;
  line-height: 1.7;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  -webkit-font-smoothing: antialiased;
}
.cd-root * {
  box-sizing: border-box;
}

.cd-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}
.cd-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.55;
}
.cd-blob-1 {
  width: 480px;
  height: 480px;
  top: -140px;
  left: -100px;
  background: radial-gradient(circle, rgba(124, 108, 246, 0.3), transparent 70%);
}
.cd-blob-2 {
  width: 460px;
  height: 460px;
  bottom: -160px;
  right: -120px;
  background: radial-gradient(circle, rgba(236, 95, 168, 0.24), transparent 70%);
}
.cd-blob-3 {
  width: 420px;
  height: 420px;
  top: 34%;
  right: 22%;
  background: radial-gradient(circle, rgba(31, 182, 212, 0.18), transparent 70%);
}

.cd-top,
.cd-main,
.cd-panel,
.cd-modal {
  position: relative;
  z-index: 2;
}

/* —— 顶栏 —— */
.cd-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px clamp(20px, 4vw, 44px) 0;
}
.cd-brand {
  display: inline-flex;
  align-items: center;
  gap: 11px;
  text-decoration: none;
  color: var(--cd-ink);
}
.cd-brand-mark {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  font-weight: 800;
  font-size: 14px;
  color: #fff;
  background: linear-gradient(120deg, #7c6cf6, #4d8dff);
  border-radius: 11px;
  box-shadow: 0 10px 22px -8px rgba(124, 108, 246, 0.65);
}
.cd-brand-name {
  font-weight: 700;
  font-size: 15px;
}
.cd-avail {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  font-size: 12.5px;
  font-weight: 600;
  color: #0e9f6e;
  background: rgba(52, 211, 153, 0.12);
  border: 1px solid rgba(52, 211, 153, 0.3);
  border-radius: 999px;
}
.cd-avail i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 10px #34d399;
  animation: cd-pulse 2s ease-in-out infinite;
}
@keyframes cd-pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

/* —— 主舞台 —— */
.cd-main {
  min-height: calc(100vh - 74px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: clamp(22px, 4vh, 40px);
  padding: 30px 16px 26px;
}
.cd-stage {
  width: var(--cd-cube);
  height: var(--cd-cube);
  perspective: 1500px;
  cursor: grab;
  touch-action: none;
  -webkit-user-select: none;
  user-select: none;
  animation: cd-enter 0.9s cubic-bezier(0.22, 1, 0.36, 1) both;
}
@keyframes cd-enter {
  from { opacity: 0; transform: scale(0.7); }
  to { opacity: 1; transform: scale(1); }
}
.cd-stage:active {
  cursor: grabbing;
}
.cd-cube-scale {
  width: 100%;
  height: 100%;
  transition: transform 0.55s cubic-bezier(0.22, 1, 0.36, 1);
}
.cd-root.is-open .cd-cube-scale {
  transform: scale(0.78) translateX(clamp(-150px, -13vw, -46px));
}
.cd-cube {
  position: relative;
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
}

/* —— 六个面 —— */
.cd-face {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.92), rgba(255, 255, 255, 0.7));
  border: 1px solid rgba(255, 255, 255, 0.95);
  box-shadow:
    inset 0 0 0 1px color-mix(in srgb, var(--acc) 18%, transparent),
    inset 0 0 34px color-mix(in srgb, var(--acc) 10%, transparent),
    0 18px 44px -18px color-mix(in srgb, var(--acc) 38%, transparent);
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
  overflow: hidden;
  cursor: pointer;
}
.cd-face-tag {
  align-self: flex-start;
  padding: 4px 11px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  color: var(--acc);
  background: color-mix(in srgb, var(--acc) 10%, transparent);
  border: 1px solid color-mix(in srgb, var(--acc) 26%, transparent);
  border-radius: 999px;
  margin-bottom: 12px;
  white-space: nowrap;
}
.cd-face-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 7px;
  min-height: 0;
}
.cd-face-tip {
  font-size: 11px;
  color: var(--cd-ink-faint);
  text-align: center;
  letter-spacing: 0.08em;
}
.cd-face-front { transform: translateZ(calc(var(--cd-cube) / 2)); }
.cd-face-back { transform: rotateY(180deg) translateZ(calc(var(--cd-cube) / 2)); }
.cd-face-right { transform: rotateY(90deg) translateZ(calc(var(--cd-cube) / 2)); }
.cd-face-left { transform: rotateY(-90deg) translateZ(calc(var(--cd-cube) / 2)); }
.cd-face-top { transform: rotateX(90deg) translateZ(calc(var(--cd-cube) / 2)); }
.cd-face-bottom { transform: rotateX(-90deg) translateZ(calc(var(--cd-cube) / 2)); }

.cd-face-cover { text-align: center; align-items: center; }
.cd-face-eyebrow {
  font-size: 12px;
  font-weight: 600;
  color: var(--cd-ink-mute);
  letter-spacing: 0.06em;
}
.cd-face-name {
  font-size: clamp(26px, 9vmin, 40px);
  font-weight: 900;
  letter-spacing: 0.04em;
  line-height: 1.2;
}
.cd-face-nameen {
  font-family: 'Manrope', sans-serif;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.3em;
  text-transform: uppercase;
  color: var(--cd-ink-faint);
}
.cd-face-tagline {
  font-size: 12.5px;
  color: var(--cd-ink-mute);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.cd-face-bio {
  font-size: 12.5px;
  color: var(--cd-ink-soft);
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 6;
  line-clamp: 6;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.cd-face-meta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--acc);
  font-weight: 600;
}
.cd-face-list {
  list-style: none;
  margin: 0;
  padding: 0;
  gap: 10px;
}
.cd-face-list li {
  display: flex;
  align-items: baseline;
  gap: 9px;
  font-size: 13px;
  border-bottom: 1px dashed rgba(35, 42, 77, 0.1);
  padding-bottom: 8px;
}
.cd-face-list li:last-child {
  border-bottom: 0;
  padding-bottom: 0;
}
.cd-li-year {
  flex-shrink: 0;
  font-family: 'Manrope', sans-serif;
  font-size: 11px;
  font-weight: 700;
  color: var(--acc);
}
.cd-li-title {
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cd-li-count {
  margin-left: auto;
  font-size: 11px;
  color: var(--cd-ink-faint);
  flex-shrink: 0;
}
.cd-face-contact { align-items: center; text-align: center; }
.cd-face-contact :deep(svg) {
  color: var(--acc);
}
.cd-face-email {
  font-family: 'Manrope', 'Noto Sans SC', sans-serif;
  font-size: 13px;
  font-weight: 700;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cd-face-loc {
  font-size: 12px;
  color: var(--cd-ink-mute);
}

/* —— 提示与导航 —— */
.cd-hint {
  font-size: 13px;
  color: var(--cd-ink-mute);
  letter-spacing: 0.04em;
  text-align: center;
}
.cd-nav {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  padding: 8px;
  background: var(--cd-glass);
  border: 1px solid var(--cd-line);
  border-radius: 999px;
  backdrop-filter: blur(16px) saturate(150%);
  -webkit-backdrop-filter: blur(16px) saturate(150%);
  box-shadow: 0 14px 40px -18px rgba(84, 76, 199, 0.3);
}
.cd-nav button {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 16px;
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  color: var(--cd-ink-mute);
  background: transparent;
  border: 0;
  border-radius: 999px;
  cursor: pointer;
  transition: color 0.2s ease, background 0.2s ease, box-shadow 0.2s ease;
}
.cd-nav button i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--acc, #7c6cf6);
  box-shadow: 0 0 8px var(--acc, #7c6cf6);
}
.cd-nav button:hover {
  color: var(--cd-ink);
  background: rgba(255, 255, 255, 0.55);
}
.cd-nav button.is-active {
  color: #fff;
  background: #232a4d;
}

/* —— 展开面板 —— */
.cd-panel {
  position: fixed;
  z-index: 60;
  top: 50%;
  right: clamp(14px, 3vw, 36px);
  width: min(400px, calc(100vw - 28px));
  max-height: 84vh;
  overflow-y: auto;
  padding: 28px 28px 26px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(255, 255, 255, 0.95);
  border-radius: 26px;
  box-shadow: 0 34px 80px -24px rgba(46, 51, 96, 0.4);
  backdrop-filter: blur(22px) saturate(160%);
  -webkit-backdrop-filter: blur(22px) saturate(160%);
  transform: translateY(-50%) translateX(calc(100% + 60px));
  transition: transform 0.55s cubic-bezier(0.22, 1, 0.36, 1);
}
.cd-root.is-open .cd-panel {
  transform: translateY(-50%) translateX(0);
}
.cd-close {
  position: sticky;
  top: 0;
  margin-left: calc(100% - 38px);
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  color: var(--cd-ink-soft);
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--cd-line);
  border-radius: 50%;
  cursor: pointer;
  transition: transform 0.2s ease, color 0.2s ease, background 0.2s ease;
}
.cd-close:hover {
  color: #7c6cf6;
  background: rgba(124, 108, 246, 0.12);
  transform: rotate(90deg);
}
.cd-view {
  display: none;
}
.cd-view.is-active {
  display: block;
  animation: cd-view-in 0.45s cubic-bezier(0.22, 1, 0.36, 1) both;
}
@keyframes cd-view-in {
  from { opacity: 0; transform: translateY(14px); }
  to { opacity: 1; transform: none; }
}
.cd-view-head-cover {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
}
.cd-avatar {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  font-size: 21px;
  font-weight: 800;
  color: #fff;
  background: linear-gradient(120deg, #7c6cf6, #ec5fa8);
  border-radius: 17px;
  box-shadow: 0 12px 26px -8px rgba(124, 108, 246, 0.6);
}
.cd-view-name {
  display: block;
  font-size: 20px;
  font-weight: 800;
}
.cd-view-nameen {
  font-family: 'Manrope', sans-serif;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.26em;
  text-transform: uppercase;
  color: var(--cd-ink-faint);
}
.cd-view-title {
  font-size: 14px;
  font-weight: 700;
  color: #7c6cf6;
  margin: 0 0 8px;
}
.cd-view-tagline {
  font-size: 13.5px;
  color: var(--cd-ink-soft);
  line-height: 1.8;
  margin: 0 0 18px;
}
.cd-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.cd-stat {
  padding: 12px 10px;
  text-align: center;
  background: rgba(124, 108, 246, 0.06);
  border: 1px solid rgba(124, 108, 246, 0.16);
  border-radius: 14px;
}
.cd-stat strong {
  display: block;
  font-family: 'Manrope', sans-serif;
  font-size: 20px;
  font-weight: 800;
  background: linear-gradient(120deg, #7c6cf6, #ec5fa8);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.cd-stat span {
  font-size: 11.5px;
  color: var(--cd-ink-mute);
}
.cd-copyright {
  margin: 18px 0 0;
  font-size: 11.5px;
  color: var(--cd-ink-faint);
  text-align: center;
}
.cd-view-h {
  font-size: 18px;
  font-weight: 800;
  margin: 4px 0 14px;
}
.cd-view-text {
  font-size: 13.5px;
  color: var(--cd-ink-soft);
  line-height: 1.9;
  margin: 0 0 16px;
}
.cd-view-meta {
  list-style: none;
  margin: 0 0 16px;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.cd-view-meta li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13.5px;
  color: var(--cd-ink-soft);
}
.cd-view-meta li :deep(svg) {
  color: #7c6cf6;
  flex-shrink: 0;
}
.cd-view-meta a {
  color: var(--cd-ink-soft);
  text-decoration: none;
  font-family: 'Manrope', 'Noto Sans SC', sans-serif;
}
.cd-view-meta a:hover {
  color: #7c6cf6;
}
.cd-mailto {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 12px 22px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  text-decoration: none;
  background: linear-gradient(120deg, #7c6cf6, #ec5fa8);
  border-radius: 999px;
  box-shadow: 0 14px 30px -10px rgba(124, 108, 246, 0.6);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.cd-mailto:hover {
  transform: translateY(-2px);
}
.cd-socials {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}
.cd-socials a {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 8px 14px;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--cd-ink-soft);
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid var(--cd-line);
  border-radius: 999px;
  text-decoration: none;
  transition: color 0.2s ease, border-color 0.2s ease;
}
.cd-socials a:hover {
  color: #7c6cf6;
  border-color: rgba(124, 108, 246, 0.4);
}
.cd-socials a :deep(svg) {
  color: #7c6cf6;
}

/* —— 作品列表 —— */
.cd-works {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.cd-works li {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 11px;
  background: rgba(255, 255, 255, 0.66);
  border: 1px solid var(--cd-line);
  border-radius: 16px;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}
.cd-works li:hover {
  transform: translateY(-2px);
  border-color: rgba(77, 141, 255, 0.45);
  box-shadow: 0 16px 34px -16px rgba(77, 141, 255, 0.4);
}
.cd-works li:focus-visible {
  outline: 2px solid #4d8dff;
  outline-offset: 2px;
}
.cd-work-cover {
  flex-shrink: 0;
  width: 52px;
  height: 52px;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(238, 240, 255, 0.8);
}
.cd-work-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cd-work-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.cd-work-title {
  font-size: 14.5px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cd-work-sub {
  font-size: 12px;
  color: var(--cd-ink-mute);
}
.cd-work-tags {
  display: flex;
  gap: 5px;
  margin-top: 3px;
}
.cd-work-tags i,
.cd-modal-tags i {
  font-style: normal;
  padding: 2px 8px;
  font-size: 10.5px;
  color: var(--cd-ink-soft);
  background: rgba(77, 141, 255, 0.08);
  border: 1px solid rgba(77, 141, 255, 0.2);
  border-radius: 999px;
}
.cd-work-arrow {
  color: var(--cd-ink-faint);
  flex-shrink: 0;
}

/* —— 技能 —— */
.cd-skill-group {
  margin-bottom: 16px;
}
.cd-skill-group:last-child {
  margin-bottom: 0;
}
.cd-skill-cat {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 14.5px;
  font-weight: 700;
  margin-bottom: 8px;
}
.cd-skill-cat i {
  font-style: normal;
  font-family: 'Manrope', sans-serif;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--cd-ink-faint);
}
.cd-skill-items {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.cd-skill-items span {
  padding: 5px 12px;
  font-size: 12px;
  color: var(--cd-ink-soft);
  background: rgba(31, 182, 212, 0.07);
  border: 1px solid rgba(31, 182, 212, 0.22);
  border-radius: 999px;
  font-family: 'Manrope', 'Noto Sans SC', sans-serif;
}

/* —— 历程 —— */
.cd-timeline {
  list-style: none;
  margin: 0;
  padding: 0 0 0 18px;
  position: relative;
}
.cd-timeline::before {
  content: '';
  position: absolute;
  left: 4px;
  top: 6px;
  bottom: 6px;
  width: 2px;
  border-radius: 2px;
  background: linear-gradient(180deg, #7c6cf6, #ec5fa8, #f5a623);
}
.cd-timeline li {
  position: relative;
  padding-bottom: 16px;
}
.cd-timeline li:last-child {
  padding-bottom: 0;
}
.cd-timeline li::before {
  content: '';
  position: absolute;
  left: -18px;
  top: 5px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #fff;
  border: 3px solid #f5a623;
}
.cd-tl-period {
  display: inline-block;
  font-family: 'Manrope', sans-serif;
  font-size: 11px;
  font-weight: 700;
  color: #f5a623;
  margin-bottom: 3px;
}
.cd-tl-title {
  display: block;
  font-size: 14.5px;
  font-weight: 700;
}
.cd-tl-org {
  font-size: 12.5px;
  color: #1fb6d4;
  font-weight: 600;
}
.cd-tl-desc {
  font-size: 12.5px;
  color: var(--cd-ink-mute);
  line-height: 1.7;
  margin: 5px 0 0;
}

/* —— 作品弹窗 —— */
.cd-modal {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: none;
  align-items: center;
  justify-content: center;
  padding: 18px;
}
.cd-modal.is-open {
  display: flex;
}
.modal-overlay {
  position: absolute;
  inset: 0;
  background: rgba(46, 51, 96, 0.42);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}
.cd-modal-card {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1.1fr;
  max-width: 780px;
  width: 100%;
  max-height: 86vh;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid #fff;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 40px 90px -30px rgba(46, 51, 96, 0.45);
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
  color: var(--cd-ink-soft);
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--cd-line);
  border-radius: 50%;
  cursor: pointer;
  transition: transform 0.2s ease, color 0.2s ease, background 0.2s ease;
}
.modal-close:hover {
  color: #7c6cf6;
  background: rgba(124, 108, 246, 0.12);
  transform: rotate(90deg);
}
.cd-modal-cover {
  min-height: 260px;
  overflow: hidden;
  background: rgba(238, 240, 255, 0.8);
}
.cd-modal-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cd-modal-body {
  padding: 32px 32px 28px;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.cd-modal-cat {
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #4d8dff;
  margin-bottom: 10px;
}
.cd-modal-title {
  font-size: 22px;
  font-weight: 800;
  margin: 0 0 12px;
  line-height: 1.3;
}
.cd-modal-desc {
  font-size: 13.5px;
  color: var(--cd-ink-mute);
  line-height: 1.85;
  margin: 0 0 18px;
}
.cd-modal-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 20px;
}
.cd-modal-link {
  align-self: flex-start;
  margin-top: auto;
}

/* —— 减少动效 —— */
@media (prefers-reduced-motion: reduce) {
  .cd-root *,
  .cd-root *::before,
  .cd-root *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* —— 响应式 —— */
@media (max-width: 720px) {
  .cd-panel {
    top: auto;
    bottom: 12px;
    left: 12px;
    right: 12px;
    width: auto;
    max-height: 52vh;
    padding: 20px;
    transform: translateY(calc(100% + 40px));
  }
  .cd-root.is-open .cd-panel {
    transform: translateY(0);
  }
  .cd-root.is-open .cd-cube-scale {
    transform: scale(0.66) translateY(-58px);
  }
  .cd-main {
    min-height: calc(100vh - 66px);
  }
  .cd-modal-card {
    grid-template-columns: 1fr;
    max-height: 88vh;
    overflow-y: auto;
  }
  .cd-modal-cover {
    min-height: 170px;
    max-height: 200px;
  }
}
</style>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { safeUrl } from '@/utils/safeUrl'
import { X, ArrowUpRight, Mail, MapPin, Github } from 'lucide-vue-next'
import type { PortfolioData } from '../types'
import { VIRUS_ENGINE } from './engine'

const props = defineProps<{ data: PortfolioData }>()

const rootEl = ref<HTMLElement | null>(null)
let dispose: (() => void) | null = null

/** 病毒感染生命周期六阶段，顺序必须与 [data-panel] / [data-nav] / [data-hd] 一致 */
const stages = [
  { cn: '觉醒', en: 'Awakening', num: 'I', line: '一份来自深空的病原档案苏醒' },
  { cn: '附着', en: 'Attachment', num: 'II', line: '病毒贴近宿主——附着，正是认识我的开始' },
  { cn: '注入', en: 'Penetration', num: 'III', line: '遗传物质注入，数据沿轨道汇入' },
  { cn: '复制', en: 'Replication', num: 'IV', line: '病毒开始复制，每个子体都是一件作品' },
  { cn: '装配', en: 'Assembly', num: 'V', line: '蛋白质亚基按序列扫描，装配成技能' },
  { cn: '释放', en: 'Release', num: 'VI', line: '裂解释放——现在，轮到你被感染' },
] as const

onMounted(() => {
  if (!rootEl.value) return
  // 与导出 HTML 共用同一份引擎源码，保证预览与导出交互完全一致
  const create = new Function('return ' + VIRUS_ENGINE)() as (root: HTMLElement) => () => void
  dispose = create(rootEl.value)
})

onBeforeUnmount(() => {
  dispose?.()
  dispose = null
})
</script>

<template>
  <div ref="rootEl" class="vi-root">
    <div class="vi-stage">
      <canvas class="vi-canvas"></canvas>
      <!-- 子病毒热点：复制阶段显示，坐标由引擎写入 -->
      <button
        v-for="(w, i) in data.works.slice(0, 9)"
        :key="w.id"
        class="vi-hot"
        type="button"
        :data-wv="i"
        :data-work-id="w.id"
      >
        <i></i>
        <span>
          <b>{{ w.title }}</b>
          <em>{{ w.category }}</em>
        </span>
      </button>
    </div>

    <!-- 显微镜 HUD：四角 -->
    <header class="vi-corn vi-corn-tl">
      <span class="vi-logo">◈</span>
      <div class="vi-id">
        <b>{{ data.profile.name }}</b>
        <i>VIRION · SPECIMEN 001</i>
      </div>
    </header>
    <div class="vi-corn vi-corn-tr">
      <i>活性 98.6%</i>
      <i>×80,000</i>
      <i>37.0°C</i>
    </div>

    <!-- 左下：阶段大标题 -->
    <div class="vi-phase">
      <div
        v-for="(s, i) in stages"
        :key="s.en"
        class="vi-phase-item"
        :data-hd="i"
      >
        <span class="vi-phase-num">{{ s.num }}</span>
        <div class="vi-phase-txt">
          <h2>{{ s.cn }}<i>{{ s.en }}</i></h2>
          <p>{{ s.line }}</p>
        </div>
      </div>
    </div>

    <!-- 右下：信息面板 -->
    <main class="vi-info">
      <!-- 0 觉醒 -->
      <section class="vi-view" data-panel="0">
        <span class="vi-kicker">{{ data.profile.title }}</span>
        <h1 class="vi-title">{{ data.profile.name }}</h1>
        <span class="vi-latin">{{ data.profile.nameEn }}</span>
        <p class="vi-lede">{{ data.profile.tagline }}</p>
        <div class="vi-stats">
          <div v-for="s in data.profile.stats" :key="s.label">
            <strong>{{ s.value }}</strong>
            <span>{{ s.label }}</span>
          </div>
        </div>
        <p class="vi-hint">拖拽旋转病毒 · 点击病毒推进生命周期 · 或用底部进度条</p>
      </section>

      <!-- 1 附着 -->
      <section class="vi-view" data-panel="1">
        <h3 class="vi-h">关于宿主</h3>
        <p class="vi-p">{{ data.profile.bio }}</p>
        <dl class="vi-facts">
          <div><dt>坐标</dt><dd>{{ data.profile.location }}</dd></div>
          <div><dt>状态</dt><dd>{{ data.profile.availability }}</dd></div>
        </dl>
        <p class="vi-hint">前侧衣壳已透明，可见内部 RNA 螺旋</p>
      </section>

      <!-- 2 注入 -->
      <section class="vi-view" data-panel="2">
        <h3 class="vi-h">注入数据</h3>
        <div class="vi-stats">
          <div v-for="s in data.profile.stats" :key="s.label">
            <strong>{{ s.value }}</strong>
            <span>{{ s.label }}</span>
          </div>
        </div>
        <p class="vi-hint">{{ data.profile.stats.length }} 组数据正沿螺旋轨道汇入</p>
      </section>

      <!-- 3 复制 -->
      <section class="vi-view" data-panel="3">
        <h3 class="vi-h">复制子体</h3>
        <p class="vi-p">病毒已完成复制——每个漂浮的子病毒都是一件作品，点击任意子体查看详情。</p>
        <ul class="vi-works">
          <li
            v-for="w in data.works.slice(0, 9)"
            :key="w.id"
            :data-work-id="w.id"
            role="button"
            tabindex="0"
          >
            <b>{{ w.title }}</b>
            <em>{{ w.category }} · {{ w.year }}</em>
          </li>
        </ul>
      </section>

      <!-- 4 装配 -->
      <section class="vi-view" data-panel="4">
        <h3 class="vi-h">装配序列</h3>
        <div v-for="g in data.skills" :key="g.id" class="vi-skill">
          <b>{{ g.category }}<i>{{ g.categoryEn }}</i></b>
          <p>{{ g.items.join(' · ') }}</p>
        </div>
      </section>

      <!-- 5 释放 -->
      <section class="vi-view" data-panel="5">
        <h3 class="vi-h">裂解释放</h3>
        <a class="vi-mail" :href="'mailto:' + data.profile.email">{{ data.profile.email }}</a>
        <dl class="vi-facts">
          <div><dt><MapPin :size="13" :stroke-width="1.75" />坐标</dt><dd>{{ data.profile.location }}</dd></div>
          <div><dt>回复</dt><dd>通常 24 小时内</dd></div>
        </dl>
        <div class="vi-links">
          <a
            v-for="s in data.profile.socials"
            :key="s.name"
            :href="safeUrl(s.url)"
            target="_blank"
            rel="noopener noreferrer"
          >
            <component :is="s.icon === 'github' ? Github : Mail" :size="13" :stroke-width="1.75" />
            {{ s.name }}<ArrowUpRight :size="12" :stroke-width="2" />
          </a>
        </div>
      </section>
    </main>

    <!-- 底部：感染进度 -->
    <nav class="vi-flow" aria-label="感染生命周期">
      <span class="vi-flow-line" aria-hidden="true"></span>
      <button v-for="(s, i) in stages" :key="s.en" type="button" :data-nav="i">
        <i></i>
        <span>{{ s.cn }}</span>
      </button>
    </nav>

    <!-- 作品弹窗 -->
    <div v-for="w in data.works" :key="w.id" class="vi-modal" :data-modal-id="w.id">
      <div class="modal-overlay"></div>
      <article class="vi-modal-card">
        <button class="modal-close" aria-label="关闭" type="button">
          <X :size="20" :stroke-width="1.75" />
        </button>
        <div class="vi-modal-cover">
          <img :src="w.cover" :alt="w.title" loading="lazy" />
        </div>
        <div class="vi-modal-body">
          <span class="vi-modal-cat">{{ w.category }} · {{ w.year }}</span>
          <h3>{{ w.title }}</h3>
          <p>{{ w.description }}</p>
          <div class="vi-modal-tags"><i v-for="t in w.tags" :key="t">{{ t }}</i></div>
          <a v-if="safeUrl(w.link)" class="vi-modal-link" :href="safeUrl(w.link)" target="_blank" rel="noopener noreferrer">
            查看项目 <ArrowUpRight :size="15" :stroke-width="2" />
          </a>
        </div>
      </article>
    </div>
  </div>
</template>

<style scoped>
/* —— 设计令牌：暗场显微镜 · 电镜青绿 × 病理品红 —— */
.vi-root {
  --bg: #02080a;
  --ink: #e8f6f1;
  --soft: #9fbdb4;
  --mute: #67807a;
  --faint: #3f5750;
  --teal: #4dd6bd;
  --teal-dim: rgba(77, 214, 189, 0.14);
  --mag: #ff4d7e;
  --amber: #ffb45e;
  --line: rgba(120, 200, 180, 0.14);
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(ellipse 60% 50% at 50% 44%, rgba(38, 92, 84, 0.22), transparent 65%),
    radial-gradient(ellipse 100% 80% at 50% 110%, rgba(20, 50, 48, 0.4), transparent 60%),
    var(--bg);
  color: var(--ink);
  font-family: 'Noto Sans SC', system-ui, -apple-system, sans-serif;
  font-size: 15px;
  line-height: 1.7;
  -webkit-font-smoothing: antialiased;
}
.vi-root::after {
  /* 暗角：显微镜视野 */
  content: '';
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(ellipse 88% 78% at 50% 46%, transparent 58%, rgba(0, 4, 5, 0.82) 100%);
}
.vi-root * {
  box-sizing: border-box;
}
.vi-stage {
  position: absolute;
  inset: 0;
  z-index: 0;
}
.vi-canvas {
  position: absolute;
  inset: 0;
  display: block;
  cursor: grab;
  touch-action: none;
}
.vi-canvas:active {
  cursor: grabbing;
}

/* —— 子病毒热点 —— */
.vi-hot {
  position: absolute;
  left: 0;
  top: 0;
  z-index: 3;
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 0;
  font-family: inherit;
  background: none;
  border: 0;
  cursor: pointer;
  opacity: 0;
  will-change: transform, opacity;
  color: var(--ink);
}
.vi-hot i {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--mag);
  box-shadow: 0 0 14px rgba(255, 77, 126, 0.9), 0 0 0 5px rgba(255, 77, 126, 0.15);
  flex-shrink: 0;
  transition: transform 0.25s ease;
}
.vi-hot span {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0;
  padding: 4px 10px;
  background: rgba(3, 12, 12, 0.78);
  border: 1px solid rgba(255, 77, 126, 0.3);
  border-radius: 4px;
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  transform: translateX(-4px);
  transition: border-color 0.2s ease, transform 0.25s ease;
}
.vi-hot b {
  font-size: 12.5px;
  font-weight: 700;
  white-space: nowrap;
}
.vi-hot em {
  font-style: normal;
  font-size: 10px;
  letter-spacing: 0.08em;
  color: var(--mute);
}
.vi-hot:hover i {
  transform: scale(1.5);
}
.vi-hot:hover span {
  border-color: var(--mag);
  transform: translateX(0);
}

/* —— 四角 HUD —— */
.vi-corn {
  position: absolute;
  z-index: 4;
  display: flex;
  align-items: center;
  gap: 12px;
}
.vi-corn-tl {
  top: 22px;
  left: clamp(18px, 3vw, 36px);
}
.vi-corn-tr {
  top: 26px;
  right: clamp(18px, 3vw, 36px);
  gap: 18px;
}
.vi-corn-tr i {
  font-style: normal;
  font-family: ui-monospace, 'SFMono-Regular', Menlo, Consolas, monospace;
  font-size: 10px;
  letter-spacing: 0.12em;
  color: var(--faint);
}
.vi-corn-tr i:first-child {
  color: var(--teal);
}
.vi-logo {
  font-size: 20px;
  color: var(--teal);
  text-shadow: 0 0 18px rgba(77, 214, 189, 0.8);
}
.vi-id b {
  display: block;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.1em;
}
.vi-id i {
  font-style: normal;
  font-family: ui-monospace, 'SFMono-Regular', Menlo, Consolas, monospace;
  font-size: 9px;
  letter-spacing: 0.22em;
  color: var(--faint);
}

/* —— 左下：阶段标题 —— */
.vi-phase {
  position: absolute;
  z-index: 4;
  left: clamp(18px, 3vw, 36px);
  bottom: 110px;
  pointer-events: none;
}
.vi-phase-item {
  display: none;
  align-items: flex-end;
  gap: 16px;
}
.vi-phase-item.is-active {
  display: flex;
  animation: vi-phase-in 0.55s cubic-bezier(0.22, 1, 0.36, 1) both;
}
@keyframes vi-phase-in {
  from { opacity: 0; transform: translateY(18px); }
  to { opacity: 1; transform: none; }
}
.vi-phase-num {
  font-family: 'Times New Roman', Georgia, serif;
  font-size: clamp(64px, 9vw, 110px);
  font-weight: 400;
  line-height: 0.8;
  color: transparent;
  -webkit-text-stroke: 1px rgba(77, 214, 189, 0.45);
}
.vi-phase-txt h2 {
  margin: 0 0 2px;
  font-family: 'Noto Serif SC', 'Noto Sans SC', serif;
  font-size: clamp(26px, 3vw, 36px);
  font-weight: 900;
  letter-spacing: 0.14em;
  line-height: 1.2;
}
.vi-phase-txt h2 i {
  font-style: normal;
  font-family: ui-monospace, 'SFMono-Regular', Menlo, Consolas, monospace;
  font-size: 10px;
  font-weight: 400;
  letter-spacing: 0.3em;
  text-transform: uppercase;
  color: var(--teal);
  margin-left: 12px;
  vertical-align: middle;
}
.vi-phase-txt p {
  margin: 0;
  font-size: 12.5px;
  color: var(--soft);
  letter-spacing: 0.06em;
}

/* —— 右下：信息面板 —— */
.vi-info {
  position: absolute;
  z-index: 4;
  right: clamp(18px, 3vw, 36px);
  bottom: 110px;
  width: min(390px, calc(100vw - 36px));
  max-height: 58vh;
  overflow-y: auto;
  overscroll-behavior: contain;
  padding: 4px 2px 4px 0;
  scrollbar-width: thin;
  scrollbar-color: rgba(77, 214, 189, 0.3) transparent;
}
.vi-view {
  display: none;
}
.vi-view.is-active {
  display: block;
  animation: vi-phase-in 0.5s cubic-bezier(0.22, 1, 0.36, 1) 0.06s both;
}
.vi-kicker {
  display: inline-block;
  padding: 4px 12px;
  font-family: ui-monospace, 'SFMono-Regular', Menlo, Consolas, monospace;
  font-size: 10px;
  letter-spacing: 0.2em;
  color: var(--teal);
  border: 1px solid rgba(77, 214, 189, 0.3);
  border-radius: 3px;
  margin-bottom: 12px;
}
.vi-title {
  margin: 0 0 4px;
  font-family: 'Noto Serif SC', 'Noto Sans SC', serif;
  font-size: clamp(34px, 4.6vw, 52px);
  font-weight: 900;
  line-height: 1.15;
  letter-spacing: 0.06em;
}
.vi-latin {
  display: block;
  font-family: ui-monospace, 'SFMono-Regular', Menlo, Consolas, monospace;
  font-size: 10px;
  letter-spacing: 0.34em;
  color: var(--faint);
  margin-bottom: 14px;
}
.vi-lede {
  margin: 0 0 16px;
  font-size: 13.5px;
  line-height: 1.85;
  color: var(--soft);
}
.vi-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 14px;
}
.vi-stats > div {
  padding: 10px 6px;
  text-align: center;
  background: var(--teal-dim);
  border: 1px solid rgba(77, 214, 189, 0.18);
  border-radius: 6px;
}
.vi-stats strong {
  display: block;
  font-family: ui-monospace, Menlo, Consolas, monospace;
  font-size: 19px;
  color: var(--teal);
}
.vi-stats span {
  font-size: 10.5px;
  color: var(--mute);
}
.vi-h {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 800;
  letter-spacing: 0.12em;
  color: var(--ink);
}
.vi-h::after {
  content: '';
  display: block;
  width: 34px;
  height: 2px;
  margin-top: 7px;
  background: linear-gradient(90deg, var(--mag), transparent);
}
.vi-p {
  margin: 0 0 14px;
  font-size: 13px;
  line-height: 1.9;
  color: var(--soft);
}
.vi-facts {
  margin: 0 0 14px;
}
.vi-facts > div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 7px 0;
  border-bottom: 1px dashed var(--line);
}
.vi-facts dt {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 10.5px;
  letter-spacing: 0.14em;
  color: var(--faint);
}
.vi-facts dt :deep(svg) {
  color: var(--mag);
}
.vi-facts dd {
  margin: 0;
  font-size: 13px;
}
.vi-hint {
  margin: 2px 0 0;
  padding-top: 10px;
  font-family: ui-monospace, 'SFMono-Regular', Menlo, Consolas, monospace;
  font-size: 10px;
  letter-spacing: 0.06em;
  color: var(--faint);
  border-top: 1px dashed var(--line);
}
.vi-works {
  list-style: none;
  margin: 0;
  padding: 0;
}
.vi-works li {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 6px;
  border-bottom: 1px dashed var(--line);
  cursor: pointer;
  transition: background 0.2s ease, padding-left 0.2s ease;
}
.vi-works li:hover {
  padding-left: 12px;
  background: linear-gradient(90deg, rgba(255, 77, 126, 0.1), transparent 75%);
}
.vi-works b {
  font-size: 13.5px;
}
.vi-works em {
  font-style: normal;
  font-family: ui-monospace, Menlo, Consolas, monospace;
  font-size: 10px;
  color: var(--mute);
  white-space: nowrap;
}
.vi-skill {
  padding: 9px 0;
  border-bottom: 1px dashed var(--line);
}
.vi-skill b {
  font-size: 13.5px;
}
.vi-skill b i {
  font-style: normal;
  font-family: ui-monospace, Menlo, Consolas, monospace;
  font-size: 9px;
  letter-spacing: 0.18em;
  color: var(--faint);
  margin-left: 8px;
}
.vi-skill p {
  margin: 3px 0 0;
  font-size: 12.5px;
  line-height: 1.85;
  color: var(--soft);
}
.vi-mail {
  display: inline-block;
  margin: 0 0 12px;
  font-family: ui-monospace, Menlo, Consolas, monospace;
  font-size: clamp(15px, 1.6vw, 18px);
  font-weight: 700;
  color: var(--ink);
  text-decoration: none;
  border-bottom: 1px solid rgba(255, 77, 126, 0.5);
  padding-bottom: 2px;
  transition: color 0.2s ease, border-color 0.2s ease;
}
.vi-mail:hover {
  color: var(--mag);
  border-color: var(--mag);
}
.vi-links {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
}
.vi-links a {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  color: var(--soft);
  text-decoration: none;
  transition: color 0.2s ease;
}
.vi-links a:hover {
  color: var(--teal);
}
.vi-links a :deep(svg) {
  color: var(--mag);
}
.vi-links a :deep(svg:last-child) {
  color: var(--faint);
}

/* —— 底部：感染进度 —— */
.vi-flow {
  position: absolute;
  z-index: 5;
  left: 50%;
  bottom: 30px;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 0;
  padding: 0 10px;
}
.vi-flow-line {
  position: absolute;
  left: 30px;
  right: 30px;
  top: 50%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(77, 214, 189, 0.3) 15%, rgba(255, 77, 126, 0.3) 85%, transparent);
  pointer-events: none;
}
.vi-flow button {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 0 16px;
  font-family: inherit;
  background: none;
  border: 0;
  cursor: pointer;
}
.vi-flow button i {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: var(--bg);
  border: 1.5px solid var(--faint);
  transition: all 0.3s ease;
}
.vi-flow button span {
  font-size: 10.5px;
  letter-spacing: 0.14em;
  color: var(--faint);
  transition: color 0.3s ease;
}
.vi-flow button:hover i {
  border-color: var(--teal);
}
.vi-flow button.is-active i {
  background: var(--mag);
  border-color: var(--mag);
  box-shadow: 0 0 12px rgba(255, 77, 126, 0.9);
  transform: scale(1.25);
}
.vi-flow button.is-active span {
  color: var(--ink);
}

/* —— 弹窗 —— */
.vi-modal {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: none;
  align-items: center;
  justify-content: center;
  padding: 18px;
}
.vi-modal.is-open {
  display: flex;
}
.modal-overlay {
  position: absolute;
  inset: 0;
  background: rgba(1, 6, 7, 0.78);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}
.vi-modal-card {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1.1fr;
  width: 100%;
  max-width: 780px;
  max-height: 86vh;
  background: #061012;
  border: 1px solid rgba(77, 214, 189, 0.22);
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 40px 90px -30px rgba(0, 0, 0, 0.92), 0 0 60px -20px rgba(255, 77, 126, 0.25);
}
.modal-close {
  position: absolute;
  top: 14px;
  right: 14px;
  z-index: 2;
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  color: var(--soft);
  background: rgba(6, 16, 18, 0.85);
  border: 1px solid var(--line);
  border-radius: 50%;
  cursor: pointer;
  transition: color 0.2s ease, border-color 0.2s ease;
}
.modal-close:hover {
  color: var(--mag);
  border-color: var(--mag);
}
.vi-modal-cover {
  min-height: 260px;
  overflow: hidden;
  background: rgba(77, 214, 189, 0.05);
}
.vi-modal-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.vi-modal-body {
  display: flex;
  flex-direction: column;
  padding: 32px 30px 28px;
  overflow-y: auto;
}
.vi-modal-cat {
  font-family: ui-monospace, Menlo, Consolas, monospace;
  font-size: 10px;
  letter-spacing: 0.18em;
  color: var(--mag);
  margin-bottom: 10px;
}
.vi-modal-body h3 {
  margin: 0 0 12px;
  font-family: 'Noto Serif SC', 'Noto Sans SC', serif;
  font-size: 22px;
  font-weight: 900;
}
.vi-modal-body > p {
  margin: 0 0 18px;
  font-size: 13.5px;
  line-height: 1.85;
  color: var(--soft);
}
.vi-modal-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 20px;
}
.vi-modal-tags i {
  font-style: normal;
  padding: 3px 10px;
  font-size: 10.5px;
  color: var(--teal);
  border: 1px solid rgba(77, 214, 189, 0.28);
  border-radius: 3px;
}
.vi-modal-link {
  align-self: flex-start;
  margin-top: auto;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--mag);
  text-decoration: none;
  border-bottom: 1px solid rgba(255, 77, 126, 0.4);
  padding-bottom: 2px;
}

/* —— 减少动效 —— */
@media (prefers-reduced-motion: reduce) {
  .vi-root *,
  .vi-root *::before,
  .vi-root *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* —— 响应式 —— */
@media (max-width: 860px) {
  .vi-corn-tr i:nth-child(2) {
    display: none;
  }
  .vi-phase {
    bottom: 172px;
  }
  .vi-phase-num {
    font-size: 52px;
  }
  .vi-phase-txt h2 {
    font-size: 22px;
  }
  .vi-phase-txt p {
    font-size: 11px;
  }
  .vi-info {
    right: 16px;
    left: 16px;
    bottom: 96px;
    width: auto;
    max-height: 40vh;
  }
  .vi-title {
    font-size: clamp(30px, 9vw, 40px);
  }
  .vi-flow {
    bottom: 20px;
    gap: 0;
  }
  .vi-flow button {
    padding: 0 8px;
  }
  .vi-flow button span {
    font-size: 9.5px;
  }
  .vi-modal-card {
    grid-template-columns: 1fr;
    max-height: 88vh;
    overflow-y: auto;
  }
  .vi-modal-cover {
    min-height: 150px;
    max-height: 190px;
  }
}
</style>

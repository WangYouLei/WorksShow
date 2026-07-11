<script setup lang="ts">
import { computed } from 'vue'
import {
  X,
  ArrowUpRight,
  ArrowUp,
  Mail,
  MapPin,
  Clock,
  Sparkles,
  Zap,
  Command,
  Github,
} from 'lucide-vue-next'
import type { PortfolioData } from '../types'

const props = defineProps<{ data: PortfolioData }>()

const featured = computed(() => props.data.works.find((w) => w.highlight) ?? props.data.works[0])
const rest = computed(() => props.data.works.filter((w) => w.id !== featured.value?.id))
const stats = computed(() => props.data.profile.stats)
const initials = computed(() => props.data.profile.name.slice(0, 1))
</script>

<template>
  <div class="a-page">
    <!-- 鼠标跟随光效 -->
    <div class="a-cursor-glow" aria-hidden="true"></div>
    <!-- 背景极光氛围层 -->
    <div class="a-aurora-bg" aria-hidden="true">
      <span class="aurora-blob aurora-blob-1"></span>
      <span class="aurora-blob aurora-blob-2"></span>
      <span class="aurora-grid"></span>
    </div>

    <!-- 导航栏 -->
    <nav class="a-navbar">
      <a class="a-brand" href="#top">
        <span class="brand-mark">{{ initials }}</span>
        <span class="brand-name">{{ data.profile.name }}</span>
      </a>
      <div class="nav-links">
        <a href="#works">项目作品</a>
        <a href="#skills">技术栈</a>
        <a href="#about">关于我</a>
        <a href="#timeline">项目历程</a>
        <a href="#contact">联系方式</a>
      </div>
      <a class="nav-cta" href="#contact">
        <span>联系我</span>
        <ArrowUpRight :size="14" :stroke-width="2" />
      </a>
    </nav>

    <main id="top">
      <!-- Hero -->
      <section class="a-hero">
        <div class="hero-inner">
          <div class="hero-left reveal">
            <span class="eyebrow">
              <span class="eyebrow-dot"></span>
              {{ data.profile.title }}
            </span>
            <h1 class="hero-title">
              把想法变成<br />
              <em>真正被使用</em>的产品
            </h1>
            <p class="hero-sub">{{ data.profile.tagline }}</p>
            <div class="hero-actions">
              <a class="btn btn-primary" href="#works">
                查看作品 <ArrowUpRight :size="16" :stroke-width="2" />
              </a>
              <a class="btn btn-ghost" href="#contact">联系我</a>
            </div>
            <div class="hero-stats">
              <div v-for="s in stats" :key="s.label" class="stat">
                <span class="stat-value">{{ s.value }}</span>
                <span class="stat-label">{{ s.label }}</span>
              </div>
            </div>
          </div>

          <!-- 右侧抽象科技视觉：玻璃拟态光卡 + 浮动数据条 -->
          <div class="hero-right reveal">
            <div class="visual-stage">
              <div class="visual-ring visual-ring-1"></div>
              <div class="visual-ring visual-ring-2"></div>
              <div class="visual-core">
                <span class="core-glyph">{{ initials }}</span>
                <span class="core-pulse"></span>
              </div>
              <div class="visual-chip visual-chip-1">
                <Zap :size="13" :stroke-width="2" />
                <span>实时在线</span>
              </div>
              <div class="visual-chip visual-chip-2">
                <Command :size="13" :stroke-width="2" />
                <span>全栈交付</span>
              </div>
              <div class="visual-chip visual-chip-3">
                <Sparkles :size="13" :stroke-width="2" />
                <span>AI 原生</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 项目展示 Bento Grid -->
      <section id="works" class="a-section">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">项目作品</span>
          <h2 class="sec-title">把每一个想法<br />打磨成可被使用的产品</h2>
          <p class="sec-sub">从 AI 应用、Web 产品到自动化工具，以下是独立设计与交付的代表作品</p>
        </header>

        <div class="bento-grid">
          <!-- 旗舰大卡 -->
          <article
            v-if="featured"
            class="bento-card bento-featured reveal"
            :data-work-id="featured.id"
            role="button"
            tabindex="0"
            :aria-label="featured.title"
          >
            <div class="featured-cover">
              <img :src="featured.cover" :alt="featured.title" loading="lazy" />
              <div class="featured-cover-veil"></div>
              <span class="featured-badge">旗舰作品</span>
            </div>
            <div class="featured-body">
              <div class="featured-meta">
                <span class="card-cat">{{ featured.category }}</span>
                <span class="card-year">{{ featured.year }}</span>
              </div>
              <h3 class="featured-title">{{ featured.title }}</h3>
              <p class="featured-desc">{{ featured.description }}</p>
              <div class="featured-tags">
                <span v-for="t in featured.tags.slice(0, 4)" :key="t" class="chip">{{ t }}</span>
              </div>
              <span class="featured-cta">
                查看详情 <ArrowUpRight :size="15" :stroke-width="2" />
              </span>
            </div>
          </article>

          <!-- 其余作品：Bento 小卡 -->
          <article
            v-for="w in rest"
            :key="w.id"
            class="bento-card bento-small reveal"
            :data-work-id="w.id"
            role="button"
            tabindex="0"
            :aria-label="w.title"
          >
            <div class="small-cover">
              <img :src="w.cover" :alt="w.title" loading="lazy" />
              <div class="small-cover-veil"></div>
              <span class="small-year">{{ w.year }}</span>
            </div>
            <div class="small-body">
              <span class="card-cat">{{ w.category }}</span>
              <h3 class="small-title">{{ w.title }}</h3>
              <p class="small-desc">{{ w.description }}</p>
              <div class="small-tags">
                <span v-for="t in w.tags.slice(0, 3)" :key="t" class="chip">{{ t }}</span>
              </div>
            </div>
          </article>
        </div>
      </section>

      <!-- 技术栈 -->
      <section id="skills" class="a-section a-section-glass">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">技术栈</span>
          <h2 class="sec-title">从想法到上线<br />全链路独立交付能力</h2>
          <p class="sec-sub">覆盖前端、后端、AI、数据库与云服务，独立完成产品研发全流程</p>
        </header>

        <div class="skills-grid">
          <article v-for="g in data.skills" :key="g.id" class="skill-card reveal">
            <div class="skill-head">
              <span class="skill-cn">{{ g.category }}</span>
              <span class="skill-en">{{ g.categoryEn }}</span>
            </div>
            <ul class="skill-list">
              <li v-for="item in g.items" :key="item">{{ item }}</li>
            </ul>
          </article>
        </div>
      </section>

      <!-- 关于我 -->
      <section id="about" class="a-section">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">关于我</span>
          <h2 class="sec-title">一个持续创造产品的人</h2>
        </header>
        <div class="about-grid">
          <div class="about-text reveal">
            <p class="about-lead">{{ data.profile.bio }}</p>
            <p class="about-extra">
              <MapPin :size="15" :stroke-width="1.75" />
              <span>{{ data.profile.location }}</span>
              <span class="dot-sep">·</span>
              <Clock :size="15" :stroke-width="1.75" />
              <span>{{ data.profile.availability }}</span>
            </p>
          </div>
          <div class="about-cards reveal">
            <div v-for="s in stats" :key="s.label" class="about-card">
              <span class="about-value">{{ s.value }}</span>
              <span class="about-label">{{ s.label }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 项目历程 -->
      <section id="timeline" class="a-section a-section-glass">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">项目历程</span>
          <h2 class="sec-title">每个里程碑<br />都是一次产品突破</h2>
        </header>

        <ol class="timeline">
          <li
            v-for="(e, i) in data.experiences"
            :key="e.id"
            class="tl-item reveal"
            :class="{ 'tl-item-right': i % 2 === 1 }"
            :style="{ '--i': i }"
          >
            <span class="tl-dot"></span>
            <div class="tl-content">
              <span class="tl-period">{{ e.period }}</span>
              <h3 class="tl-title">{{ e.title }}</h3>
              <span class="tl-org">{{ e.organization }}</span>
              <p class="tl-desc">{{ e.description }}</p>
            </div>
          </li>
        </ol>
      </section>

      <!-- 联系方式 -->
      <section id="contact" class="a-section">
        <div class="contact-card reveal">
          <div class="contact-glow"></div>
          <div class="contact-left">
            <span class="sec-eyebrow">联系方式</span>
            <h2 class="contact-title">有想法？<br />一起把它变成产品</h2>
            <p class="contact-sub">
              无论是合作机会、产品咨询，还是只是想打个招呼，我都很乐意聊聊。通常 24 小时内回复。
            </p>
            <ul class="contact-list">
              <li>
                <Mail :size="18" :stroke-width="1.75" />
                <a :href="'mailto:' + data.profile.email">{{ data.profile.email }}</a>
              </li>
              <li>
                <MapPin :size="18" :stroke-width="1.75" />
                <span>{{ data.profile.location }}</span>
              </li>
              <li>
                <Clock :size="18" :stroke-width="1.75" />
                <span>{{ data.profile.availability }}</span>
              </li>
            </ul>
          </div>
          <div class="contact-right">
            <a class="btn btn-primary btn-lg" :href="'mailto:' + data.profile.email">
              发邮件给我 <ArrowUpRight :size="18" :stroke-width="2" />
            </a>
            <a
              v-for="s in data.profile.socials"
              :key="s.name"
              class="social-link"
              :href="s.url"
              target="_blank"
              rel="noopener noreferrer"
            >
              <component :is="s.icon === 'github' ? Github : Mail" :size="15" :stroke-width="1.75" />
              <span>{{ s.name }}</span>
              <ArrowUpRight :size="13" :stroke-width="2" />
            </a>
          </div>
        </div>
      </section>
    </main>

    <footer class="a-footer">
      <div class="footer-inner">
        <span class="footer-brand">{{ initials }} · {{ data.profile.name }}</span>
        <span class="footer-copy">© {{ new Date().getFullYear() }} {{ data.profile.name }}. 基于 WorksShow 构建。</span>
      </div>
    </footer>

    <button class="back-top" type="button" aria-label="回到顶部">
      <ArrowUp :size="18" :stroke-width="2" />
    </button>

    <!-- 作品详情弹窗 -->
    <div
      v-for="w in data.works"
      :key="w.id"
      class="a-modal"
      :data-modal-id="w.id"
    >
      <div class="modal-overlay"></div>
      <article class="modal-card">
        <button class="modal-close" aria-label="关闭" type="button">
          <X :size="20" :stroke-width="1.75" />
        </button>
        <div class="modal-cover">
          <img :src="w.cover" :alt="w.title" loading="lazy" />
          <span class="modal-year">{{ w.year }}</span>
        </div>
        <div class="modal-body">
          <span class="modal-cat">{{ w.category }}</span>
          <h3 class="modal-title">{{ w.title }}</h3>
          <p class="modal-desc">{{ w.description }}</p>
          <div class="modal-tags">
            <span v-for="t in w.tags" :key="t" class="chip">{{ t }}</span>
          </div>
          <a
            v-if="w.link"
            class="btn btn-primary modal-link"
            :href="w.link"
            target="_blank"
            rel="noopener noreferrer"
          >
            查看项目 <ArrowUpRight :size="16" :stroke-width="2" />
          </a>
        </div>
      </article>
    </div>
  </div>
</template>

<style scoped>
/* —— 设计令牌 —— */
.a-page {
  --a-bg: #050507;
  --a-bg-soft: #0a0a0f;
  --a-surface: #101018;
  --a-surface-2: #15151f;
  --a-glass: rgba(255, 255, 255, 0.035);
  --a-glass-strong: rgba(255, 255, 255, 0.06);
  --a-border: rgba(255, 255, 255, 0.08);
  --a-border-strong: rgba(255, 255, 255, 0.14);
  --a-ink: #ffffff;
  --a-ink-soft: #c7c7d1;
  --a-ink-mute: #8a8a99;
  --a-ink-faint: #5a5a6a;
  --a-ice: #7dd3fc;
  --a-ice-deep: #38bdf8;
  --a-indigo: #818cf8;
  --a-violet: #c084fc;
  --a-grad: linear-gradient(135deg, #818cf8 0%, #c084fc 100%);
  --a-grad-ice: linear-gradient(135deg, #38bdf8 0%, #818cf8 100%);
  --radius-sm: 12px;
  --radius-md: 18px;
  --radius-lg: 28px;
  --radius-xl: 36px;
  --shadow-glow: 0 0 80px -20px rgba(129, 140, 248, 0.35);
  --shadow-lift: 0 30px 80px -30px rgba(0, 0, 0, 0.8);
  background: var(--a-bg);
  color: var(--a-ink);
  font-family: 'Noto Sans SC', 'Manrope', system-ui, -apple-system, sans-serif;
  font-size: 15px;
  line-height: 1.75;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  -webkit-font-smoothing: antialiased;
  letter-spacing: 0.01em;
}

.a-page * {
  box-sizing: border-box;
}

/* —— 鼠标跟随光效 —— */
.a-cursor-glow {
  position: fixed;
  top: 0;
  left: 0;
  width: 600px;
  height: 600px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(129, 140, 248, 0.12) 0%, rgba(192, 132, 252, 0.06) 35%, transparent 65%);
  pointer-events: none;
  z-index: 1;
  opacity: 0;
  transition: opacity 0.6s ease;
  mix-blend-mode: screen;
  will-change: transform;
}

/* —— 背景极光氛围层 —— */
.a-aurora-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}
.aurora-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.45;
  will-change: transform;
}
.aurora-blob-1 {
  width: 600px;
  height: 600px;
  top: -200px;
  right: -150px;
  background: radial-gradient(circle, rgba(129, 140, 248, 0.55), transparent 70%);
  animation: drift1 22s ease-in-out infinite;
}
.aurora-blob-2 {
  width: 520px;
  height: 520px;
  bottom: -180px;
  left: -120px;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.4), transparent 70%);
  animation: drift2 26s ease-in-out infinite;
}
@keyframes drift1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-60px, 80px) scale(1.1); }
}
@keyframes drift2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(80px, -60px) scale(0.95); }
}
.aurora-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.025) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.025) 1px, transparent 1px);
  background-size: 64px 64px;
  mask-image: radial-gradient(ellipse 80% 60% at 50% 40%, #000 30%, transparent 80%);
  -webkit-mask-image: radial-gradient(ellipse 80% 60% at 50% 40%, #000 30%, transparent 80%);
}

/* 让所有内容层位于背景之上 */
.a-navbar,
main,
.a-footer,
.back-top,
.a-modal {
  position: relative;
  z-index: 2;
}

/* —— 导航栏 —— */
.a-navbar {
  position: sticky;
  top: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 18px clamp(20px, 5vw, 64px);
  background: rgba(5, 5, 7, 0.6);
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  border-bottom: 1px solid transparent;
  transition: border-color 0.3s ease, background 0.3s ease;
}
.a-navbar.is-scrolled {
  border-bottom-color: var(--a-border);
  background: rgba(5, 5, 7, 0.85);
}
.a-brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: var(--a-ink);
}
.brand-mark {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  font-weight: 800;
  font-size: 15px;
  color: #fff;
  background: var(--a-grad);
  border-radius: 10px;
  box-shadow: 0 8px 22px -8px rgba(129, 140, 248, 0.7);
}
.brand-name {
  font-weight: 600;
  letter-spacing: 0.5px;
  font-size: 15px;
}
.nav-links {
  display: flex;
  gap: 32px;
}
.nav-links a {
  font-size: 14px;
  color: var(--a-ink-mute);
  text-decoration: none;
  transition: color 0.2s ease;
  position: relative;
}
.nav-links a::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -6px;
  height: 1px;
  background: var(--a-grad);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}
.nav-links a:hover {
  color: var(--a-ink);
}
.nav-links a:hover::after {
  transform: scaleX(1);
}
.nav-cta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  font-size: 14px;
  font-weight: 500;
  color: #fff;
  background: var(--a-glass-strong);
  border: 1px solid var(--a-border-strong);
  border-radius: 999px;
  text-decoration: none;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  transition: border-color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}
.nav-cta:hover {
  border-color: var(--a-indigo);
  background: rgba(129, 140, 248, 0.15);
  transform: translateY(-1px);
}

/* —— 通用按钮 —— */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 13px 24px;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  border-radius: 999px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease, color 0.2s ease, border-color 0.2s ease;
  font-family: inherit;
}
.btn-primary {
  color: #fff;
  background: var(--a-grad);
  box-shadow: 0 14px 36px -12px rgba(129, 140, 248, 0.75);
}
.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 48px -14px rgba(129, 140, 248, 0.9);
}
.btn-ghost {
  color: var(--a-ink);
  background: var(--a-glass);
  border-color: var(--a-border-strong);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}
.btn-ghost:hover {
  border-color: var(--a-ice);
  color: var(--a-ice);
}
.btn-lg {
  padding: 17px 30px;
  font-size: 15px;
}

/* —— Hero —— */
.a-hero {
  padding: clamp(70px, 10vw, 120px) clamp(20px, 5vw, 64px) clamp(80px, 10vw, 120px);
  position: relative;
}
.hero-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.15fr 1fr;
  gap: clamp(40px, 6vw, 80px);
  align-items: center;
}
.eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 7px 16px;
  font-size: 13px;
  color: var(--a-ink-soft);
  background: var(--a-glass);
  border: 1px solid var(--a-border);
  border-radius: 999px;
  margin-bottom: 28px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}
.eyebrow-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--a-ice);
  box-shadow: 0 0 12px var(--a-ice);
  animation: pulse 2s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.85); }
}
.hero-title {
  font-size: clamp(42px, 6.2vw, 76px);
  font-weight: 900;
  line-height: 1.08;
  letter-spacing: -0.02em;
  margin: 0 0 24px;
  color: var(--a-ink);
}
.hero-title em {
  font-style: normal;
  background: var(--a-grad);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  position: relative;
}
.hero-sub {
  font-size: 17px;
  color: var(--a-ink-soft);
  margin: 0 0 36px;
  max-width: 540px;
  line-height: 1.8;
}
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-bottom: 48px;
}
.hero-stats {
  display: flex;
  gap: 48px;
  padding-top: 32px;
  border-top: 1px solid var(--a-border);
}
.stat-value {
  display: block;
  font-family: 'Manrope', 'Noto Sans SC', sans-serif;
  font-size: 34px;
  font-weight: 800;
  color: var(--a-ink);
  letter-spacing: -0.02em;
  line-height: 1;
  margin-bottom: 8px;
}
.stat-label {
  font-size: 13px;
  color: var(--a-ink-mute);
  letter-spacing: 0.05em;
}

/* —— Hero 右侧抽象科技视觉 —— */
.hero-right {
  display: flex;
  justify-content: center;
  align-items: center;
}
.visual-stage {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  max-width: 460px;
  display: grid;
  place-items: center;
}
.visual-ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid var(--a-border-strong);
}
.visual-ring-1 {
  width: 100%;
  height: 100%;
  border-color: rgba(129, 140, 248, 0.25);
  animation: spin 28s linear infinite;
}
.visual-ring-1::before {
  content: '';
  position: absolute;
  top: -4px;
  left: 50%;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--a-indigo);
  box-shadow: 0 0 18px var(--a-indigo);
  transform: translateX(-50%);
}
.visual-ring-2 {
  width: 72%;
  height: 72%;
  border-color: rgba(56, 189, 248, 0.22);
  animation: spin 20s linear infinite reverse;
}
.visual-ring-2::before {
  content: '';
  position: absolute;
  top: 50%;
  right: -4px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--a-ice);
  box-shadow: 0 0 14px var(--a-ice);
  transform: translateY(-50%);
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.visual-core {
  position: relative;
  width: 44%;
  height: 44%;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, rgba(129, 140, 248, 0.4), rgba(192, 132, 252, 0.2) 60%, transparent);
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow:
    0 0 60px -10px rgba(129, 140, 248, 0.6),
    inset 0 0 30px rgba(129, 140, 248, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}
.core-glyph {
  font-family: 'Manrope', sans-serif;
  font-size: 48px;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.02em;
}
.core-pulse {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 1px solid rgba(129, 140, 248, 0.5);
  animation: corePulse 3s ease-out infinite;
}
@keyframes corePulse {
  0% { transform: scale(1); opacity: 0.8; }
  100% { transform: scale(1.6); opacity: 0; }
}
.visual-chip {
  position: absolute;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  font-size: 12px;
  color: var(--a-ink-soft);
  background: var(--a-glass-strong);
  border: 1px solid var(--a-border-strong);
  border-radius: 999px;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  white-space: nowrap;
}
.visual-chip :deep(svg) {
  color: var(--a-ice);
}
.visual-chip-1 {
  top: 8%;
  right: -4%;
  animation: float 5s ease-in-out infinite;
}
.visual-chip-2 {
  bottom: 18%;
  left: -8%;
  animation: float 6s ease-in-out infinite 0.5s;
}
.visual-chip-3 {
  bottom: -2%;
  right: 12%;
  animation: float 7s ease-in-out infinite 1s;
}
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

/* —— 通用 section —— */
.a-section {
  padding: clamp(80px, 10vw, 130px) clamp(20px, 5vw, 64px);
  position: relative;
}
.a-section-glass {
  background: linear-gradient(180deg, transparent 0%, rgba(129, 140, 248, 0.025) 50%, transparent 100%);
}
.sec-head {
  max-width: 760px;
  margin: 0 auto clamp(48px, 6vw, 80px);
  text-align: center;
}
.sec-eyebrow {
  display: inline-block;
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: var(--a-ice);
  font-weight: 600;
  margin-bottom: 16px;
  padding: 5px 14px;
  border: 1px solid rgba(56, 189, 248, 0.25);
  border-radius: 999px;
  background: rgba(56, 189, 248, 0.06);
}
.sec-title {
  font-size: clamp(30px, 4.2vw, 48px);
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.2;
  margin: 0 0 16px;
  color: var(--a-ink);
}
.sec-sub {
  font-size: 15px;
  color: var(--a-ink-mute);
  margin: 0;
  line-height: 1.8;
}

.chip {
  padding: 5px 12px;
  font-size: 12px;
  color: var(--a-ink-soft);
  background: var(--a-glass);
  border-radius: 999px;
  border: 1px solid var(--a-border);
  font-family: 'Manrope', 'Noto Sans SC', sans-serif;
  letter-spacing: 0.02em;
}

/* —— Bento Grid 项目展示 —— */
.bento-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  grid-auto-rows: minmax(180px, auto);
  gap: 20px;
}
.bento-card {
  background: var(--a-glass);
  border: 1px solid var(--a-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  transition: transform 0.4s cubic-bezier(0.22, 1, 0.36, 1), border-color 0.3s ease, box-shadow 0.3s ease;
  position: relative;
}
.bento-card::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: var(--radius-lg);
  padding: 1px;
  background: linear-gradient(135deg, rgba(129, 140, 248, 0.4), transparent 40%, transparent 60%, rgba(56, 189, 248, 0.3));
  -webkit-mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
  mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}
.bento-card:hover {
  transform: translateY(-6px);
  border-color: var(--a-border-strong);
  box-shadow: var(--shadow-lift), 0 0 50px -20px rgba(129, 140, 248, 0.4);
}
.bento-card:hover::before {
  opacity: 1;
}

/* 旗舰卡：占 4 列 2 行 */
.bento-featured {
  grid-column: span 4;
  grid-row: span 2;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
}
.featured-cover {
  position: relative;
  overflow: hidden;
  background: var(--a-surface);
}
.featured-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}
.bento-featured:hover .featured-cover img {
  transform: scale(1.04);
}
.featured-cover-veil {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent 60%, var(--a-surface-2) 100%),
    linear-gradient(0deg, rgba(5, 5, 7, 0.4) 0%, transparent 50%);
}
.featured-badge {
  position: absolute;
  top: 18px;
  left: 18px;
  padding: 6px 14px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  background: var(--a-grad);
  border-radius: 999px;
  box-shadow: 0 8px 20px -6px rgba(129, 140, 248, 0.7);
  letter-spacing: 0.05em;
}
.featured-body {
  padding: 32px 32px 30px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.featured-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.card-cat {
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--a-ice);
  font-weight: 600;
}
.card-year {
  font-size: 12px;
  color: var(--a-ink-faint);
  font-family: 'Manrope', sans-serif;
}
.featured-title {
  font-size: 26px;
  font-weight: 700;
  margin: 0 0 14px;
  color: var(--a-ink);
  letter-spacing: -0.01em;
  line-height: 1.3;
}
.featured-desc {
  font-size: 14px;
  color: var(--a-ink-mute);
  line-height: 1.75;
  margin: 0 0 20px;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.featured-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-bottom: 22px;
}
.featured-cta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--a-ink);
  transition: gap 0.2s ease, color 0.2s ease;
}
.bento-featured:hover .featured-cta {
  gap: 10px;
  color: var(--a-ice);
}

/* 小卡：每张占 2 列 1 行 */
.bento-small {
  grid-column: span 2;
  grid-row: span 1;
  display: flex;
  flex-direction: column;
}
.small-cover {
  position: relative;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background: var(--a-surface);
}
.small-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}
.bento-small:hover .small-cover img {
  transform: scale(1.06);
}
.small-cover-veil {
  position: absolute;
  inset: 0;
  background: linear-gradient(0deg, rgba(5, 5, 7, 0.6) 0%, transparent 50%);
}
.small-year {
  position: absolute;
  bottom: 12px;
  right: 12px;
  padding: 4px 11px;
  font-size: 11px;
  color: var(--a-ink);
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid var(--a-border-strong);
  border-radius: 999px;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  font-family: 'Manrope', sans-serif;
}
.small-body {
  padding: 20px 22px 22px;
  flex: 1;
  display: flex;
  flex-direction: column;
}
.small-title {
  font-size: 18px;
  font-weight: 700;
  margin: 8px 0 8px;
  color: var(--a-ink);
  letter-spacing: -0.01em;
}
.small-desc {
  font-size: 13px;
  color: var(--a-ink-mute);
  line-height: 1.7;
  margin: 0 0 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}
.small-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

/* —— 技术栈 —— */
.skills-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}
.skill-card {
  background: var(--a-glass);
  border: 1px solid var(--a-border);
  border-radius: var(--radius-md);
  padding: 28px 30px;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  transition: border-color 0.3s ease, transform 0.3s ease, box-shadow 0.3s ease;
}
.skill-card:hover {
  border-color: var(--a-border-strong);
  transform: translateY(-3px);
  box-shadow: 0 20px 50px -24px rgba(129, 140, 248, 0.3);
}
.skill-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
  padding-bottom: 18px;
  margin-bottom: 18px;
  border-bottom: 1px solid var(--a-border);
}
.skill-cn {
  font-size: 18px;
  font-weight: 700;
  color: var(--a-ink);
  letter-spacing: 0.02em;
}
.skill-en {
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--a-ink-faint);
  font-family: 'Manrope', sans-serif;
}
.skill-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.skill-list li {
  padding: 7px 14px;
  font-size: 13px;
  color: var(--a-ink-soft);
  background: var(--a-glass-strong);
  border: 1px solid var(--a-border);
  border-radius: 999px;
  font-family: 'Manrope', 'Noto Sans SC', sans-serif;
  letter-spacing: 0.02em;
  transition: color 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}
.skill-list li:hover {
  color: var(--a-ice);
  border-color: rgba(56, 189, 248, 0.4);
  background: rgba(56, 189, 248, 0.08);
}

/* —— 关于我 —— */
.about-grid {
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: clamp(40px, 6vw, 72px);
  align-items: center;
}
.about-lead {
  font-size: 17px;
  color: var(--a-ink-soft);
  line-height: 2;
  margin: 0 0 24px;
  letter-spacing: 0.01em;
}
.about-extra {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: var(--a-ice);
  margin: 0;
  flex-wrap: wrap;
}
.about-extra :deep(svg) {
  opacity: 0.85;
}
.dot-sep {
  color: var(--a-ink-faint);
  margin: 0 4px;
}
.about-cards {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
}
.about-card {
  background: var(--a-glass);
  border: 1px solid var(--a-border);
  border-radius: var(--radius-md);
  padding: 24px 26px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  transition: border-color 0.3s ease, transform 0.3s ease;
}
.about-card:hover {
  border-color: var(--a-border-strong);
  transform: translateX(4px);
}
.about-value {
  font-family: 'Manrope', sans-serif;
  font-size: 32px;
  font-weight: 800;
  background: var(--a-grad);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.02em;
  line-height: 1;
}
.about-label {
  font-size: 13px;
  color: var(--a-ink-mute);
  letter-spacing: 0.05em;
}

/* —— 时间线（左右交替） —— */
.timeline {
  max-width: 920px;
  margin: 0 auto;
  list-style: none;
  padding: 0;
  position: relative;
}
.timeline::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 1px;
  background: linear-gradient(180deg, transparent 0%, var(--a-border-strong) 8%, var(--a-border-strong) 92%, transparent 100%);
  transform: translateX(-50%);
}
.tl-item {
  position: relative;
  width: 50%;
  padding: 0 0 48px;
}
.tl-item:not(.tl-item-right) {
  left: 0;
  padding-right: 48px;
  text-align: right;
}
.tl-item-right {
  left: 50%;
  padding-left: 48px;
}
.tl-item:last-child {
  padding-bottom: 0;
}
.tl-dot {
  position: absolute;
  top: 6px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--a-bg);
  border: 2px solid var(--a-indigo);
  box-shadow: 0 0 0 4px rgba(129, 140, 248, 0.12), 0 0 18px rgba(129, 140, 248, 0.6);
}
.tl-item:not(.tl-item-right) .tl-dot {
  right: -7px;
}
.tl-item-right .tl-dot {
  left: -7px;
}
.tl-content {
  background: var(--a-glass);
  border: 1px solid var(--a-border);
  border-radius: var(--radius-md);
  padding: 22px 26px;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  transition: border-color 0.3s ease, transform 0.3s ease;
  display: inline-block;
  max-width: 100%;
}
.tl-item:not(.tl-item-right) .tl-content {
  text-align: right;
}
.tl-content:hover {
  border-color: var(--a-border-strong);
  transform: translateY(-2px);
}
.tl-period {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  color: var(--a-ice);
  background: rgba(56, 189, 248, 0.1);
  border: 1px solid rgba(56, 189, 248, 0.2);
  padding: 3px 11px;
  border-radius: 999px;
  margin-bottom: 12px;
  font-family: 'Manrope', sans-serif;
  letter-spacing: 0.05em;
}
.tl-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 6px;
  color: var(--a-ink);
  letter-spacing: -0.01em;
}
.tl-org {
  font-size: 13px;
  color: var(--a-violet);
  font-weight: 600;
}
.tl-desc {
  font-size: 14px;
  color: var(--a-ink-mute);
  line-height: 1.75;
  margin: 12px 0 0;
}

/* —— 联系方式 —— */
.contact-card {
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: clamp(36px, 5vw, 64px);
  align-items: center;
  background: var(--a-glass);
  border: 1px solid var(--a-border-strong);
  border-radius: var(--radius-xl);
  padding: clamp(40px, 5vw, 64px);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  position: relative;
  overflow: hidden;
}
.contact-glow {
  position: absolute;
  top: -50%;
  right: -20%;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(129, 140, 248, 0.18), transparent 60%);
  pointer-events: none;
}
.contact-card .sec-eyebrow {
  margin-bottom: 14px;
}
.contact-title {
  font-size: clamp(28px, 3.6vw, 40px);
  font-weight: 800;
  color: var(--a-ink);
  margin: 0 0 18px;
  letter-spacing: -0.02em;
  line-height: 1.25;
}
.contact-sub {
  font-size: 15px;
  color: var(--a-ink-mute);
  line-height: 1.85;
  margin: 0 0 28px;
  max-width: 420px;
}
.contact-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.contact-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  color: var(--a-ink-soft);
}
.contact-list li :deep(svg) {
  color: var(--a-ice);
  flex-shrink: 0;
}
.contact-list a {
  color: var(--a-ink-soft);
  text-decoration: none;
  transition: color 0.2s ease;
  font-family: 'Manrope', 'Noto Sans SC', sans-serif;
}
.contact-list a:hover {
  color: var(--a-ice);
}
.contact-right {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 16px;
  position: relative;
  z-index: 1;
}
.social-link {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 18px;
  font-size: 14px;
  color: var(--a-ink-soft);
  background: var(--a-glass-strong);
  border: 1px solid var(--a-border);
  border-radius: 999px;
  text-decoration: none;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  transition: color 0.2s ease, border-color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}
.social-link:hover {
  color: var(--a-ink);
  border-color: var(--a-indigo);
  background: rgba(129, 140, 248, 0.12);
  transform: translateX(4px);
}
.social-link :deep(svg) {
  color: var(--a-ice);
}

/* —— 页脚 —— */
.a-footer {
  padding: 36px clamp(20px, 5vw, 64px);
  border-top: 1px solid var(--a-border);
  background: var(--a-bg-soft);
}
.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.footer-brand {
  font-weight: 600;
  color: var(--a-ink);
  letter-spacing: 0.02em;
}
.footer-copy {
  font-size: 13px;
  color: var(--a-ink-faint);
}

/* —— 回到顶部 —— */
.back-top {
  position: fixed;
  right: 28px;
  bottom: 28px;
  z-index: 40;
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  color: #fff;
  background: var(--a-glass-strong);
  border: 1px solid var(--a-border-strong);
  border-radius: 50%;
  cursor: pointer;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  transition: transform 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}
.back-top:hover {
  transform: translateY(-3px);
  border-color: var(--a-ice);
  background: rgba(56, 189, 248, 0.15);
}

/* —— 作品弹窗 —— */
.a-modal {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: none;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.a-modal.is-open {
  display: flex;
}
.modal-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}
.modal-card {
  position: relative;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  max-width: 940px;
  width: 100%;
  max-height: 88vh;
  background: var(--a-surface);
  border: 1px solid var(--a-border-strong);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: 0 40px 100px -30px rgba(0, 0, 0, 0.9), 0 0 60px -20px rgba(129, 140, 248, 0.3);
}
.modal-close {
  position: absolute;
  top: 18px;
  right: 18px;
  z-index: 2;
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  color: var(--a-ink-soft);
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--a-border-strong);
  border-radius: 50%;
  cursor: pointer;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  transition: transform 0.2s ease, background 0.2s ease, color 0.2s ease;
}
.modal-close:hover {
  background: rgba(129, 140, 248, 0.2);
  color: #fff;
  transform: rotate(90deg);
}
.modal-cover {
  position: relative;
  overflow: hidden;
  min-height: 320px;
  background: var(--a-bg-soft);
}
.modal-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.modal-year {
  position: absolute;
  bottom: 16px;
  left: 16px;
  padding: 5px 13px;
  font-size: 12px;
  color: var(--a-ink);
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid var(--a-border-strong);
  border-radius: 999px;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  font-family: 'Manrope', sans-serif;
}
.modal-body {
  padding: 40px 40px 36px;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.modal-cat {
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--a-ice);
  font-weight: 600;
  margin-bottom: 12px;
}
.modal-title {
  font-size: 28px;
  font-weight: 800;
  color: var(--a-ink);
  margin: 0 0 16px;
  line-height: 1.3;
  letter-spacing: -0.01em;
}
.modal-desc {
  font-size: 14px;
  color: var(--a-ink-mute);
  line-height: 1.9;
  margin: 0 0 24px;
}
.modal-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 28px;
}
.modal-link {
  align-self: flex-start;
  margin-top: auto;
}

/* —— 渐入动画 —— */
.reveal {
  opacity: 0;
  transform: translateY(28px);
  transition: opacity 0.8s cubic-bezier(0.22, 1, 0.36, 1),
    transform 0.8s cubic-bezier(0.22, 1, 0.36, 1);
  transition-delay: calc(var(--i, 0) * 80ms);
}
.reveal.is-visible {
  opacity: 1;
  transform: none;
}

/* —— 响应式 —— */
@media (max-width: 960px) {
  .hero-inner,
  .about-grid,
  .contact-card {
    grid-template-columns: 1fr;
  }
  .hero-right {
    order: -1;
  }
  .visual-stage {
    max-width: 340px;
  }
  .bento-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .bento-featured {
    grid-column: span 2;
    grid-row: span 1;
    grid-template-columns: 1fr;
  }
  .featured-cover {
    aspect-ratio: 16 / 9;
  }
  .bento-small {
    grid-column: span 1;
  }
  .skills-grid {
    grid-template-columns: 1fr;
  }
  .nav-links {
    display: none;
  }
  .hero-stats {
    gap: 28px;
  }
  .timeline::before {
    left: 7px;
  }
  .tl-item,
  .tl-item-right {
    width: 100%;
    left: 0;
    padding-left: 36px;
    padding-right: 0;
    text-align: left;
  }
  .tl-item:not(.tl-item-right) .tl-content,
  .tl-item-right .tl-content {
    text-align: left;
  }
  .tl-item:not(.tl-item-right) .tl-dot,
  .tl-item-right .tl-dot {
    left: 0;
    right: auto;
  }
}
@media (max-width: 560px) {
  .modal-card {
    grid-template-columns: 1fr;
    max-height: 90vh;
    overflow-y: auto;
  }
  .modal-cover {
    min-height: 200px;
    max-height: 220px;
  }
  .modal-body {
    padding: 26px;
  }
  .bento-grid {
    grid-template-columns: 1fr;
  }
  .bento-featured,
  .bento-small {
    grid-column: span 1;
  }
  .hero-stats {
    gap: 20px;
  }
  .stat-value {
    font-size: 28px;
  }
}
</style>

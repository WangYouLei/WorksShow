<script setup lang="ts">
import { computed } from 'vue'
import {
  X,
  ArrowUpRight,
  ArrowUp,
  Mail,
  MapPin,
  Clock,
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
  <div class="d-page">
    <!-- 导航栏 -->
    <nav class="d-navbar">
      <a class="d-brand" href="#top">
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
      <!-- Hero：居中编辑式排版 -->
      <section class="d-hero">
        <div class="hero-inner reveal">
          <span class="eyebrow">{{ data.profile.title }}</span>
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
        <div class="hero-rule reveal"></div>
      </section>

      <!-- 项目展示：纵向大图列表，左右交替 -->
      <section id="works" class="d-section">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">项目作品</span>
          <h2 class="sec-title">把每一个想法<br />打磨成可被使用的产品</h2>
          <p class="sec-sub">从 AI 应用、Web 产品到自动化工具，以下是独立设计与交付的代表作品</p>
        </header>

        <!-- 旗舰作品：大图横幅 -->
        <article
          v-if="featured"
          class="work-feature reveal"
          :data-work-id="featured.id"
          role="button"
          tabindex="0"
          :aria-label="featured.title"
        >
          <div class="feature-cover">
            <img :src="featured.cover" :alt="featured.title" loading="lazy" />
          </div>
          <div class="feature-body">
            <div class="feature-meta">
              <span class="card-cat">{{ featured.category }}</span>
              <span class="dot-sep">·</span>
              <span class="card-year">{{ featured.year }}</span>
              <span class="featured-badge">旗舰作品</span>
            </div>
            <h3 class="feature-title">{{ featured.title }}</h3>
            <p class="feature-desc">{{ featured.description }}</p>
            <div class="feature-tags">
              <span v-for="t in featured.tags.slice(0, 4)" :key="t" class="chip">{{ t }}</span>
            </div>
            <span class="feature-cta">
              查看详情 <ArrowUpRight :size="15" :stroke-width="2" />
            </span>
          </div>
        </article>

        <!-- 其余作品：左右交替大图列表 -->
        <div class="work-list">
          <article
            v-for="(w, i) in rest"
            :key="w.id"
            class="work-item reveal"
            :class="{ 'work-item-reverse': i % 2 === 1 }"
            :data-work-id="w.id"
            role="button"
            tabindex="0"
            :aria-label="w.title"
          >
            <div class="work-cover">
              <img :src="w.cover" :alt="w.title" loading="lazy" />
            </div>
            <div class="work-body">
              <div class="work-meta">
                <span class="card-cat">{{ w.category }}</span>
                <span class="dot-sep">·</span>
                <span class="card-year">{{ w.year }}</span>
              </div>
              <h3 class="work-title">{{ w.title }}</h3>
              <p class="work-desc">{{ w.description }}</p>
              <div class="work-tags">
                <span v-for="t in w.tags.slice(0, 3)" :key="t" class="chip">{{ t }}</span>
              </div>
              <span class="work-cta">
                查看详情 <ArrowUpRight :size="14" :stroke-width="2" />
              </span>
            </div>
          </article>
        </div>
      </section>

      <!-- 技术栈：简约标签云分组 -->
      <section id="skills" class="d-section d-section-tinted">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">技术栈</span>
          <h2 class="sec-title">从想法到上线<br />全链路独立交付能力</h2>
          <p class="sec-sub">覆盖前端、后端、AI、数据库与云服务，独立完成产品研发全流程</p>
        </header>

        <div class="skills-wrap">
          <div v-for="g in data.skills" :key="g.id" class="skill-group reveal">
            <div class="skill-head">
              <span class="skill-num">{{ String(data.skills.indexOf(g) + 1).padStart(2, '0') }}</span>
              <div class="skill-titles">
                <span class="skill-cn">{{ g.category }}</span>
                <span class="skill-en">{{ g.categoryEn }}</span>
              </div>
            </div>
            <ul class="skill-list">
              <li v-for="item in g.items" :key="item">{{ item }}</li>
            </ul>
          </div>
        </div>
      </section>

      <!-- 关于我：大段引文式 -->
      <section id="about" class="d-section">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">关于我</span>
          <h2 class="sec-title">一个持续创造产品的人</h2>
        </header>
        <div class="about-wrap">
          <blockquote class="about-quote reveal">
            <span class="quote-mark">"</span>
            <p class="quote-text">{{ data.profile.bio }}</p>
          </blockquote>
          <div class="about-meta reveal">
            <p class="about-extra">
              <MapPin :size="16" :stroke-width="1.75" />
              <span>{{ data.profile.location }}</span>
            </p>
            <p class="about-extra">
              <Clock :size="16" :stroke-width="1.75" />
              <span>{{ data.profile.availability }}</span>
            </p>
            <div class="about-stats">
              <div v-for="s in stats" :key="s.label" class="about-stat">
                <span class="about-value">{{ s.value }}</span>
                <span class="about-label">{{ s.label }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 项目历程：简约竖向左对齐时间轴 -->
      <section id="timeline" class="d-section d-section-tinted">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">项目历程</span>
          <h2 class="sec-title">每个里程碑<br />都是一次产品突破</h2>
        </header>

        <ol class="timeline">
          <li
            v-for="(e, i) in data.experiences"
            :key="e.id"
            class="tl-item reveal"
            :style="{ '--i': i }"
          >
            <span class="tl-dot"></span>
            <span class="tl-period">{{ e.period }}</span>
            <h3 class="tl-title">{{ e.title }}</h3>
            <span class="tl-org">{{ e.organization }}</span>
            <p class="tl-desc">{{ e.description }}</p>
          </li>
        </ol>
      </section>

      <!-- 联系方式：极简居中 CTA -->
      <section id="contact" class="d-section">
        <div class="contact-wrap reveal">
          <span class="sec-eyebrow">联系方式</span>
          <h2 class="contact-title">有想法？<br />一起把它变成产品</h2>
          <p class="contact-sub">
            无论是合作机会、产品咨询，还是只是想打个招呼，我都很乐意聊聊。通常 24 小时内回复。
          </p>
          <a class="btn btn-primary btn-lg" :href="'mailto:' + data.profile.email">
            发邮件给我 <ArrowUpRight :size="18" :stroke-width="2" />
          </a>
          <div class="contact-list">
            <a class="contact-item" :href="'mailto:' + data.profile.email">
              <Mail :size="16" :stroke-width="1.75" />
              <span>{{ data.profile.email }}</span>
            </a>
            <a
              v-for="s in data.profile.socials"
              :key="s.name"
              class="contact-item"
              :href="s.url"
              target="_blank"
              rel="noopener noreferrer"
            >
              <component :is="s.icon === 'github' ? Github : Mail" :size="16" :stroke-width="1.75" />
              <span>{{ s.name }}</span>
              <ArrowUpRight :size="13" :stroke-width="2" />
            </a>
          </div>
        </div>
      </section>
    </main>

    <footer class="d-footer">
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
      class="d-modal"
      :data-modal-id="w.id"
    >
      <div class="modal-overlay"></div>
      <article class="modal-card">
        <button class="modal-close" aria-label="关闭" type="button">
          <X :size="20" :stroke-width="1.75" />
        </button>
        <div class="modal-cover">
          <img :src="w.cover" :alt="w.title" loading="lazy" />
        </div>
        <div class="modal-body">
          <span class="modal-cat">{{ w.category }} · {{ w.year }}</span>
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
.d-page {
  --d-bg: #faf8f4;
  --d-bg-tint: #f3efe7;
  --d-surface: #ffffff;
  --d-border: #e7e5e0;
  --d-border-strong: #d6d3cc;
  --d-ink: #1c1c1a;
  --d-ink-soft: #57534e;
  --d-ink-mute: #a8a29e;
  --d-ink-faint: #c7c4bd;
  --d-accent: #c2410c;
  --d-accent-deep: #9a3412;
  --d-accent-soft: #fed7aa;
  --d-green: #166534;
  --d-grad: linear-gradient(135deg, #fb923c 0%, #ea580c 100%);
  --radius-sm: 10px;
  --radius-md: 16px;
  --radius-lg: 24px;
  --shadow-soft: 0 1px 3px rgba(28, 28, 26, 0.04), 0 8px 24px -8px rgba(28, 28, 26, 0.08);
  --shadow-lift: 0 4px 12px rgba(28, 28, 26, 0.06), 0 24px 60px -20px rgba(28, 28, 26, 0.15);
  background: var(--d-bg);
  color: var(--d-ink);
  font-family: 'Noto Sans SC', system-ui, -apple-system, sans-serif;
  font-size: 15px;
  line-height: 1.8;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  -webkit-font-smoothing: antialiased;
  letter-spacing: 0.01em;
}

.d-page * {
  box-sizing: border-box;
}

/* —— 导航栏 —— */
.d-navbar {
  position: sticky;
  top: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 16px clamp(20px, 5vw, 64px);
  background: rgba(250, 248, 244, 0.7);
  backdrop-filter: blur(16px) saturate(160%);
  -webkit-backdrop-filter: blur(16px) saturate(160%);
  border-bottom: 1px solid transparent;
  transition: border-color 0.3s ease, background 0.3s ease;
}
.d-navbar.is-scrolled {
  border-bottom-color: var(--d-border);
  background: rgba(250, 248, 244, 0.92);
}
.d-brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: var(--d-ink);
}
.brand-mark {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  font-family: 'Noto Serif SC', serif;
  font-weight: 700;
  font-size: 15px;
  color: #fff;
  background: var(--d-ink);
  border-radius: 8px;
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
  color: var(--d-ink-soft);
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
  background: var(--d-accent);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}
.nav-links a:hover {
  color: var(--d-ink);
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
  color: var(--d-ink);
  background: var(--d-surface);
  border: 1px solid var(--d-border-strong);
  border-radius: 999px;
  text-decoration: none;
  transition: border-color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}
.nav-cta:hover {
  border-color: var(--d-accent);
  box-shadow: 0 6px 18px -8px rgba(194, 65, 12, 0.3);
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
  background: var(--d-accent);
  box-shadow: 0 10px 28px -10px rgba(194, 65, 12, 0.6);
}
.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 40px -12px rgba(194, 65, 12, 0.75);
}
.btn-ghost {
  color: var(--d-ink);
  background: transparent;
  border-color: var(--d-border-strong);
}
.btn-ghost:hover {
  border-color: var(--d-ink);
  background: var(--d-surface);
}
.btn-lg {
  padding: 17px 30px;
  font-size: 15px;
}

/* —— Hero：居中编辑式排版 —— */
.d-hero {
  padding: clamp(80px, 12vw, 140px) clamp(20px, 5vw, 64px) clamp(60px, 8vw, 100px);
  text-align: center;
  position: relative;
}
.hero-inner {
  max-width: 760px;
  margin: 0 auto;
}
.eyebrow {
  display: inline-block;
  font-size: 13px;
  color: var(--d-accent);
  font-weight: 500;
  letter-spacing: 0.15em;
  margin-bottom: 24px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--d-border-strong);
}
.hero-title {
  font-family: 'Noto Serif SC', serif;
  font-size: clamp(40px, 6vw, 72px);
  font-weight: 700;
  line-height: 1.15;
  letter-spacing: -0.02em;
  margin: 0 0 24px;
  color: var(--d-ink);
}
.hero-title em {
  font-style: normal;
  color: var(--d-accent);
  position: relative;
}
.hero-title em::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 4px;
  height: 8px;
  background: rgba(194, 65, 12, 0.12);
  z-index: -1;
}
.hero-sub {
  font-size: 17px;
  color: var(--d-ink-soft);
  margin: 0 auto 36px;
  max-width: 520px;
  line-height: 1.85;
}
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  justify-content: center;
  margin-bottom: 56px;
}
.hero-stats {
  display: flex;
  gap: 56px;
  justify-content: center;
  padding-top: 36px;
  border-top: 1px solid var(--d-border);
}
.stat-value {
  display: block;
  font-family: 'Noto Serif SC', serif;
  font-size: 36px;
  font-weight: 700;
  color: var(--d-ink);
  letter-spacing: -0.02em;
  line-height: 1;
  margin-bottom: 8px;
}
.stat-label {
  font-size: 13px;
  color: var(--d-ink-mute);
  letter-spacing: 0.05em;
}
.hero-rule {
  width: 1px;
  height: 60px;
  margin: 0 auto;
  background: linear-gradient(180deg, var(--d-border-strong), transparent);
}

/* —— 通用 section —— */
.d-section {
  padding: clamp(80px, 10vw, 130px) clamp(20px, 5vw, 64px);
  position: relative;
}
.d-section-tinted {
  background: var(--d-bg-tint);
}
.sec-head {
  max-width: 680px;
  margin: 0 auto clamp(56px, 7vw, 88px);
  text-align: center;
}
.sec-eyebrow {
  display: inline-block;
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: var(--d-accent);
  font-weight: 600;
  margin-bottom: 16px;
}
.sec-title {
  font-family: 'Noto Serif SC', serif;
  font-size: clamp(28px, 4vw, 44px);
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.25;
  margin: 0 0 16px;
  color: var(--d-ink);
}
.sec-sub {
  font-size: 15px;
  color: var(--d-ink-soft);
  margin: 0;
  line-height: 1.85;
}

.chip {
  padding: 5px 13px;
  font-size: 12px;
  color: var(--d-ink-soft);
  background: var(--d-bg-tint);
  border-radius: 999px;
  border: 1px solid var(--d-border);
  letter-spacing: 0.02em;
}

.card-cat {
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--d-accent);
  font-weight: 600;
}
.card-year {
  font-size: 12px;
  color: var(--d-ink-mute);
}
.dot-sep {
  color: var(--d-ink-faint);
  margin: 0 4px;
}

/* —— 旗舰作品：大图横幅 —— */
.work-feature {
  max-width: 1080px;
  margin: 0 auto 80px;
  background: var(--d-surface);
  border: 1px solid var(--d-border);
  border-radius: var(--d-lg);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.4s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.3s ease, border-color 0.3s ease;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
}
.work-feature:hover {
  transform: translateY(-4px);
  box-shadow: var(--d-shadow-lift);
  border-color: var(--d-border-strong);
}
.feature-cover {
  position: relative;
  overflow: hidden;
  min-height: 360px;
  background: var(--d-bg-tint);
}
.feature-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}
.work-feature:hover .feature-cover img {
  transform: scale(1.04);
}
.feature-body {
  padding: 40px 40px 36px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.feature-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
}
.featured-badge {
  margin-left: 8px;
  padding: 3px 11px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  background: var(--d-accent);
  border-radius: 999px;
  letter-spacing: 0.05em;
}
.feature-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 14px;
  color: var(--d-ink);
  letter-spacing: -0.01em;
  line-height: 1.3;
}
.feature-desc {
  font-size: 14px;
  color: var(--d-ink-soft);
  line-height: 1.8;
  margin: 0 0 20px;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.feature-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-bottom: 24px;
}
.feature-cta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--d-ink);
  transition: gap 0.2s ease, color 0.2s ease;
}
.work-feature:hover .feature-cta {
  gap: 10px;
  color: var(--d-accent);
}

/* —— 作品列表：左右交替 —— */
.work-list {
  max-width: 1080px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 64px;
}
.work-item {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48px;
  align-items: center;
  cursor: pointer;
  transition: transform 0.3s ease;
}
.work-item-reverse {
  direction: rtl;
}
.work-item-reverse > * {
  direction: ltr;
}
.work-cover {
  position: relative;
  overflow: hidden;
  border-radius: var(--d-md);
  aspect-ratio: 4 / 3;
  background: var(--d-bg-tint);
  box-shadow: var(--d-shadow-soft);
  transition: box-shadow 0.3s ease;
}
.work-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}
.work-item:hover .work-cover img {
  transform: scale(1.05);
}
.work-item:hover .work-cover {
  box-shadow: var(--d-shadow-lift);
}
.work-body {
  padding: 0 8px;
}
.work-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 14px;
}
.work-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 12px;
  color: var(--d-ink);
  letter-spacing: -0.01em;
  line-height: 1.35;
}
.work-desc {
  font-size: 14px;
  color: var(--d-ink-soft);
  line-height: 1.8;
  margin: 0 0 18px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.work-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-bottom: 20px;
}
.work-cta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--d-ink);
  border-bottom: 1px solid var(--d-border-strong);
  padding-bottom: 4px;
  transition: gap 0.2s ease, color 0.2s ease, border-color 0.2s ease;
}
.work-item:hover .work-cta {
  gap: 10px;
  color: var(--d-accent);
  border-color: var(--d-accent);
}

/* —— 技术栈：标签云分组 —— */
.skills-wrap {
  max-width: 1080px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}
.skill-group {
  background: var(--d-surface);
  border: 1px solid var(--d-border);
  border-radius: var(--d-md);
  padding: 28px 30px;
  transition: box-shadow 0.3s ease, border-color 0.3s ease;
}
.skill-group:hover {
  border-color: var(--d-border-strong);
  box-shadow: var(--d-shadow-soft);
}
.skill-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 18px;
  margin-bottom: 18px;
  border-bottom: 1px solid var(--d-border);
}
.skill-num {
  font-family: 'Noto Serif SC', serif;
  font-size: 24px;
  font-weight: 700;
  color: var(--d-accent);
  line-height: 1;
}
.skill-titles {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.skill-cn {
  font-size: 17px;
  font-weight: 600;
  color: var(--d-ink);
}
.skill-en {
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--d-ink-mute);
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
  color: var(--d-ink-soft);
  background: var(--d-bg-tint);
  border-radius: 999px;
  transition: color 0.2s ease, background 0.2s ease;
}
.skill-list li:hover {
  color: var(--d-accent);
  background: var(--d-accent-soft);
}

/* —— 关于我：大段引文式 —— */
.about-wrap {
  max-width: 920px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  gap: clamp(40px, 6vw, 72px);
  align-items: start;
}
.about-quote {
  margin: 0;
  position: relative;
  padding-left: 32px;
  border-left: 2px solid var(--d-accent);
}
.quote-mark {
  position: absolute;
  top: -16px;
  left: 18px;
  font-family: 'Noto Serif SC', serif;
  font-size: 64px;
  font-weight: 700;
  color: var(--d-accent);
  opacity: 0.2;
  line-height: 1;
}
.quote-text {
  font-family: 'Noto Serif SC', serif;
  font-size: 22px;
  line-height: 1.9;
  color: var(--d-ink);
  margin: 0;
  font-weight: 500;
  letter-spacing: 0.01em;
}
.about-meta {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-top: 8px;
}
.about-extra {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: var(--d-ink-soft);
  margin: 0;
}
.about-extra :deep(svg) {
  color: var(--d-accent);
}
.about-stats {
  margin-top: 16px;
  padding-top: 20px;
  border-top: 1px solid var(--d-border);
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.about-stat {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.about-value {
  font-family: 'Noto Serif SC', serif;
  font-size: 26px;
  font-weight: 700;
  color: var(--d-accent);
  line-height: 1;
}
.about-label {
  font-size: 13px;
  color: var(--d-ink-mute);
}

/* —— 时间轴：简约竖向 —— */
.timeline {
  max-width: 720px;
  margin: 0 auto;
  list-style: none;
  padding: 0;
  position: relative;
}
.timeline::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 8px;
  bottom: 8px;
  width: 1px;
  background: var(--d-border-strong);
}
.tl-item {
  position: relative;
  padding: 0 0 48px 40px;
}
.tl-item:last-child {
  padding-bottom: 0;
}
.tl-dot {
  position: absolute;
  left: 3px;
  top: 6px;
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: var(--d-surface);
  border: 2px solid var(--d-accent);
  transition: background 0.3s ease;
}
.tl-item:hover .tl-dot {
  background: var(--d-accent);
}
.tl-period {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  color: var(--d-accent);
  background: var(--d-accent-soft);
  padding: 3px 11px;
  border-radius: 999px;
  margin-bottom: 12px;
  letter-spacing: 0.05em;
}
.tl-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 19px;
  font-weight: 700;
  margin: 0 0 4px;
  color: var(--d-ink);
  letter-spacing: -0.01em;
}
.tl-org {
  font-size: 13px;
  color: var(--d-green);
  font-weight: 600;
}
.tl-desc {
  font-size: 14px;
  color: var(--d-ink-soft);
  line-height: 1.8;
  margin: 10px 0 0;
}

/* —— 联系方式：极简居中 —— */
.contact-wrap {
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
}
.contact-title {
  font-family: 'Noto Serif SC', serif;
  font-size: clamp(28px, 3.6vw, 40px);
  font-weight: 700;
  color: var(--d-ink);
  margin: 0 0 18px;
  letter-spacing: -0.02em;
  line-height: 1.3;
}
.contact-sub {
  font-size: 15px;
  color: var(--d-ink-soft);
  line-height: 1.85;
  margin: 0 0 32px;
}
.contact-list {
  margin-top: 36px;
  padding-top: 36px;
  border-top: 1px solid var(--d-border);
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
}
.contact-item {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 18px;
  font-size: 14px;
  color: var(--d-ink-soft);
  background: var(--d-surface);
  border: 1px solid var(--d-border);
  border-radius: 999px;
  text-decoration: none;
  transition: color 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}
.contact-item:hover {
  color: var(--d-accent);
  border-color: var(--d-accent);
  box-shadow: 0 6px 18px -8px rgba(194, 65, 12, 0.25);
}
.contact-item :deep(svg) {
  color: var(--d-accent);
}

/* —— 页脚 —— */
.d-footer {
  padding: 36px clamp(20px, 5vw, 64px);
  border-top: 1px solid var(--d-border);
  background: var(--d-bg);
}
.footer-inner {
  max-width: 1080px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.footer-brand {
  font-weight: 600;
  color: var(--d-ink);
}
.footer-copy {
  font-size: 13px;
  color: var(--d-ink-mute);
}

/* —— 回到顶部 —— */
.back-top {
  position: fixed;
  right: 28px;
  bottom: 28px;
  z-index: 40;
  display: grid;
  place-items: center;
  width: 46px;
  height: 46px;
  color: var(--d-ink);
  background: var(--d-surface);
  border: 1px solid var(--d-border-strong);
  border-radius: 50%;
  cursor: pointer;
  box-shadow: var(--d-shadow-soft);
  transition: transform 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}
.back-top:hover {
  transform: translateY(-3px);
  border-color: var(--d-accent);
  color: var(--d-accent);
}

/* —— 作品弹窗 —— */
.d-modal {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: none;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.d-modal.is-open {
  display: flex;
}
.modal-overlay {
  position: absolute;
  inset: 0;
  background: rgba(28, 28, 26, 0.5);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}
.modal-card {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1fr;
  max-width: 900px;
  width: 100%;
  max-height: 88vh;
  background: var(--d-surface);
  border: 1px solid var(--d-border);
  border-radius: var(--d-lg);
  overflow: hidden;
  box-shadow: 0 40px 100px -30px rgba(28, 28, 26, 0.3);
}
.modal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 2;
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  color: var(--d-ink-soft);
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid var(--d-border);
  border-radius: 50%;
  cursor: pointer;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  transition: transform 0.2s ease, background 0.2s ease, color 0.2s ease;
}
.modal-close:hover {
  background: var(--d-accent);
  color: #fff;
  transform: rotate(90deg);
}
.modal-cover {
  position: relative;
  overflow: hidden;
  min-height: 300px;
  background: var(--d-bg-tint);
}
.modal-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.modal-body {
  padding: 40px 40px 36px;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.modal-cat {
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--d-accent);
  font-weight: 600;
  margin-bottom: 12px;
}
.modal-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 26px;
  font-weight: 700;
  color: var(--d-ink);
  margin: 0 0 16px;
  line-height: 1.3;
  letter-spacing: -0.01em;
}
.modal-desc {
  font-size: 14px;
  color: var(--d-ink-soft);
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
@media (max-width: 900px) {
  .work-feature {
    grid-template-columns: 1fr;
  }
  .feature-cover {
    min-height: 240px;
    aspect-ratio: 16 / 9;
  }
  .feature-body {
    padding: 28px;
  }
  .work-item,
  .work-item-reverse {
    grid-template-columns: 1fr;
    gap: 24px;
    direction: ltr;
  }
  .skills-wrap {
    grid-template-columns: 1fr;
  }
  .about-wrap {
    grid-template-columns: 1fr;
  }
  .nav-links {
    display: none;
  }
  .hero-stats {
    gap: 32px;
  }
}
@media (max-width: 560px) {
  .modal-card {
    grid-template-columns: 1fr;
    max-height: 90vh;
    overflow-y: auto;
  }
  .modal-cover {
    min-height: 180px;
    max-height: 220px;
  }
  .modal-body {
    padding: 26px;
  }
  .hero-stats {
    gap: 20px;
  }
  .stat-value {
    font-size: 30px;
  }
  .quote-text {
    font-size: 19px;
  }
}
</style>

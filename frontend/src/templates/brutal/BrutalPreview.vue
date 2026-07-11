<script setup lang="ts">
import { computed } from 'vue'
import {
  X,
  ArrowUpRight,
  ArrowUp,
  Mail,
  MapPin,
  Clock,
  Square,
} from 'lucide-vue-next'
import type { PortfolioData } from '../types'

const props = defineProps<{ data: PortfolioData }>()

const featured = computed(() => props.data.works.find((w) => w.highlight) ?? props.data.works[0])
const rest = computed(() => props.data.works.filter((w) => w.id !== featured.value?.id))
const stats = computed(() => props.data.profile.stats)
const initials = computed(() => props.data.profile.name.slice(0, 1))
const year = computed(() => new Date().getFullYear())
</script>

<template>
  <div class="b-page">
    <!-- 导航栏 -->
    <nav class="b-navbar">
      <a class="b-brand" href="#top">
        <span class="brand-mark">{{ initials }}</span>
        <span class="brand-name">{{ data.profile.name }}</span>
      </a>
      <div class="nav-links">
        <a href="#works">作品</a>
        <a href="#skills">技术</a>
        <a href="#about">关于</a>
        <a href="#timeline">历程</a>
        <a href="#contact">联系</a>
      </div>
      <a class="nav-cta" href="#contact">联系我 <ArrowUpRight :size="14" :stroke-width="2.5" /></a>
    </nav>

    <main id="top">
      <!-- Hero -->
      <section class="b-hero">
        <div class="hero-grid-line"></div>
        <div class="hero-inner">
          <div class="hero-left reveal">
            <span class="eyebrow">
              <Square :size="10" :stroke-width="3" fill="currentColor" />
              {{ data.profile.title }}
            </span>
            <h1 class="hero-title">
              把想法变成<br />
              <em>真正被使用</em>的产品
            </h1>
            <p class="hero-sub">{{ data.profile.tagline }}</p>
            <p class="hero-bio">{{ data.profile.bio }}</p>
            <div class="hero-actions">
              <a class="btn btn-primary" href="#works">查看作品 <ArrowUpRight :size="16" :stroke-width="2.5" /></a>
              <a class="btn btn-ghost" href="#contact">联系我</a>
            </div>
          </div>

          <div v-if="featured" class="hero-right reveal">
            <article
              class="feature-card"
              :data-work-id="featured.id"
              role="button"
              tabindex="0"
              :aria-label="featured.title"
            >
              <div class="feature-cover">
                <img :src="featured.cover" :alt="featured.title" loading="lazy" />
                <span class="feature-badge">旗舰作品</span>
              </div>
              <div class="feature-body">
                <span class="feature-cat">{{ featured.category }}</span>
                <h3 class="feature-title">{{ featured.title }}</h3>
                <p class="feature-desc">{{ featured.description }}</p>
                <div class="feature-tags">
                  <span v-for="t in featured.tags.slice(0, 4)" :key="t" class="chip">{{ t }}</span>
                </div>
                <span class="feature-cta">查看详情 <ArrowUpRight :size="15" :stroke-width="2.5" /></span>
              </div>
            </article>
          </div>
        </div>

        <!-- 横向统计条 -->
        <div class="hero-stats reveal">
          <div v-for="(s, i) in stats" :key="s.label" class="stat" :class="`stat-${i % 3}`">
            <span class="stat-value">{{ s.value }}</span>
            <span class="stat-label">{{ s.label }}</span>
          </div>
        </div>
      </section>

      <!-- 作品矩阵 -->
      <section id="works" class="b-section">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">[ 01 / 作品 ]</span>
          <h2 class="sec-title">产品矩阵</h2>
          <p class="sec-sub">每一个产品都解决真实问题,服务真实用户</p>
        </header>

        <div class="works-grid">
          <article
            v-for="w in rest"
            :key="w.id"
            class="work-card reveal"
            :data-work-id="w.id"
            role="button"
            tabindex="0"
            :aria-label="w.title"
          >
            <div class="work-cover">
              <img :src="w.cover" :alt="w.title" loading="lazy" />
              <span class="work-year">{{ w.year }}</span>
            </div>
            <div class="work-body">
              <span class="work-cat">{{ w.category }}</span>
              <h3 class="work-title">{{ w.title }}</h3>
              <p class="work-desc">{{ w.description }}</p>
              <div class="work-tags">
                <span v-for="t in w.tags.slice(0, 3)" :key="t" class="chip">{{ t }}</span>
              </div>
            </div>
          </article>
        </div>
      </section>

      <!-- 技术栈 -->
      <section id="skills" class="b-section b-section-mono">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">[ 02 / 技术 ]</span>
          <h2 class="sec-title">工具与技术</h2>
          <p class="sec-sub">从前端到 AI,具备全链路独立开发能力</p>
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
      <section id="about" class="b-section">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">[ 03 / 关于 ]</span>
          <h2 class="sec-title">创造者自述</h2>
        </header>
        <div class="about-grid">
          <div class="about-text reveal">
            <p>{{ data.profile.bio }}</p>
            <p class="about-extra">
              {{ data.profile.availability }} · {{ data.profile.location }}
            </p>
          </div>
          <div class="about-cards reveal">
            <div v-for="(s, i) in stats" :key="s.label" class="about-card" :class="`about-card-${i % 3}`">
              <span class="about-value">{{ s.value }}</span>
              <span class="about-label">{{ s.label }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 创作历程 -->
      <section id="timeline" class="b-section b-section-mono">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">[ 04 / 历程 ]</span>
          <h2 class="sec-title">里程碑</h2>
        </header>
        <ol class="timeline">
          <li v-for="(e, i) in data.experiences" :key="e.id" class="tl-item reveal" :style="{ '--i': i }">
            <span class="tl-index">{{ String(i + 1).padStart(2, '0') }}</span>
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
      <section id="contact" class="b-section">
        <div class="contact-card reveal">
          <div class="contact-left">
            <span class="sec-eyebrow">[ 05 / 联系 ]</span>
            <h2 class="sec-title">有想法?一起聊聊</h2>
            <p class="contact-sub">
              无论是合作机会、产品咨询,还是只是想打个招呼,我都很乐意聊聊。
            </p>
            <ul class="contact-list">
              <li>
                <Mail :size="18" :stroke-width="2" />
                <a :href="'mailto:' + data.profile.email">{{ data.profile.email }}</a>
              </li>
              <li>
                <MapPin :size="18" :stroke-width="2" />
                <span>{{ data.profile.location }}</span>
              </li>
              <li>
                <Clock :size="18" :stroke-width="2" />
                <span>{{ data.profile.availability }}</span>
              </li>
            </ul>
          </div>
          <div class="contact-right">
            <a class="btn btn-primary btn-lg" :href="'mailto:' + data.profile.email">
              发邮件给我 <ArrowUpRight :size="18" :stroke-width="2.5" />
            </a>
            <a
              v-for="s in data.profile.socials"
              :key="s.name"
              class="social-link"
              :href="s.url"
              target="_blank"
              rel="noopener noreferrer"
            >
              {{ s.name }} <ArrowUpRight :size="14" :stroke-width="2.5" />
            </a>
          </div>
        </div>
      </section>
    </main>

    <footer class="b-footer">
      <div class="footer-inner">
        <span class="footer-brand">{{ initials }} / {{ data.profile.name }}</span>
        <span class="footer-copy">© {{ year }} {{ data.profile.name }}. 基于 WorksShow 构建。</span>
      </div>
    </footer>

    <!-- 回到顶部 -->
    <button class="back-top" type="button" aria-label="回到顶部">
      <ArrowUp :size="18" :stroke-width="2.5" />
    </button>

    <!-- 作品详情弹窗（导出时由 runtime 切换 is-open） -->
    <div
      v-for="w in data.works"
      :key="w.id"
      class="b-modal"
      :data-modal-id="w.id"
    >
      <div class="modal-overlay"></div>
      <article class="modal-card">
        <button class="modal-close" aria-label="关闭"><X :size="20" :stroke-width="2" /></button>
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
            查看项目 <ArrowUpRight :size="16" :stroke-width="2.5" />
          </a>
        </div>
      </article>
    </div>
  </div>
</template>

<style scoped>
.b-page {
  /* 野性色彩系统：纯黑白 + 红/黄/蓝原色 */
  --b-bg: #f4f1ea;
  --b-bg-soft: #ebe7dd;
  --b-card: #ffffff;
  --b-ink: #0a0a0a;
  --b-ink-soft: #4a4a4a;
  --b-ink-mute: #8a8a8a;
  --b-red: #ff3b00;
  --b-yellow: #ffd600;
  --b-blue: #0046ff;
  --b-border: #0a0a0a;
  /* 硬阴影：无 blur,纯位移 */
  --shadow-hard: 8px 8px 0 var(--b-border);
  --shadow-hard-sm: 4px 4px 0 var(--b-border);
  --shadow-hard-lg: 12px 12px 0 var(--b-border);
  background: var(--b-bg);
  color: var(--b-ink);
  font-family: 'JetBrains Mono', 'Noto Sans SC', ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 15px;
  line-height: 1.7;
  min-height: 100vh;
  -webkit-font-smoothing: antialiased;
  /* 噪点纹理叠加 */
  background-image:
    radial-gradient(circle at 1px 1px, rgba(0,0,0,0.06) 1px, transparent 0);
  background-size: 24px 24px;
}

.b-page * {
  box-sizing: border-box;
}

/* —— 导航栏 —— */
.b-navbar {
  position: sticky;
  top: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 16px clamp(20px, 5vw, 64px);
  background: var(--b-bg);
  border-bottom: 3px solid var(--b-border);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.b-navbar.is-scrolled {
  box-shadow: 0 6px 0 -3px var(--b-border);
}
.b-brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: var(--b-ink);
}
.brand-mark {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-weight: 700;
  font-size: 18px;
  color: var(--b-card);
  background: var(--b-ink);
  border: 3px solid var(--b-border);
  box-shadow: var(--shadow-hard-sm);
}
.brand-name {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}
.nav-links {
  display: flex;
  gap: 28px;
}
.nav-links a {
  font-size: 13px;
  font-weight: 500;
  color: var(--b-ink);
  text-decoration: none;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  transition: color 0.15s ease, background 0.15s ease;
  padding: 4px 8px;
}
.nav-links a:hover {
  color: var(--b-card);
  background: var(--b-ink);
}
.nav-cta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  font-size: 13px;
  font-weight: 700;
  color: var(--b-card);
  background: var(--b-ink);
  border: 3px solid var(--b-border);
  box-shadow: var(--shadow-hard-sm);
  text-decoration: none;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.nav-cta:hover {
  transform: translate(-2px, -2px);
  box-shadow: var(--shadow-hard);
}
.nav-cta:active {
  transform: translate(2px, 2px);
  box-shadow: 0 0 0 var(--b-border);
}

/* —— 通用按钮 —— */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 24px;
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 14px;
  font-weight: 700;
  text-decoration: none;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  border: 3px solid var(--b-border);
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease, background 0.15s ease, color 0.15s ease;
}
.btn-primary {
  color: var(--b-card);
  background: var(--b-red);
  box-shadow: var(--shadow-hard-sm);
}
.btn-primary:hover {
  transform: translate(-2px, -2px);
  box-shadow: var(--shadow-hard);
}
.btn-primary:active {
  transform: translate(2px, 2px);
  box-shadow: 0 0 0 var(--b-border);
}
.btn-ghost {
  color: var(--b-ink);
  background: var(--b-card);
  box-shadow: var(--shadow-hard-sm);
}
.btn-ghost:hover {
  transform: translate(-2px, -2px);
  box-shadow: var(--shadow-hard);
  background: var(--b-yellow);
}
.btn-lg {
  padding: 18px 32px;
  font-size: 15px;
}

/* —— Hero —— */
.b-hero {
  position: relative;
  padding: clamp(48px, 7vw, 88px) clamp(20px, 5vw, 64px) clamp(40px, 5vw, 64px);
  border-bottom: 3px solid var(--b-border);
  overflow: hidden;
}
/* Hero 背景网格线 */
.hero-grid-line {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(to right, transparent calc(50% - 0.5px), rgba(0,0,0,0.06) calc(50% - 0.5px), rgba(0,0,0,0.06) calc(50% + 0.5px), transparent calc(50% + 0.5px));
  pointer-events: none;
}
.hero-inner {
  position: relative;
  max-width: 1180px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.15fr 1fr;
  gap: clamp(32px, 5vw, 64px);
  align-items: center;
}
.eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  font-size: 12px;
  font-weight: 700;
  color: var(--b-ink);
  background: var(--b-yellow);
  border: 2px solid var(--b-border);
  text-transform: uppercase;
  letter-spacing: 0.1em;
  margin-bottom: 24px;
}
.hero-title {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: clamp(40px, 6.5vw, 80px);
  font-weight: 700;
  line-height: 0.98;
  letter-spacing: -0.03em;
  margin: 0 0 24px;
  color: var(--b-ink);
  text-transform: uppercase;
}
.hero-title em {
  font-style: normal;
  display: inline-block;
  background: var(--b-ink);
  color: var(--b-yellow);
  padding: 0 12px;
  transform: rotate(-1deg);
  box-shadow: var(--shadow-hard-sm);
}
.hero-sub {
  font-size: 18px;
  color: var(--b-ink);
  font-weight: 500;
  margin: 0 0 12px;
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
}
.hero-bio {
  font-size: 14px;
  color: var(--b-ink-soft);
  margin: 0 0 32px;
  max-width: 520px;
  line-height: 1.8;
}
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

/* Hero 右侧特色卡 */
.feature-card {
  background: var(--b-card);
  border: 4px solid var(--b-border);
  box-shadow: var(--shadow-hard-lg);
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.feature-card:hover {
  transform: translate(-4px, -4px);
  box-shadow: 16px 16px 0 var(--b-border);
}
.feature-cover {
  position: relative;
  aspect-ratio: 16 / 10;
  overflow: hidden;
  background: var(--b-bg-soft);
  border-bottom: 4px solid var(--b-border);
}
.feature-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: contrast(1.1) saturate(0.9);
}
.feature-badge {
  position: absolute;
  top: 0;
  left: 0;
  padding: 8px 14px;
  font-size: 11px;
  font-weight: 700;
  color: var(--b-card);
  background: var(--b-red);
  border: 2px solid var(--b-border);
  border-top: none;
  border-left: none;
  text-transform: uppercase;
  letter-spacing: 0.1em;
}
.feature-body {
  padding: 24px 26px 28px;
}
.feature-cat {
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--b-red);
  font-weight: 700;
}
.feature-title {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 24px;
  font-weight: 700;
  margin: 8px 0 12px;
  color: var(--b-ink);
  letter-spacing: -0.02em;
}
.feature-desc {
  font-size: 13px;
  color: var(--b-ink-soft);
  line-height: 1.7;
  margin: 0 0 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.feature-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
}
.feature-cta {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 13px;
  font-weight: 700;
  color: var(--b-ink);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  border-bottom: 2px solid var(--b-ink);
  padding-bottom: 2px;
}

.chip {
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 500;
  color: var(--b-ink);
  background: var(--b-bg-soft);
  border: 2px solid var(--b-border);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* —— 横向统计条 —— */
.hero-stats {
  position: relative;
  max-width: 1180px;
  margin: clamp(40px, 6vw, 72px) auto 0;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  border: 3px solid var(--b-border);
  box-shadow: var(--shadow-hard);
}
.stat {
  padding: 24px 20px;
  border-right: 3px solid var(--b-border);
  background: var(--b-card);
}
.stat:last-child {
  border-right: none;
}
.stat-0 { background: var(--b-card); }
.stat-1 { background: var(--b-yellow); }
.stat-2 { background: var(--b-card); }
.stat-value {
  display: block;
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 36px;
  font-weight: 700;
  color: var(--b-ink);
  letter-spacing: -0.03em;
  line-height: 1;
  margin-bottom: 6px;
}
.stat-label {
  font-size: 11px;
  color: var(--b-ink-soft);
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

/* —— 通用 section —— */
.b-section {
  padding: clamp(64px, 8vw, 104px) clamp(20px, 5vw, 64px);
  border-bottom: 3px solid var(--b-border);
}
.b-section-mono {
  background: var(--b-bg-soft);
}
.sec-head {
  max-width: 760px;
  margin: 0 auto clamp(40px, 5vw, 64px);
}
.sec-eyebrow {
  display: inline-block;
  font-size: 12px;
  font-weight: 700;
  color: var(--b-red);
  margin-bottom: 14px;
  text-transform: uppercase;
  letter-spacing: 0.1em;
}
.sec-title {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: clamp(32px, 4.5vw, 52px);
  font-weight: 700;
  letter-spacing: -0.03em;
  margin: 0 0 12px;
  color: var(--b-ink);
  text-transform: uppercase;
  line-height: 1;
}
.sec-sub {
  font-size: 14px;
  color: var(--b-ink-soft);
  margin: 0;
}

/* —— 作品网格 —— */
.works-grid {
  max-width: 1180px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0;
  border: 3px solid var(--b-border);
  box-shadow: var(--shadow-hard);
}
.work-card {
  background: var(--b-card);
  border-right: 3px solid var(--b-border);
  border-bottom: 3px solid var(--b-border);
  cursor: pointer;
  transition: background 0.15s ease;
}
.work-card:nth-child(2n) {
  border-right: none;
}
.work-card:nth-last-child(-n+2):not(:nth-last-child(1)):not(:nth-last-child(2)) {
  border-bottom: 3px solid var(--b-border);
}
.work-card:hover {
  background: var(--b-yellow);
}
.work-cover {
  position: relative;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background: var(--b-bg);
  border-bottom: 3px solid var(--b-border);
}
.work-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: contrast(1.1) saturate(0.85);
  transition: transform 0.3s ease;
}
.work-card:hover .work-cover img {
  transform: scale(1.04);
}
.work-year {
  position: absolute;
  top: 0;
  right: 0;
  padding: 6px 12px;
  font-size: 11px;
  font-weight: 700;
  color: var(--b-card);
  background: var(--b-ink);
  border: 2px solid var(--b-border);
  border-top: none;
  border-right: none;
  letter-spacing: 0.08em;
}
.work-body {
  padding: 22px 24px 26px;
}
.work-cat {
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--b-blue);
  font-weight: 700;
}
.work-title {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 20px;
  font-weight: 700;
  margin: 8px 0 10px;
  color: var(--b-ink);
  letter-spacing: -0.02em;
}
.work-desc {
  font-size: 13px;
  color: var(--b-ink-soft);
  line-height: 1.65;
  margin: 0 0 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.work-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

/* —— 技术栈 —— */
.skills-grid {
  max-width: 1180px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0;
  border: 3px solid var(--b-border);
  box-shadow: var(--shadow-hard);
  background: var(--b-border);
}
.skill-card {
  background: var(--b-card);
  border-right: 3px solid var(--b-border);
  border-bottom: 3px solid var(--b-border);
  padding: 28px 28px 26px;
}
.skill-card:nth-child(2n) {
  border-right: none;
}
.skill-card:nth-last-child(-n+2) {
  border-bottom: none;
}
.skill-head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 2px dashed var(--b-ink);
}
.skill-cn {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 18px;
  font-weight: 700;
  color: var(--b-ink);
}
.skill-en {
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--b-ink-mute);
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
  padding: 6px 12px;
  font-size: 12px;
  color: var(--b-ink);
  background: var(--b-bg-soft);
  border: 2px solid var(--b-border);
  letter-spacing: 0.03em;
}

/* —— 关于我 —— */
.about-grid {
  max-width: 1080px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: clamp(32px, 5vw, 56px);
  align-items: center;
}
.about-text p {
  font-size: 16px;
  color: var(--b-ink-soft);
  line-height: 1.9;
  margin: 0 0 18px;
}
.about-extra {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 14px !important;
  color: var(--b-ink) !important;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 12px 16px;
  background: var(--b-yellow);
  border: 2px solid var(--b-border);
  display: inline-block;
  box-shadow: var(--shadow-hard-sm);
}
.about-cards {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0;
  border: 3px solid var(--b-border);
  box-shadow: var(--shadow-hard);
}
.about-card {
  padding: 20px 24px;
  border-bottom: 3px solid var(--b-border);
  background: var(--b-card);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.about-card:last-child {
  border-bottom: none;
}
.about-card-0 { background: var(--b-card); }
.about-card-1 { background: var(--b-yellow); }
.about-card-2 { background: var(--b-card); }
.about-value {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 32px;
  font-weight: 700;
  color: var(--b-ink);
  letter-spacing: -0.03em;
  line-height: 1;
}
.about-label {
  font-size: 11px;
  color: var(--b-ink-soft);
  text-transform: uppercase;
  letter-spacing: 0.1em;
  text-align: right;
}

/* —— 时间线 —— */
.timeline {
  max-width: 860px;
  margin: 0 auto;
  list-style: none;
  padding: 0;
  position: relative;
}
.timeline::before {
  content: '';
  position: absolute;
  left: 24px;
  top: 8px;
  bottom: 8px;
  width: 3px;
  background: var(--b-ink);
}
.tl-item {
  position: relative;
  padding-left: 72px;
  padding-bottom: 32px;
}
.tl-item:last-child {
  padding-bottom: 0;
}
.tl-index {
  position: absolute;
  left: 0;
  top: 0;
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  font-family: 'Space Grotesk', sans-serif;
  font-size: 16px;
  font-weight: 700;
  color: var(--b-card);
  background: var(--b-ink);
  border: 3px solid var(--b-border);
  box-shadow: var(--shadow-hard-sm);
}
.tl-content {
  background: var(--b-card);
  border: 3px solid var(--b-border);
  box-shadow: var(--shadow-hard-sm);
  padding: 22px 26px;
}
.tl-period {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  color: var(--b-card);
  background: var(--b-ink);
  padding: 4px 10px;
  margin-bottom: 10px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}
.tl-title {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 4px;
  color: var(--b-ink);
  letter-spacing: -0.02em;
}
.tl-org {
  font-size: 12px;
  color: var(--b-blue);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}
.tl-desc {
  font-size: 13px;
  color: var(--b-ink-soft);
  line-height: 1.7;
  margin: 10px 0 0;
}

/* —— 联系方式 —— */
.contact-card {
  max-width: 1080px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: clamp(32px, 5vw, 56px);
  align-items: center;
  background: var(--b-card);
  border: 4px solid var(--b-border);
  box-shadow: var(--shadow-hard-lg);
  padding: clamp(36px, 5vw, 56px);
}
.contact-card .sec-eyebrow {
  margin-bottom: 8px;
}
.contact-card .sec-title {
  margin-bottom: 14px;
}
.contact-sub {
  font-size: 14px;
  color: var(--b-ink-soft);
  line-height: 1.8;
  margin: 0 0 24px;
}
.contact-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.contact-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: var(--b-ink);
}
.contact-list li :deep(svg) {
  color: var(--b-red);
  flex-shrink: 0;
}
.contact-list a {
  color: var(--b-ink);
  text-decoration: none;
  border-bottom: 2px solid var(--b-ink);
  transition: color 0.15s ease, border-color 0.15s ease;
}
.contact-list a:hover {
  color: var(--b-red);
  border-bottom-color: var(--b-red);
}
.contact-right {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 16px;
}
.social-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 13px;
  font-weight: 700;
  color: var(--b-ink);
  text-decoration: none;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  border-bottom: 2px solid var(--b-ink);
  padding-bottom: 2px;
  transition: color 0.15s ease, border-color 0.15s ease;
}
.social-link:hover {
  color: var(--b-blue);
  border-bottom-color: var(--b-blue);
}

/* —— 页脚 —— */
.b-footer {
  padding: 28px clamp(20px, 5vw, 64px);
  border-top: 3px solid var(--b-border);
  background: var(--b-ink);
  color: var(--b-card);
}
.footer-inner {
  max-width: 1180px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.footer-brand {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-weight: 700;
  color: var(--b-card);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}
.footer-copy {
  font-size: 12px;
  color: var(--b-ink-mute);
}

/* —— 回到顶部 —— */
.back-top {
  position: fixed;
  right: 28px;
  bottom: 28px;
  z-index: 40;
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  color: var(--b-card);
  background: var(--b-ink);
  border: 3px solid var(--b-border);
  box-shadow: var(--shadow-hard-sm);
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
}
.back-top:hover {
  transform: translate(-2px, -2px);
  box-shadow: var(--shadow-hard);
  background: var(--b-red);
}
.back-top:active {
  transform: translate(2px, 2px);
  box-shadow: 0 0 0 var(--b-border);
}

/* —— 作品弹窗 —— */
.b-modal {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: none;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.b-modal.is-open {
  display: flex;
}
.modal-overlay {
  position: absolute;
  inset: 0;
  background: rgba(10, 10, 10, 0.7);
}
.modal-card {
  position: relative;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  max-width: 920px;
  width: 100%;
  max-height: 88vh;
  background: var(--b-card);
  border: 4px solid var(--b-border);
  box-shadow: var(--shadow-hard-lg);
  overflow: hidden;
}
.modal-close {
  position: absolute;
  top: 0;
  right: 0;
  z-index: 2;
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  color: var(--b-card);
  background: var(--b-ink);
  border: 3px solid var(--b-border);
  border-top: none;
  border-right: none;
  cursor: pointer;
  transition: background 0.15s ease;
}
.modal-close:hover {
  background: var(--b-red);
}
.modal-cover {
  position: relative;
  overflow: hidden;
  min-height: 300px;
  background: var(--b-bg-soft);
  border-right: 4px solid var(--b-border);
}
.modal-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: contrast(1.1) saturate(0.85);
}
.modal-year {
  position: absolute;
  bottom: 14px;
  left: 14px;
  padding: 6px 12px;
  font-size: 11px;
  font-weight: 700;
  color: var(--b-card);
  background: var(--b-ink);
  border: 2px solid var(--b-border);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}
.modal-body {
  padding: 36px 36px 32px;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.modal-cat {
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--b-red);
  font-weight: 700;
  margin-bottom: 10px;
}
.modal-title {
  font-family: 'Space Grotesk', 'Noto Sans SC', sans-serif;
  font-size: 28px;
  font-weight: 700;
  color: var(--b-ink);
  margin: 0 0 14px;
  line-height: 1.1;
  letter-spacing: -0.02em;
}
.modal-desc {
  font-size: 14px;
  color: var(--b-ink-soft);
  line-height: 1.8;
  margin: 0 0 22px;
}
.modal-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-bottom: 26px;
}
.modal-link {
  align-self: flex-start;
  margin-top: auto;
}

/* —— 渐入动画（粗野风:硬切,无缓动模糊） —— */
.reveal {
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.2s steps(4, end), transform 0.2s steps(4, end);
  transition-delay: calc(var(--i, 0) * 60ms);
}
.reveal.is-visible {
  opacity: 1;
  transform: none;
}

/* —— 响应式 —— */
@media (max-width: 880px) {
  .hero-inner,
  .about-grid,
  .contact-card {
    grid-template-columns: 1fr;
  }
  .hero-right {
    order: -1;
  }
  .works-grid,
  .skills-grid {
    grid-template-columns: 1fr;
  }
  .work-card,
  .skill-card {
    border-right: none !important;
    border-bottom: 3px solid var(--b-border) !important;
  }
  .work-card:last-child,
  .skill-card:last-child {
    border-bottom: none !important;
  }
  .nav-links {
    display: none;
  }
  .hero-stats {
    grid-template-columns: 1fr;
  }
  .stat {
    border-right: none;
    border-bottom: 3px solid var(--b-border);
  }
  .stat:last-child {
    border-bottom: none;
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
    border-right: none;
    border-bottom: 4px solid var(--b-border);
  }
  .modal-body {
    padding: 24px;
  }
  .hero-title em {
    padding: 0 8px;
  }
}
</style>

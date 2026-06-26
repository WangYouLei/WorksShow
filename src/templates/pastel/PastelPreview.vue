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
} from 'lucide-vue-next'
import type { PortfolioData } from '../types'
import type { Work } from '@/data/types'

const props = defineProps<{ data: PortfolioData }>()

const featured = computed(() => props.data.works.find((w) => w.highlight) ?? props.data.works[0])
const rest = computed(() => props.data.works.filter((w) => w.id !== featured.value?.id))
const stats = computed(() => props.data.profile.stats)
const initials = computed(() => props.data.profile.name.slice(0, 1))
</script>

<template>
  <div class="p-page">
    <!-- 导航栏 -->
    <nav class="p-navbar">
      <a class="p-brand" href="#top">
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
      <a class="nav-cta" href="#contact">联系我</a>
    </nav>

    <main id="top">
      <!-- Hero -->
      <section class="p-hero">
        <div class="hero-inner">
          <div class="hero-left reveal">
            <span class="eyebrow"><Sparkles :size="14" :stroke-width="2" /> {{ data.profile.title }}</span>
            <h1 class="hero-title">
              把想法变成<br />
              <em>真正被使用</em>的产品
            </h1>
            <p class="hero-sub">{{ data.profile.tagline }}</p>
            <p class="hero-bio">{{ data.profile.bio }}</p>
            <div class="hero-actions">
              <a class="btn btn-primary" href="#works">查看作品 <ArrowUpRight :size="16" :stroke-width="2" /></a>
              <a class="btn btn-ghost" href="#contact">联系我</a>
            </div>
            <div class="hero-stats">
              <div v-for="s in stats" :key="s.label" class="stat">
                <span class="stat-value">{{ s.value }}</span>
                <span class="stat-label">{{ s.label }}</span>
              </div>
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
                <span class="feature-cta">查看详情 <ArrowUpRight :size="15" :stroke-width="2" /></span>
              </div>
            </article>
          </div>
        </div>
      </section>

      <!-- 作品矩阵 -->
      <section id="works" class="p-section">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">精选作品</span>
          <h2 class="sec-title">我的产品矩阵</h2>
          <p class="sec-sub">每一个产品都解决真实问题，服务真实用户</p>
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
      <section id="skills" class="p-section p-section-soft">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">技术栈</span>
          <h2 class="sec-title">精通的工具与技术</h2>
          <p class="sec-sub">从前端到 AI，具备全链路独立开发能力</p>
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
      <section id="about" class="p-section">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">关于我</span>
          <h2 class="sec-title">创造者的自我介绍</h2>
        </header>
        <div class="about-grid">
          <div class="about-text reveal">
            <p>{{ data.profile.bio }}</p>
            <p class="about-extra">
              {{ data.profile.availability }} · {{ data.profile.location }}
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

      <!-- 创作历程 -->
      <section id="timeline" class="p-section p-section-soft">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">创作历程</span>
          <h2 class="sec-title">每个里程碑都是一次突破</h2>
        </header>
        <ol class="timeline">
          <li v-for="(e, i) in data.experiences" :key="e.id" class="tl-item reveal" :style="{ '--i': i }">
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
      <section id="contact" class="p-section">
        <div class="contact-card reveal">
          <div class="contact-left">
            <span class="sec-eyebrow">联系方式</span>
            <h2 class="sec-title">有想法？一起聊聊</h2>
            <p class="contact-sub">
              无论是合作机会、产品咨询，还是只是想打个招呼，我都很乐意聊聊。
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
              {{ s.name }} <ArrowUpRight :size="14" :stroke-width="2" />
            </a>
          </div>
        </div>
      </section>
    </main>

    <footer class="p-footer">
      <div class="footer-inner">
        <span class="footer-brand">{{ initials }} · {{ data.profile.name }}</span>
        <span class="footer-copy">© {{ new Date().getFullYear() }} {{ data.profile.name }}. 基于 WorksShow 构建。</span>
      </div>
    </footer>

    <!-- 回到顶部 -->
    <button class="back-top" type="button" aria-label="回到顶部">
      <ArrowUp :size="18" :stroke-width="2" />
    </button>

    <!-- 作品详情弹窗（导出时由 runtime 切换 is-open） -->
    <div
      v-for="w in data.works"
      :key="w.id"
      class="p-modal"
      :data-modal-id="w.id"
    >
      <div class="modal-overlay"></div>
      <article class="modal-card">
        <button class="modal-close" aria-label="关闭"><X :size="20" :stroke-width="1.75" /></button>
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
.p-page {
  --p-bg: #fbf7f2;
  --p-bg-soft: #f4ede4;
  --p-card: #ffffff;
  --p-ink: #3d3536;
  --p-ink-soft: #7a6f6a;
  --p-ink-mute: #a89e99;
  --p-pink: #e8a0a0;
  --p-pink-soft: #f6dcdc;
  --p-mint: #9fc7b4;
  --p-mint-soft: #dceee4;
  --p-lilac: #c3b1e1;
  --p-border: #ece2d6;
  --radius-sm: 10px;
  --radius-md: 18px;
  --radius-lg: 28px;
  --shadow-soft: 0 18px 50px -24px rgba(60, 45, 35, 0.22);
  --shadow-lift: 0 30px 70px -30px rgba(60, 45, 35, 0.32);
  background: var(--p-bg);
  color: var(--p-ink);
  font-family: 'Noto Sans SC', system-ui, -apple-system, 'Segoe UI', sans-serif;
  font-size: 15px;
  line-height: 1.75;
  min-height: 100vh;
  -webkit-font-smoothing: antialiased;
}

.p-page * {
  box-sizing: border-box;
}

/* —— 导航栏 —— */
.p-navbar {
  position: sticky;
  top: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 18px clamp(20px, 5vw, 64px);
  background: rgba(251, 247, 242, 0.72);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-bottom: 1px solid transparent;
  transition: border-color 0.3s ease, background 0.3s ease, box-shadow 0.3s ease;
}
.p-navbar.is-scrolled {
  border-bottom-color: var(--p-border);
  box-shadow: 0 8px 30px -20px rgba(60, 45, 35, 0.18);
  background: rgba(251, 247, 242, 0.92);
}
.p-brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--p-ink);
}
.brand-mark {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  font-weight: 700;
  font-size: 16px;
  color: #fff;
  background: linear-gradient(135deg, var(--p-pink) 0%, var(--p-lilac) 100%);
  border-radius: 12px;
  box-shadow: 0 6px 18px -8px rgba(232, 160, 160, 0.7);
}
.brand-name {
  font-weight: 600;
  letter-spacing: 0.5px;
}
.nav-links {
  display: flex;
  gap: 28px;
}
.nav-links a {
  font-size: 14px;
  color: var(--p-ink-soft);
  text-decoration: none;
  transition: color 0.2s ease;
}
.nav-links a:hover {
  color: var(--p-pink);
}
.nav-cta {
  padding: 9px 18px;
  font-size: 14px;
  font-weight: 500;
  color: #fff;
  background: var(--p-ink);
  border-radius: 999px;
  text-decoration: none;
  transition: background 0.2s ease, transform 0.2s ease;
}
.nav-cta:hover {
  background: var(--p-pink);
  transform: translateY(-1px);
}

/* —— 通用按钮 —— */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 22px;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  border-radius: 999px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease, color 0.2s ease;
}
.btn-primary {
  color: #fff;
  background: linear-gradient(135deg, var(--p-pink) 0%, #d98a8a 100%);
  box-shadow: 0 12px 28px -12px rgba(232, 160, 160, 0.8);
}
.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 36px -14px rgba(232, 160, 160, 0.9);
}
.btn-ghost {
  color: var(--p-ink);
  background: var(--p-card);
  border-color: var(--p-border);
}
.btn-ghost:hover {
  border-color: var(--p-pink);
  color: var(--p-pink);
}
.btn-lg {
  padding: 16px 28px;
  font-size: 15px;
}

/* —— Hero —— */
.p-hero {
  padding: clamp(60px, 9vw, 110px) clamp(20px, 5vw, 64px) clamp(60px, 8vw, 100px);
  background:
    radial-gradient(circle at 85% 15%, var(--p-pink-soft) 0%, transparent 42%),
    radial-gradient(circle at 10% 85%, var(--p-mint-soft) 0%, transparent 45%);
}
.hero-inner {
  max-width: 1180px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.15fr 1fr;
  gap: clamp(32px, 5vw, 72px);
  align-items: center;
}
.eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  font-size: 13px;
  color: var(--p-pink);
  background: var(--p-pink-soft);
  border-radius: 999px;
  margin-bottom: 22px;
}
.hero-title {
  font-size: clamp(34px, 5.4vw, 60px);
  font-weight: 800;
  line-height: 1.15;
  letter-spacing: -0.5px;
  margin: 0 0 20px;
  color: var(--p-ink);
}
.hero-title em {
  font-style: normal;
  background: linear-gradient(120deg, var(--p-pink) 0%, var(--p-lilac) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.hero-sub {
  font-size: 17px;
  color: var(--p-ink);
  font-weight: 500;
  margin: 0 0 12px;
}
.hero-bio {
  font-size: 15px;
  color: var(--p-ink-soft);
  margin: 0 0 28px;
  max-width: 520px;
}
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-bottom: 40px;
}
.hero-stats {
  display: flex;
  gap: 36px;
}
.stat-value {
  display: block;
  font-size: 30px;
  font-weight: 800;
  color: var(--p-ink);
  letter-spacing: -0.5px;
}
.stat-label {
  font-size: 13px;
  color: var(--p-ink-mute);
}

/* Hero 右侧特色卡 */
.feature-card {
  background: var(--p-card);
  border: 1px solid var(--p-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-lift);
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.feature-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 40px 80px -32px rgba(60, 45, 35, 0.4);
}
.feature-cover {
  position: relative;
  aspect-ratio: 16 / 10;
  overflow: hidden;
  background: var(--p-bg-soft);
}
.feature-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.feature-badge {
  position: absolute;
  top: 14px;
  left: 14px;
  padding: 5px 12px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, var(--p-pink) 0%, var(--p-lilac) 100%);
  border-radius: 999px;
}
.feature-body {
  padding: 24px 26px 28px;
}
.feature-cat {
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--p-pink);
  font-weight: 600;
}
.feature-title {
  font-size: 22px;
  font-weight: 700;
  margin: 8px 0 10px;
  color: var(--p-ink);
}
.feature-desc {
  font-size: 14px;
  color: var(--p-ink-soft);
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
  font-size: 14px;
  font-weight: 600;
  color: var(--p-pink);
}

.chip {
  padding: 4px 11px;
  font-size: 12px;
  color: var(--p-ink-soft);
  background: var(--p-bg-soft);
  border-radius: 999px;
  border: 1px solid var(--p-border);
}

/* —— 通用 section —— */
.p-section {
  padding: clamp(70px, 9vw, 110px) clamp(20px, 5vw, 64px);
}
.p-section-soft {
  background: var(--p-bg-soft);
}
.sec-head {
  max-width: 720px;
  margin: 0 auto clamp(40px, 5vw, 64px);
  text-align: center;
}
.sec-eyebrow {
  display: inline-block;
  font-size: 13px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--p-pink);
  font-weight: 600;
  margin-bottom: 12px;
}
.sec-title {
  font-size: clamp(26px, 3.6vw, 38px);
  font-weight: 800;
  letter-spacing: -0.4px;
  margin: 0 0 12px;
  color: var(--p-ink);
}
.sec-sub {
  font-size: 15px;
  color: var(--p-ink-soft);
  margin: 0;
}

/* —— 作品网格 —— */
.works-grid {
  max-width: 1180px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 26px;
}
.work-card {
  background: var(--p-card);
  border: 1px solid var(--p-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-soft);
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.work-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lift);
}
.work-cover {
  position: relative;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background: var(--p-bg-soft);
}
.work-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.work-card:hover .work-cover img {
  transform: scale(1.05);
}
.work-year {
  position: absolute;
  bottom: 12px;
  right: 12px;
  padding: 4px 10px;
  font-size: 12px;
  color: var(--p-ink);
  background: rgba(255, 255, 255, 0.9);
  border-radius: 999px;
  backdrop-filter: blur(4px);
}
.work-body {
  padding: 22px 22px 26px;
}
.work-cat {
  font-size: 12px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--p-mint);
  font-weight: 600;
}
.work-title {
  font-size: 19px;
  font-weight: 700;
  margin: 8px 0 8px;
  color: var(--p-ink);
}
.work-desc {
  font-size: 14px;
  color: var(--p-ink-soft);
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
  gap: 24px;
}
.skill-card {
  background: var(--p-card);
  border: 1px solid var(--p-border);
  border-radius: var(--radius-md);
  padding: 28px 28px 26px;
  box-shadow: var(--shadow-soft);
}
.skill-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px dashed var(--p-border);
}
.skill-cn {
  font-size: 17px;
  font-weight: 700;
  color: var(--p-ink);
}
.skill-en {
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--p-ink-mute);
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
  padding: 6px 13px;
  font-size: 13px;
  color: var(--p-ink);
  background: var(--p-bg);
  border: 1px solid var(--p-border);
  border-radius: 999px;
}

/* —— 关于我 —— */
.about-grid {
  max-width: 1080px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: clamp(32px, 5vw, 64px);
  align-items: center;
}
.about-text p {
  font-size: 16px;
  color: var(--p-ink-soft);
  line-height: 1.9;
  margin: 0 0 18px;
}
.about-extra {
  font-size: 14px !important;
  color: var(--p-pink) !important;
  font-weight: 500;
}
.about-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.about-card {
  background: var(--p-card);
  border: 1px solid var(--p-border);
  border-radius: var(--radius-md);
  padding: 22px 16px;
  text-align: center;
  box-shadow: var(--shadow-soft);
}
.about-value {
  display: block;
  font-size: 26px;
  font-weight: 800;
  color: var(--p-pink);
  letter-spacing: -0.5px;
}
.about-label {
  font-size: 12px;
  color: var(--p-ink-mute);
  margin-top: 4px;
}

/* —— 时间线 —— */
.timeline {
  max-width: 820px;
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
  width: 2px;
  background: linear-gradient(var(--p-pink-soft), var(--p-mint-soft));
}
.tl-item {
  position: relative;
  padding-left: 40px;
  padding-bottom: 36px;
}
.tl-item:last-child {
  padding-bottom: 0;
}
.tl-dot {
  position: absolute;
  left: 0;
  top: 6px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--p-card);
  border: 3px solid var(--p-pink);
  box-shadow: 0 0 0 4px var(--p-pink-soft);
}
.tl-content {
  background: var(--p-card);
  border: 1px solid var(--p-border);
  border-radius: var(--radius-md);
  padding: 22px 26px;
  box-shadow: var(--shadow-soft);
}
.tl-period {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  color: var(--p-pink);
  background: var(--p-pink-soft);
  padding: 3px 10px;
  border-radius: 999px;
  margin-bottom: 10px;
}
.tl-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 4px;
  color: var(--p-ink);
}
.tl-org {
  font-size: 13px;
  color: var(--p-mint);
  font-weight: 600;
}
.tl-desc {
  font-size: 14px;
  color: var(--p-ink-soft);
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
  background: linear-gradient(135deg, #fff 0%, var(--p-pink-soft) 100%);
  border: 1px solid var(--p-border);
  border-radius: var(--radius-lg);
  padding: clamp(36px, 5vw, 56px);
  box-shadow: var(--shadow-lift);
}
.contact-card .sec-eyebrow {
  margin-bottom: 8px;
}
.contact-card .sec-title {
  margin-bottom: 14px;
}
.contact-sub {
  font-size: 15px;
  color: var(--p-ink-soft);
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
  font-size: 15px;
  color: var(--p-ink);
}
.contact-list li :deep(svg) {
  color: var(--p-pink);
  flex-shrink: 0;
}
.contact-list a {
  color: var(--p-ink);
  text-decoration: none;
  transition: color 0.2s ease;
}
.contact-list a:hover {
  color: var(--p-pink);
}
.contact-right {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 14px;
}
.social-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: var(--p-ink-soft);
  text-decoration: none;
  transition: color 0.2s ease;
}
.social-link:hover {
  color: var(--p-pink);
}

/* —— 页脚 —— */
.p-footer {
  padding: 32px clamp(20px, 5vw, 64px);
  border-top: 1px solid var(--p-border);
  background: var(--p-bg-soft);
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
  font-weight: 600;
  color: var(--p-ink);
}
.footer-copy {
  font-size: 13px;
  color: var(--p-ink-mute);
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
  background: linear-gradient(135deg, var(--p-pink) 0%, var(--p-lilac) 100%);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 14px 30px -10px rgba(232, 160, 160, 0.8);
  transition: transform 0.2s ease;
}
.back-top:hover {
  transform: translateY(-3px);
}

/* —— 作品弹窗 —— */
.p-modal {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: none;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.p-modal.is-open {
  display: flex;
}
.modal-overlay {
  position: absolute;
  inset: 0;
  background: rgba(60, 45, 35, 0.45);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}
.modal-card {
  position: relative;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  max-width: 920px;
  width: 100%;
  max-height: 88vh;
  background: var(--p-card);
  border: 1px solid var(--p-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: 0 40px 90px -30px rgba(60, 45, 35, 0.5);
}
.modal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 2;
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  color: var(--p-ink);
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid var(--p-border);
  border-radius: 50%;
  cursor: pointer;
  transition: transform 0.2s ease, background 0.2s ease, color 0.2s ease;
}
.modal-close:hover {
  background: var(--p-pink);
  color: #fff;
  transform: rotate(90deg);
}
.modal-cover {
  position: relative;
  overflow: hidden;
  min-height: 300px;
  background: var(--p-bg-soft);
}
.modal-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.modal-year {
  position: absolute;
  bottom: 14px;
  left: 14px;
  padding: 4px 12px;
  font-size: 12px;
  color: var(--p-ink);
  background: rgba(255, 255, 255, 0.9);
  border-radius: 999px;
  backdrop-filter: blur(4px);
}
.modal-body {
  padding: 36px 36px 32px;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.modal-cat {
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--p-pink);
  font-weight: 600;
  margin-bottom: 10px;
}
.modal-title {
  font-size: 26px;
  font-weight: 800;
  color: var(--p-ink);
  margin: 0 0 14px;
  line-height: 1.3;
}
.modal-desc {
  font-size: 14px;
  color: var(--p-ink-soft);
  line-height: 1.85;
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

/* —— 渐入动画 —— */
.reveal {
  opacity: 0;
  transform: translateY(24px);
  transition: opacity 0.7s cubic-bezier(0.22, 1, 0.36, 1),
    transform 0.7s cubic-bezier(0.22, 1, 0.36, 1);
  transition-delay: calc(var(--i, 0) * 80ms);
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
  .nav-links {
    display: none;
  }
  .hero-stats {
    gap: 24px;
  }
  .about-cards {
    grid-template-columns: repeat(3, 1fr);
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
    padding: 24px;
  }
  .about-cards {
    grid-template-columns: 1fr;
  }
}
</style>

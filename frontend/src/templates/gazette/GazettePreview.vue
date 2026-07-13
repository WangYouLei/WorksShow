<script setup lang="ts">
import { computed } from 'vue'
import {
  X,
  ArrowUpRight,
  ArrowUp,
  Mail,
  MapPin,
  Clock,
} from 'lucide-vue-next'
import type { PortfolioData } from '../types'

const props = defineProps<{ data: PortfolioData }>()

// 头版头条：取高亮作品,无则取首个
const featured = computed(() => props.data.works.find((w) => w.highlight) ?? props.data.works[0])
// 其余作品进入多栏矩阵
const rest = computed(() => props.data.works.filter((w) => w.id !== featured.value?.id))
const stats = computed(() => props.data.profile.stats)
// 刊名首字母（用于报头标识）
const initials = computed(() => props.data.profile.name.slice(0, 1))
const year = computed(() => new Date().getFullYear())
// 报纸期号（用年份+月份模拟,增强印刷感）
const issueNo = computed(() => {
  const d = new Date()
  return `NO. ${(d.getFullYear() - 2020) * 12 + d.getMonth() + 1}`
})
// 报头日期（中文长格式）
const mastheadDate = computed(() => {
  const d = new Date()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 · 星期${weekdays[d.getDay()]}`
})
</script>

<template>
  <div class="g-page">
    <!-- 报头 Masthead：刊名 + 日期 + 期号 + 双横线（报纸风标志性结构） -->
    <header class="g-masthead">
      <div class="masthead-top">
        <span class="masthead-date">{{ mastheadDate }}</span>
        <span class="masthead-issue">{{ issueNo }}</span>
      </div>
      <h1 class="masthead-title">{{ data.profile.name }} · GAZETTE</h1>
      <p class="masthead-tagline">{{ data.profile.title }} — {{ data.profile.tagline }}</p>
      <div class="masthead-rule"></div>
      <div class="masthead-rule masthead-rule-thin"></div>
    </header>

    <!-- 导航栏 -->
    <nav class="g-navbar">
      <a class="g-brand" href="#top">
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
      <a class="nav-cta" href="#contact">联系我 <ArrowUpRight :size="14" :stroke-width="2" /></a>
    </nav>

    <main id="top">
      <!-- 头版头条 Hero -->
      <section class="g-hero">
        <div class="hero-inner">
          <div class="hero-left reveal">
            <span class="eyebrow">头版头条 · LEAD STORY</span>
            <h2 class="hero-title">
              把想法变成<br />
              <em>真正被使用</em>的产品
            </h2>
            <p class="hero-deck">{{ data.profile.tagline }}</p>
            <div class="hero-body">
              <p class="hero-bio">{{ data.profile.bio }}</p>
            </div>
            <div class="hero-actions">
              <a class="btn btn-primary" href="#works">阅读作品 <ArrowUpRight :size="15" :stroke-width="2" /></a>
              <a class="btn btn-ghost" href="#contact">联系作者</a>
            </div>
            <div class="hero-stats">
              <div v-for="s in stats" :key="s.label" class="stat">
                <span class="stat-value">{{ s.value }}</span>
                <span class="stat-label">{{ s.label }}</span>
              </div>
            </div>
          </div>

          <div v-if="featured" class="hero-right reveal">
            <span class="feature-eyebrow">旗舰报道 · FEATURE</span>
            <article
              class="feature-card"
              :data-work-id="featured.id"
              role="button"
              tabindex="0"
              :aria-label="featured.title"
            >
              <div class="feature-cover">
                <img :src="featured.cover" :alt="featured.title" loading="lazy" />
              </div>
              <div class="feature-body">
                <span class="feature-cat">{{ featured.category }}</span>
                <h3 class="feature-title">{{ featured.title }}</h3>
                <p class="feature-desc">{{ featured.description }}</p>
                <div class="feature-tags">
                  <span v-for="t in featured.tags.slice(0, 4)" :key="t" class="chip">{{ t }}</span>
                </div>
                <span class="feature-cta">阅读全文 <ArrowUpRight :size="14" :stroke-width="2" /></span>
              </div>
            </article>
          </div>
        </div>
      </section>

      <!-- 作品矩阵（多栏报纸式排版） -->
      <section id="works" class="g-section">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">栏目壹 · SECTION 01</span>
          <h2 class="sec-title">产品矩阵</h2>
          <p class="sec-sub">每一个产品都解决真实问题,服务真实用户</p>
        </header>

        <div class="works-columns reveal">
          <article
            v-for="w in rest"
            :key="w.id"
            class="work-card"
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

      <!-- 技术栈（多栏报纸式排版） -->
      <section id="skills" class="g-section g-section-soft">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">栏目贰 · SECTION 02</span>
          <h2 class="sec-title">工具与技术</h2>
          <p class="sec-sub">从前端到 AI,具备全链路独立开发能力</p>
        </header>

        <div class="skills-columns reveal">
          <article v-for="g in data.skills" :key="g.id" class="skill-card">
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

      <!-- 关于我（首字下沉正文,报纸专栏式） -->
      <section id="about" class="g-section">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">栏目叁 · SECTION 03</span>
          <h2 class="sec-title">作者自述</h2>
        </header>
        <div class="about-grid">
          <div class="about-text reveal">
            <p class="about-lead">{{ data.profile.bio }}</p>
            <p class="about-extra">
              {{ data.profile.availability }} · {{ data.profile.location }}
            </p>
          </div>
          <div class="about-cards reveal">
            <div v-for="(s, i) in stats" :key="s.label" class="about-card" :class="{ 'about-card-red': i === 1 }">
              <span class="about-value">{{ s.value }}</span>
              <span class="about-label">{{ s.label }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 创作历程（报纸式编年史） -->
      <section id="timeline" class="g-section g-section-soft">
        <header class="sec-head reveal">
          <span class="sec-eyebrow">栏目肆 · SECTION 04</span>
          <h2 class="sec-title">编年史</h2>
        </header>
        <ol class="timeline">
          <li v-for="(e, i) in data.experiences" :key="e.id" class="tl-item reveal" :style="{ '--i': i }">
            <div class="tl-meta">
              <span class="tl-index">{{ String(i + 1).padStart(2, '0') }}</span>
              <span class="tl-period">{{ e.period }}</span>
            </div>
            <div class="tl-content">
              <h3 class="tl-title">{{ e.title }}</h3>
              <span class="tl-org">{{ e.organization }}</span>
              <p class="tl-desc">{{ e.description }}</p>
            </div>
          </li>
        </ol>
      </section>

      <!-- 联系方式（报社启事式） -->
      <section id="contact" class="g-section">
        <div class="contact-card reveal">
          <div class="contact-left">
            <span class="sec-eyebrow">栏目伍 · SECTION 05</span>
            <h2 class="sec-title">联系启事</h2>
            <p class="contact-sub">
              无论是合作机会、产品咨询,还是只是想打个招呼,我都很乐意聊聊。
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
              发邮件给我 <ArrowUpRight :size="17" :stroke-width="2" />
            </a>
            <a
              v-for="s in data.profile.socials"
              :key="s.name"
              class="social-link"
              :href="s.url"
              target="_blank"
              rel="noopener noreferrer"
            >
              {{ s.name }} <ArrowUpRight :size="13" :stroke-width="2" />
            </a>
          </div>
        </div>
      </section>
    </main>

    <!-- 页脚（报纸版权式） -->
    <footer class="g-footer">
      <div class="footer-rule"></div>
      <div class="footer-inner">
        <span class="footer-brand">{{ initials }} · {{ data.profile.name }} GAZETTE</span>
        <span class="footer-copy">© {{ year }} {{ data.profile.name }}. 基于 WorksShow 印刷发行。</span>
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
      class="g-modal"
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
            查看项目 <ArrowUpRight :size="15" :stroke-width="2" />
          </a>
        </div>
      </article>
    </div>
  </div>
</template>

<style scoped>
.g-page {
  /* 报章色彩系统：做旧纸张 + 纯黑 + 暗红刊名 */
  --g-bg: #f4f0e6;
  --g-bg-soft: #ebe6d8;
  --g-card: #faf7ef;
  --g-ink: #1a1a1a;
  --g-ink-soft: #4a4a4a;
  --g-ink-mute: #8a8a8a;
  --g-red: #9b1c1c;
  --g-line: #1a1a1a;
  --g-line-soft: #c4bda8;
  background: var(--g-bg);
  color: var(--g-ink);
  font-family: 'Noto Serif SC', Georgia, 'Times New Roman', serif;
  font-size: 16px;
  line-height: 1.8;
  min-height: 100vh;
  -webkit-font-smoothing: antialiased;
}

.g-page * {
  box-sizing: border-box;
}

/* —— 报头 Masthead —— */
.g-masthead {
  padding: clamp(28px, 4vw, 48px) clamp(20px, 5vw, 64px) 0;
  text-align: center;
  background: var(--g-bg);
}
.masthead-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: clamp(12px, 2vw, 20px);
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 12px;
  color: var(--g-ink-soft);
  text-transform: uppercase;
  letter-spacing: 0.1em;
}
.masthead-title {
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-size: clamp(32px, 6vw, 64px);
  font-weight: 900;
  letter-spacing: -0.02em;
  line-height: 1;
  margin: 0 0 10px;
  color: var(--g-ink);
}
.masthead-tagline {
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-style: italic;
  font-size: clamp(13px, 1.6vw, 16px);
  color: var(--g-ink-soft);
  margin: 0 0 clamp(14px, 2vw, 22px);
}
.masthead-rule {
  height: 3px;
  background: var(--g-ink);
}
.masthead-rule-thin {
  height: 1px;
  margin-top: 3px;
}

/* —— 导航栏 —— */
.g-navbar {
  position: sticky;
  top: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 14px clamp(20px, 5vw, 64px);
  background: var(--g-bg);
  border-bottom: 1px solid var(--g-ink);
  transition: box-shadow 0.3s ease, background 0.3s ease;
}
.g-navbar.is-scrolled {
  background: rgba(244, 240, 230, 0.95);
  box-shadow: 0 4px 20px -12px rgba(0, 0, 0, 0.2);
}
.g-brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: var(--g-ink);
}
.brand-mark {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  font-family: 'Playfair Display', serif;
  font-weight: 900;
  font-size: 18px;
  color: var(--g-bg);
  background: var(--g-ink);
  border: 1px solid var(--g-ink);
}
.brand-name {
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-weight: 700;
  letter-spacing: 0.5px;
}
.nav-links {
  display: flex;
  gap: 28px;
}
.nav-links a {
  font-family: 'Noto Serif SC', serif;
  font-size: 15px;
  color: var(--g-ink-soft);
  text-decoration: none;
  transition: color 0.2s ease;
  position: relative;
}
.nav-links a::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -4px;
  width: 0;
  height: 1px;
  background: var(--g-red);
  transition: width 0.2s ease;
}
.nav-links a:hover {
  color: var(--g-red);
}
.nav-links a:hover::after {
  width: 100%;
}
.nav-cta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-size: 14px;
  font-weight: 700;
  color: var(--g-bg);
  background: var(--g-ink);
  border: 1px solid var(--g-ink);
  text-decoration: none;
  transition: background 0.2s ease, color 0.2s ease;
}
.nav-cta:hover {
  background: var(--g-red);
  border-color: var(--g-red);
}

/* —— 通用按钮 —— */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 22px;
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-size: 15px;
  font-weight: 700;
  text-decoration: none;
  border: 1px solid var(--g-ink);
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease, transform 0.2s ease;
}
.btn-primary {
  color: var(--g-bg);
  background: var(--g-ink);
}
.btn-primary:hover {
  background: var(--g-red);
  border-color: var(--g-red);
  transform: translateY(-1px);
}
.btn-ghost {
  color: var(--g-ink);
  background: transparent;
}
.btn-ghost:hover {
  background: var(--g-ink);
  color: var(--g-bg);
}
.btn-lg {
  padding: 15px 28px;
  font-size: 16px;
}

/* —— 头版头条 Hero —— */
.g-hero {
  padding: clamp(40px, 6vw, 72px) clamp(20px, 5vw, 64px);
  border-bottom: 1px solid var(--g-ink);
}
.hero-inner {
  max-width: 1180px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: clamp(32px, 5vw, 64px);
  align-items: start;
}
.eyebrow,
.feature-eyebrow {
  display: inline-block;
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 11px;
  font-weight: 700;
  color: var(--g-red);
  text-transform: uppercase;
  letter-spacing: 0.16em;
  margin-bottom: 18px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--g-red);
}
.hero-title {
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-size: clamp(36px, 5.5vw, 64px);
  font-weight: 900;
  line-height: 1.05;
  letter-spacing: -0.02em;
  margin: 0 0 20px;
  color: var(--g-ink);
}
.hero-title em {
  font-style: italic;
  color: var(--g-red);
  font-weight: 700;
}
.hero-deck {
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-style: italic;
  font-size: clamp(17px, 2vw, 21px);
  color: var(--g-ink-soft);
  margin: 0 0 20px;
  line-height: 1.5;
}
.hero-body {
  border-left: 3px solid var(--g-ink);
  padding-left: 18px;
  margin-bottom: 28px;
}
.hero-bio {
  font-size: 15px;
  color: var(--g-ink-soft);
  line-height: 1.85;
  margin: 0;
  max-width: 520px;
}
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-bottom: 32px;
}
.hero-stats {
  display: flex;
  gap: 32px;
  border-top: 1px solid var(--g-line-soft);
  padding-top: 20px;
}
.stat-value {
  display: block;
  font-family: 'Playfair Display', serif;
  font-size: 32px;
  font-weight: 900;
  color: var(--g-ink);
  letter-spacing: -0.02em;
  line-height: 1;
  margin-bottom: 4px;
}
.stat-label {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 11px;
  color: var(--g-ink-mute);
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

/* Hero 右侧特色卡 */
.feature-card {
  background: var(--g-card);
  border: 1px solid var(--g-ink);
  cursor: pointer;
  transition: box-shadow 0.3s ease;
}
.feature-card:hover {
  box-shadow: 6px 6px 0 var(--g-ink);
}
.feature-cover {
  position: relative;
  aspect-ratio: 16 / 10;
  overflow: hidden;
  background: var(--g-bg-soft);
  border-bottom: 1px solid var(--g-ink);
}
.feature-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: grayscale(0.15) contrast(1.05);
}
.feature-body {
  padding: 22px 24px 26px;
}
.feature-cat {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 11px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--g-red);
  font-weight: 700;
}
.feature-title {
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-size: 24px;
  font-weight: 700;
  margin: 8px 0 10px;
  color: var(--g-ink);
  line-height: 1.2;
}
.feature-desc {
  font-size: 14px;
  color: var(--g-ink-soft);
  line-height: 1.7;
  margin: 0 0 14px;
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
  margin-bottom: 14px;
}
.feature-cta {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-size: 14px;
  font-weight: 700;
  color: var(--g-red);
  border-bottom: 1px solid var(--g-red);
  padding-bottom: 2px;
}

.chip {
  padding: 3px 10px;
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 11px;
  color: var(--g-ink-soft);
  background: var(--g-bg-soft);
  border: 1px solid var(--g-line-soft);
  letter-spacing: 0.04em;
}

/* —— 通用 section —— */
.g-section {
  padding: clamp(56px, 7vw, 96px) clamp(20px, 5vw, 64px);
  border-bottom: 1px solid var(--g-ink);
}
.g-section-soft {
  background: var(--g-bg-soft);
}
.sec-head {
  max-width: 760px;
  margin: 0 auto clamp(36px, 5vw, 56px);
  text-align: center;
}
.sec-eyebrow {
  display: inline-block;
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 12px;
  font-weight: 700;
  color: var(--g-red);
  margin-bottom: 14px;
  text-transform: uppercase;
  letter-spacing: 0.16em;
}
.sec-title {
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-size: clamp(30px, 4.2vw, 48px);
  font-weight: 900;
  letter-spacing: -0.02em;
  margin: 0 0 12px;
  color: var(--g-ink);
  line-height: 1.05;
}
.sec-sub {
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-style: italic;
  font-size: 16px;
  color: var(--g-ink-soft);
  margin: 0;
}

/* —— 作品多栏矩阵 —— */
.works-columns {
  max-width: 1180px;
  margin: 0 auto;
  column-count: 2;
  column-gap: 0;
  border: 1px solid var(--g-ink);
  border-top: none;
}
.work-card {
  break-inside: avoid;
  background: var(--g-card);
  border-top: 1px solid var(--g-ink);
  cursor: pointer;
  transition: background 0.2s ease;
}
.work-card:nth-child(odd) {
  border-right: 1px solid var(--g-ink);
}
.work-card:hover {
  background: var(--g-bg-soft);
}
.work-cover {
  position: relative;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background: var(--g-bg);
  border-bottom: 1px solid var(--g-ink);
}
.work-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: grayscale(0.15) contrast(1.05);
  transition: transform 0.4s ease;
}
.work-card:hover .work-cover img {
  transform: scale(1.03);
}
.work-year {
  position: absolute;
  top: 0;
  right: 0;
  padding: 5px 11px;
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 11px;
  font-weight: 700;
  color: var(--g-bg);
  background: var(--g-ink);
  letter-spacing: 0.08em;
}
.work-body {
  padding: 20px 22px 24px;
}
.work-cat {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 11px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--g-red);
  font-weight: 700;
}
.work-title {
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-size: 20px;
  font-weight: 700;
  margin: 6px 0 8px;
  color: var(--g-ink);
  line-height: 1.25;
}
.work-desc {
  font-size: 14px;
  color: var(--g-ink-soft);
  line-height: 1.65;
  margin: 0 0 12px;
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

/* —— 技术栈多栏 —— */
.skills-columns {
  max-width: 1180px;
  margin: 0 auto;
  column-count: 2;
  column-gap: 0;
  border: 1px solid var(--g-ink);
  border-top: none;
}
.skill-card {
  break-inside: avoid;
  background: var(--g-card);
  border-top: 1px solid var(--g-ink);
  padding: 26px 28px 24px;
}
.skill-card:nth-child(odd) {
  border-right: 1px solid var(--g-ink);
}
.skill-head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding-bottom: 14px;
  margin-bottom: 14px;
  border-bottom: 1px solid var(--g-line-soft);
}
.skill-cn {
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-size: 19px;
  font-weight: 700;
  color: var(--g-ink);
}
.skill-en {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 11px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--g-ink-mute);
}
.skill-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}
.skill-list li {
  padding: 5px 11px;
  font-family: 'Noto Serif SC', serif;
  font-size: 13px;
  color: var(--g-ink);
  background: var(--g-bg);
  border: 1px solid var(--g-line-soft);
}

/* —— 关于我（首字下沉） —— */
.about-grid {
  max-width: 1080px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: clamp(32px, 5vw, 56px);
  align-items: center;
}
.about-lead {
  font-size: 17px;
  color: var(--g-ink);
  line-height: 1.95;
  margin: 0 0 18px;
  /* 首字下沉：报纸专栏标志 */
}
.about-lead::first-letter {
  font-family: 'Playfair Display', serif;
  font-size: 56px;
  font-weight: 900;
  float: left;
  line-height: 0.9;
  margin: 6px 10px 0 0;
  color: var(--g-red);
}
.about-extra {
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-size: 15px !important;
  color: var(--g-ink-soft) !important;
  font-style: italic;
  margin: 0;
}
.about-cards {
  display: flex;
  flex-direction: column;
  gap: 0;
  border: 1px solid var(--g-ink);
}
.about-card {
  padding: 18px 22px;
  border-bottom: 1px solid var(--g-ink);
  background: var(--g-card);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.about-card:last-child {
  border-bottom: none;
}
.about-card-red {
  background: var(--g-ink);
}
.about-card-red .about-value,
.about-card-red .about-label {
  color: var(--g-bg);
}
.about-value {
  font-family: 'Playfair Display', serif;
  font-size: 30px;
  font-weight: 900;
  color: var(--g-ink);
  letter-spacing: -0.02em;
  line-height: 1;
}
.about-label {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 11px;
  color: var(--g-ink-soft);
  text-transform: uppercase;
  letter-spacing: 0.1em;
  text-align: right;
}

/* —— 编年史时间线 —— */
.timeline {
  max-width: 860px;
  margin: 0 auto;
  list-style: none;
  padding: 0;
}
.tl-item {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 28px;
  padding-bottom: 32px;
  border-bottom: 1px solid var(--g-line-soft);
  margin-bottom: 32px;
}
.tl-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}
.tl-meta {
  text-align: right;
  border-right: 1px solid var(--g-ink);
  padding-right: 20px;
}
.tl-index {
  display: block;
  font-family: 'Playfair Display', serif;
  font-size: 36px;
  font-weight: 900;
  color: var(--g-red);
  line-height: 1;
  margin-bottom: 6px;
}
.tl-period {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 12px;
  font-weight: 700;
  color: var(--g-ink-soft);
  letter-spacing: 0.08em;
}
.tl-content {
  padding-top: 4px;
}
.tl-title {
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 4px;
  color: var(--g-ink);
  line-height: 1.25;
}
.tl-org {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 12px;
  color: var(--g-red);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.1em;
}
.tl-desc {
  font-size: 14px;
  color: var(--g-ink-soft);
  line-height: 1.75;
  margin: 10px 0 0;
}

/* —— 联系启事 —— */
.contact-card {
  max-width: 1080px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: clamp(32px, 5vw, 56px);
  align-items: center;
  background: var(--g-card);
  border: 1px solid var(--g-ink);
  padding: clamp(32px, 5vw, 52px);
  box-shadow: 6px 6px 0 var(--g-ink);
}
.contact-card .sec-eyebrow {
  margin-bottom: 8px;
}
.contact-card .sec-title {
  margin-bottom: 14px;
}
.contact-sub {
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-style: italic;
  font-size: 16px;
  color: var(--g-ink-soft);
  line-height: 1.7;
  margin: 0 0 22px;
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
  color: var(--g-ink);
}
.contact-list li :deep(svg) {
  color: var(--g-red);
  flex-shrink: 0;
}
.contact-list a {
  color: var(--g-ink);
  text-decoration: none;
  border-bottom: 1px solid var(--g-ink);
  transition: color 0.2s ease, border-color 0.2s ease;
}
.contact-list a:hover {
  color: var(--g-red);
  border-bottom-color: var(--g-red);
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
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-size: 14px;
  font-weight: 700;
  color: var(--g-ink);
  text-decoration: none;
  border-bottom: 1px solid var(--g-ink);
  padding-bottom: 2px;
  transition: color 0.2s ease, border-color 0.2s ease;
}
.social-link:hover {
  color: var(--g-red);
  border-bottom-color: var(--g-red);
}

/* —— 页脚 —— */
.g-footer {
  padding: 24px clamp(20px, 5vw, 64px) 32px;
  background: var(--g-bg);
}
.footer-rule {
  height: 1px;
  background: var(--g-ink);
  margin-bottom: 20px;
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
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-weight: 700;
  color: var(--g-ink);
  letter-spacing: 0.04em;
}
.footer-copy {
  font-family: 'Noto Serif SC', serif;
  font-style: italic;
  font-size: 13px;
  color: var(--g-ink-mute);
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
  color: var(--g-bg);
  background: var(--g-ink);
  border: 1px solid var(--g-ink);
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
}
.back-top:hover {
  background: var(--g-red);
  border-color: var(--g-red);
  transform: translateY(-2px);
}

/* —— 作品弹窗 —— */
.g-modal {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: none;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.g-modal.is-open {
  display: flex;
}
.modal-overlay {
  position: absolute;
  inset: 0;
  background: rgba(26, 26, 26, 0.6);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}
.modal-card {
  position: relative;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  max-width: 920px;
  width: 100%;
  max-height: 88vh;
  background: var(--g-card);
  border: 1px solid var(--g-ink);
  box-shadow: 8px 8px 0 var(--g-ink);
  overflow: hidden;
}
.modal-close {
  position: absolute;
  top: 0;
  right: 0;
  z-index: 2;
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  color: var(--g-bg);
  background: var(--g-ink);
  border: 1px solid var(--g-ink);
  border-top: none;
  border-right: none;
  cursor: pointer;
  transition: background 0.2s ease;
}
.modal-close:hover {
  background: var(--g-red);
}
.modal-cover {
  position: relative;
  overflow: hidden;
  min-height: 300px;
  background: var(--g-bg-soft);
  border-right: 1px solid var(--g-ink);
}
.modal-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: grayscale(0.15) contrast(1.05);
}
.modal-year {
  position: absolute;
  bottom: 14px;
  left: 14px;
  padding: 5px 11px;
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 11px;
  font-weight: 700;
  color: var(--g-bg);
  background: var(--g-ink);
  letter-spacing: 0.08em;
}
.modal-body {
  padding: 32px 34px 30px;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.modal-cat {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 11px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--g-red);
  font-weight: 700;
  margin-bottom: 10px;
}
.modal-title {
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-size: 28px;
  font-weight: 700;
  color: var(--g-ink);
  margin: 0 0 14px;
  line-height: 1.2;
}
.modal-desc {
  font-size: 15px;
  color: var(--g-ink-soft);
  line-height: 1.85;
  margin: 0 0 22px;
}
.modal-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-bottom: 24px;
}
.modal-link {
  align-self: flex-start;
  margin-top: auto;
}

/* —— 渐入动画（报纸风:淡入上移,衬线阅读节奏） —— */
.reveal {
  opacity: 0;
  transform: translateY(18px);
  transition: opacity 0.6s ease, transform 0.6s ease;
  transition-delay: calc(var(--i, 0) * 70ms);
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
  .works-columns,
  .skills-columns {
    column-count: 1;
  }
  .work-card,
  .skill-card {
    border-right: none !important;
  }
  .nav-links {
    display: none;
  }
  .hero-stats {
    gap: 24px;
  }
  .tl-item {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  .tl-meta {
    text-align: left;
    border-right: none;
    border-bottom: 1px solid var(--g-ink);
    padding-right: 0;
    padding-bottom: 12px;
    display: flex;
    align-items: baseline;
    gap: 14px;
  }
  .tl-index {
    margin-bottom: 0;
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
    border-bottom: 1px solid var(--g-ink);
  }
  .modal-body {
    padding: 24px;
  }
  .masthead-top {
    flex-direction: column;
    gap: 4px;
  }
  .hero-stats {
    flex-wrap: wrap;
  }
}
</style>

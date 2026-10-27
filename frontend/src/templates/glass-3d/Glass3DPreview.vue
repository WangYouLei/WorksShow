<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted } from 'vue'
import { safeUrl } from '@/utils/safeUrl'
import {
  X,
  ArrowUpRight,
  ArrowUp,
  Mail,
  MapPin,
  Clock,
  Sparkles,
  Zap,
  Palette,
  Github,
} from 'lucide-vue-next'
import type { PortfolioData } from '../types'

const props = defineProps<{ data: PortfolioData }>()

const initials = computed(() => props.data.profile.name.slice(0, 1))
const stats = computed(() => props.data.profile.stats)
const featured = computed(() => props.data.works.find((w) => w.highlight) ?? props.data.works[0])
const rest = computed(() => props.data.works.filter((w) => w.id !== featured.value?.id))

/* —— 3D 交互（Vue 预览环境生效；导出 HTML 由 runtime.ts 还原同样逻辑）—— */
const cleanups: Array<() => void> = []

function motionAllowed(): boolean {
  return !window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

function finePointer(): boolean {
  return window.matchMedia('(pointer: fine)').matches
}

function bindNavbar() {
  const nav = document.querySelector('.g3-nav') as HTMLElement | null
  if (!nav) return
  const onScroll = () => nav.classList.toggle('is-scrolled', window.scrollY > 30)
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
  cleanups.push(() => window.removeEventListener('scroll', onScroll))
}

/** Hero 3D 场景鼠标视差：整个场景随鼠标轻微旋转（rAF + lerp 平滑） */
function bindSceneParallax() {
  const stage = document.querySelector('.g3-stage') as HTMLElement | null
  const scene = document.querySelector('.g3-scene') as HTMLElement | null
  if (!stage || !scene || !finePointer() || !motionAllowed()) return
  let raf: number | null = null
  let tx = 0
  let ty = 0
  let cx = 0
  let cy = 0
  const tick = () => {
    cx += (tx - cx) * 0.08
    cy += (ty - cy) * 0.08
    scene.style.transform = `rotateX(${(10 - cy * 8).toFixed(2)}deg) rotateY(${(-16 + cx * 12).toFixed(2)}deg)`
    if (Math.abs(tx - cx) > 0.002 || Math.abs(ty - cy) > 0.002) raf = requestAnimationFrame(tick)
    else raf = null
  }
  const wake = () => {
    if (raf === null) raf = requestAnimationFrame(tick)
  }
  const onMove = (e: MouseEvent) => {
    const r = stage.getBoundingClientRect()
    tx = ((e.clientX - r.left) / Math.max(r.width, 1) - 0.5) * 2
    ty = ((e.clientY - r.top) / Math.max(r.height, 1) - 0.5) * 2
    wake()
  }
  const onLeave = () => {
    tx = 0
    ty = 0
    wake()
  }
  stage.addEventListener('mousemove', onMove)
  stage.addEventListener('mouseleave', onLeave)
  cleanups.push(() => {
    stage.removeEventListener('mousemove', onMove)
    stage.removeEventListener('mouseleave', onLeave)
    if (raf !== null) cancelAnimationFrame(raf)
  })
}

/** 卡片 3D 倾斜跟随：事件委托 + rAF 时间归一化插值（悬停快速跟随、离开缓慢回弹） */
function bindTilt() {
  if (!finePointer() || !motionAllowed()) return
  interface TiltState {
    el: HTMLElement
    rx: number
    ry: number
    lift: number
    gx: number
    gy: number
    tRx: number
    tRy: number
    tLift: number
    tGx: number
    tGy: number
    hover: boolean
  }
  const active: TiltState[] = []
  let raf: number | null = null
  let last = 0

  const frame = (now: number) => {
    const dt = Math.min((now - last) / 1000, 0.05)
    last = now
    const follow = 1 - Math.exp(-dt * 16)
    const back = 1 - Math.exp(-dt * 10)
    const glare = 1 - Math.exp(-dt * 18)
    for (let i = active.length - 1; i >= 0; i--) {
      const s = active[i]
      // 元素已从 DOM 移除（如编辑数据触发重渲染）：直接丢弃，避免僵尸状态空转
      if (!s.el.isConnected) {
        active.splice(i, 1)
        continue
      }
      const e = s.hover ? follow : back
      s.rx += (s.tRx - s.rx) * e
      s.ry += (s.tRy - s.ry) * e
      s.lift += (s.tLift - s.lift) * e
      s.gx += (s.tGx - s.gx) * glare
      s.gy += (s.tGy - s.gy) * glare
      const settled =
        !s.hover &&
        Math.abs(s.tRx - s.rx) < 0.05 &&
        Math.abs(s.tRy - s.ry) < 0.05 &&
        Math.abs(s.tLift - s.lift) < 0.1 &&
        Math.abs(s.tGx - s.gx) < 0.5
      if (settled) {
        s.el.style.transform = ''
        s.el.style.removeProperty('--gx')
        s.el.style.removeProperty('--gy')
        active.splice(i, 1)
      } else {
        s.el.style.transform = `perspective(900px) rotateX(${s.rx.toFixed(2)}deg) rotateY(${s.ry.toFixed(2)}deg) translateY(${s.lift.toFixed(2)}px)`
        s.el.style.setProperty('--gx', `${s.gx.toFixed(1)}%`)
        s.el.style.setProperty('--gy', `${s.gy.toFixed(1)}%`)
      }
    }
    raf = active.length ? requestAnimationFrame(frame) : null
  }

  const wake = () => {
    if (raf === null) {
      last = performance.now()
      raf = requestAnimationFrame(frame)
    }
  }

  const onMove = (e: MouseEvent) => {
    const card = (e.target as HTMLElement | null)?.closest('[data-tilt]') as HTMLElement | null
    // 兜底：指针已不在卡片上时让所有活跃卡片进入回弹（不依赖 mouseout，事件丢失也能归位）
    for (const s of active) {
      if (s.el !== card && !s.el.contains(e.target as Node)) {
        s.hover = false
        s.tRx = 0
        s.tRy = 0
        s.tLift = 0
        s.tGx = 50
        s.tGy = 50
      }
    }
    if (!card) {
      if (active.length) wake()
      return
    }
    const r = card.getBoundingClientRect()
    const px = (e.clientX - r.left) / Math.max(r.width, 1)
    const py = (e.clientY - r.top) / Math.max(r.height, 1)
    let s = active.find((it) => it.el === card)
    if (!s) {
      s = { el: card, rx: 0, ry: 0, lift: 0, gx: 50, gy: 50, tRx: 0, tRy: 0, tLift: 0, tGx: 50, tGy: 50, hover: true }
      active.push(s)
    }
    s.hover = true
    s.tRx = (0.5 - py) * 9
    s.tRy = (px - 0.5) * 11
    s.tLift = -6
    s.tGx = px * 100
    s.tGy = py * 100
    wake()
  }

  const onOut = (e: MouseEvent) => {
    const card = (e.target as HTMLElement | null)?.closest('[data-tilt]') as HTMLElement | null
    if (!card || card.contains(e.relatedTarget as Node | null)) return
    const s = active.find((it) => it.el === card)
    if (!s) return
    s.hover = false
    s.tRx = 0
    s.tRy = 0
    s.tLift = 0
    s.tGx = 50
    s.tGy = 50
    wake()
  }

  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseout', onOut)
  cleanups.push(() => {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseout', onOut)
    if (raf !== null) cancelAnimationFrame(raf)
    active.length = 0
  })
}

onMounted(() => {
  bindNavbar()
  bindSceneParallax()
  bindTilt()
})

onBeforeUnmount(() => {
  cleanups.splice(0).forEach((fn) => fn())
})
</script>

<template>
  <div class="g3-page">
    <!-- 背景：粉彩渐变天空 + 漂浮光斑 -->
    <div class="g3-bg" aria-hidden="true">
      <span class="g3-orb g3-orb-1"></span>
      <span class="g3-orb g3-orb-2"></span>
      <span class="g3-orb g3-orb-3"></span>
      <span class="g3-dots"></span>
    </div>

    <!-- 悬浮玻璃导航 -->
    <nav class="g3-nav">
      <a class="g3-brand" href="#top">
        <span class="g3-brand-mark">{{ initials }}</span>
        <span class="g3-brand-name">{{ data.profile.name }}</span>
      </a>
      <div class="g3-nav-links">
        <a href="#works">作品</a>
        <a href="#skills">技能</a>
        <a href="#about">关于</a>
        <a href="#timeline">历程</a>
        <a href="#contact">联系</a>
      </div>
      <a class="g3-nav-cta" href="#contact">
        <span>联系我</span>
        <ArrowUpRight :size="14" :stroke-width="2" />
      </a>
    </nav>

    <main id="top">
      <!-- Hero -->
      <section class="g3-hero">
        <div class="g3-hero-inner">
          <div class="g3-hero-left reveal">
            <span class="g3-eyebrow">
              <i class="g3-eyebrow-dot"></i>
              {{ data.profile.title }}
            </span>
            <h1 class="g3-title">
              你好，我是{{ data.profile.name }}<br />
              <em>让创意悬浮于光影之间</em>
            </h1>
            <p class="g3-sub">{{ data.profile.tagline }}</p>
            <div class="g3-actions">
              <a class="g3-btn g3-btn-primary" href="#works">
                查看作品 <ArrowUpRight :size="16" :stroke-width="2" />
              </a>
              <a class="g3-btn g3-btn-ghost" href="#contact">联系我</a>
            </div>
            <div class="g3-stats">
              <div v-for="s in stats" :key="s.label" class="g3-stat">
                <strong>{{ s.value }}</strong>
                <span>{{ s.label }}</span>
              </div>
            </div>
          </div>

          <!-- 右侧真 3D 悬浮场景：旋转玻璃立方 + 漂浮名片 + 玻璃徽章 -->
          <div class="g3-stage reveal" style="--i: 1">
            <div class="g3-scene">
              <div class="g3-cube" aria-hidden="true">
                <i class="g3-cf g3-cf-1"></i>
                <i class="g3-cf g3-cf-2"></i>
                <i class="g3-cf g3-cf-3"></i>
                <i class="g3-cf g3-cf-4"></i>
                <i class="g3-cf g3-cf-5"></i>
                <i class="g3-cf g3-cf-6"></i>
              </div>
              <span class="g3-sphere g3-sphere-a" aria-hidden="true"></span>
              <span class="g3-sphere g3-sphere-b" aria-hidden="true"></span>
              <div class="g3-idcard">
                <span class="g3-avatar">{{ initials }}</span>
                <div class="g3-idcard-text">
                  <strong>{{ data.profile.name }}</strong>
                  <span>{{ data.profile.title }}</span>
                </div>
                <span class="g3-id-status"><i></i>{{ data.profile.availability || '随时可用' }}</span>
              </div>
              <span class="g3-fchip g3-fchip-1">
                <Sparkles :size="13" :stroke-width="2" />创意驱动
              </span>
              <span class="g3-fchip g3-fchip-2">
                <Zap :size="13" :stroke-width="2" />极致性能
              </span>
              <span class="g3-fchip g3-fchip-3">
                <Palette :size="13" :stroke-width="2" />视觉打磨
              </span>
            </div>
          </div>
        </div>
      </section>

      <!-- 作品 -->
      <section id="works" class="g3-section">
        <header class="g3-sec-head reveal">
          <span class="g3-sec-eyebrow">精选作品</span>
          <h2 class="g3-sec-title">每一件作品<br />都值得被光点亮</h2>
          <p class="g3-sec-sub">从产品构想到交互细节，用工程与设计把每个想法稳稳落地</p>
        </header>

        <div class="g3-works">
          <!-- 旗舰作品：横向大卡 -->
          <div v-if="featured" class="g3-work-wrap g3-work-wrap-featured reveal">
            <article
              class="g3-card g3-work g3-work-featured"
              :data-work-id="featured.id"
              data-tilt="1"
              role="button"
              tabindex="0"
              :aria-label="featured.title"
            >
              <div class="g3-work-cover g3-work-cover-featured">
                <img :src="featured.cover" :alt="featured.title" loading="lazy" />
                <span class="g3-work-cat">{{ featured.category }}</span>
                <span class="g3-work-year">{{ featured.year }}</span>
              </div>
              <div class="g3-work-body">
                <h3 class="g3-work-title">{{ featured.title }}</h3>
                <p class="g3-work-desc">{{ featured.description }}</p>
                <div class="g3-work-tags">
                  <span v-for="t in featured.tags.slice(0, 4)" :key="t" class="g3-mini-chip">{{ t }}</span>
                </div>
                <span class="g3-work-cta">
                  查看详情 <ArrowUpRight :size="15" :stroke-width="2" />
                </span>
              </div>
              <span class="g3-glare" aria-hidden="true"></span>
            </article>
          </div>

          <!-- 其余作品 -->
          <div
            v-for="(w, i) in rest"
            :key="w.id"
            class="g3-work-wrap reveal"
            :style="{ '--i': (i % 3) + 1 }"
          >
            <article
              class="g3-card g3-work"
              :data-work-id="w.id"
              data-tilt="1"
              role="button"
              tabindex="0"
              :aria-label="w.title"
            >
              <div class="g3-work-cover">
                <img :src="w.cover" :alt="w.title" loading="lazy" />
                <span class="g3-work-cat">{{ w.category }}</span>
                <span class="g3-work-year">{{ w.year }}</span>
              </div>
              <div class="g3-work-body">
                <h3 class="g3-work-title">{{ w.title }}</h3>
                <p class="g3-work-desc">{{ w.description }}</p>
                <div class="g3-work-tags">
                  <span v-for="t in w.tags.slice(0, 3)" :key="t" class="g3-mini-chip">{{ t }}</span>
                </div>
                <span class="g3-work-cta">
                  查看详情 <ArrowUpRight :size="14" :stroke-width="2" />
                </span>
              </div>
              <span class="g3-glare" aria-hidden="true"></span>
            </article>
          </div>
        </div>
      </section>

      <!-- 技能 -->
      <section id="skills" class="g3-section g3-section-tint">
        <header class="g3-sec-head reveal">
          <span class="g3-sec-eyebrow">能力矩阵</span>
          <h2 class="g3-sec-title">覆盖全链路的技术版图</h2>
          <p class="g3-sec-sub">前端、后端、AI 与云服务，独立完成从设计到上线的完整闭环</p>
        </header>

        <div class="g3-skills">
          <article
            v-for="(g, i) in data.skills"
            :key="g.id"
            class="g3-card g3-skill reveal"
            :style="{ '--i': i }"
          >
            <div class="g3-skill-head">
              <span class="g3-skill-cn">{{ g.category }}</span>
              <span class="g3-skill-en">{{ g.categoryEn }}</span>
            </div>
            <ul class="g3-skill-list">
              <li v-for="item in g.items" :key="item">{{ item }}</li>
            </ul>
          </article>
        </div>
      </section>

      <!-- 关于我 -->
      <section id="about" class="g3-section">
        <header class="g3-sec-head reveal">
          <span class="g3-sec-eyebrow">关于我</span>
          <h2 class="g3-sec-title">一个把细节当作品的人</h2>
        </header>
        <div class="g3-about">
          <div class="g3-about-text reveal">
            <p class="g3-about-lead">{{ data.profile.bio }}</p>
            <p class="g3-about-meta">
              <MapPin :size="15" :stroke-width="1.75" />
              <span>{{ data.profile.location }}</span>
              <i class="g3-dot-sep">·</i>
              <Clock :size="15" :stroke-width="1.75" />
              <span>{{ data.profile.availability }}</span>
            </p>
          </div>
          <div class="g3-about-cards">
            <div
              v-for="(s, i) in stats"
              :key="s.label"
              class="g3-card g3-statcard reveal"
              :style="{ '--i': i }"
            >
              <strong>{{ s.value }}</strong>
              <span>{{ s.label }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 历程 -->
      <section id="timeline" class="g3-section g3-section-tint">
        <header class="g3-sec-head reveal">
          <span class="g3-sec-eyebrow">成长轨迹</span>
          <h2 class="g3-sec-title">每一步<br />都留下光的印记</h2>
        </header>

        <ol class="g3-timeline">
          <li
            v-for="(e, i) in data.experiences"
            :key="e.id"
            class="g3-tl reveal"
            :style="{ '--i': i }"
          >
            <span class="g3-tl-dot"></span>
            <div class="g3-card g3-tl-card">
              <span class="g3-tl-period">{{ e.period }}</span>
              <h3 class="g3-tl-title">{{ e.title }}</h3>
              <span class="g3-tl-org">{{ e.organization }}</span>
              <p class="g3-tl-desc">{{ e.description }}</p>
            </div>
          </li>
        </ol>
      </section>

      <!-- 联系 -->
      <section id="contact" class="g3-section">
        <div class="g3-card g3-contact reveal">
          <div class="g3-contact-left">
            <span class="g3-sec-eyebrow">联系方式</span>
            <h2 class="g3-contact-title">有新想法？<br />让它漂浮到我这里</h2>
            <p class="g3-contact-sub">
              无论是合作机会、产品咨询，还是只是想打个招呼，我都很乐意聊聊。通常 24 小时内回复。
            </p>
            <ul class="g3-contact-list">
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
          <div class="g3-contact-right">
            <a class="g3-btn g3-btn-primary g3-btn-lg" :href="'mailto:' + data.profile.email">
              发邮件给我 <ArrowUpRight :size="18" :stroke-width="2" />
            </a>
            <a
              v-for="s in data.profile.socials"
              :key="s.name"
              class="g3-social"
              :href="safeUrl(s.url)"
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

    <footer class="g3-footer">
      <span class="g3-footer-brand">{{ initials }} · {{ data.profile.name }}</span>
      <span class="g3-footer-copy">
        © {{ new Date().getFullYear() }} {{ data.profile.name }}. 基于 WorksShow 构建 · 琉璃 3D
      </span>
    </footer>

    <button class="g3-backtop" type="button" aria-label="回到顶部">
      <ArrowUp :size="18" :stroke-width="2" />
    </button>

    <!-- 作品详情弹窗 -->
    <div v-for="w in data.works" :key="w.id" class="g3-modal" :data-modal-id="w.id">
      <div class="modal-overlay"></div>
      <article class="g3-modal-card">
        <button class="modal-close" aria-label="关闭" type="button">
          <X :size="20" :stroke-width="1.75" />
        </button>
        <div class="g3-modal-cover">
          <img :src="w.cover" :alt="w.title" loading="lazy" />
          <span class="g3-modal-year">{{ w.year }}</span>
        </div>
        <div class="g3-modal-body">
          <span class="g3-modal-cat">{{ w.category }}</span>
          <h3 class="g3-modal-title">{{ w.title }}</h3>
          <p class="g3-modal-desc">{{ w.description }}</p>
          <div class="g3-modal-tags">
            <span v-for="t in w.tags" :key="t" class="g3-mini-chip">{{ t }}</span>
          </div>
          <a
            v-if="safeUrl(w.link)"
            class="g3-btn g3-btn-primary g3-modal-link"
            :href="safeUrl(w.link)"
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
/* —— 设计令牌：粉彩渐变天空 + 玻璃拟态 —— */
.g3-page {
  --g3-ink: #232a4d;
  --g3-ink-soft: #4a5378;
  --g3-ink-mute: #6d7699;
  --g3-ink-faint: #98a1c0;
  --g3-violet: #7c6cf6;
  --g3-pink: #ec5fa8;
  --g3-cyan: #1fb6d4;
  --g3-grad: linear-gradient(120deg, #7c6cf6 0%, #ec5fa8 100%);
  --g3-glass: rgba(255, 255, 255, 0.5);
  --g3-glass-strong: rgba(255, 255, 255, 0.68);
  --g3-line: rgba(255, 255, 255, 0.88);
  --g3-line-soft: rgba(255, 255, 255, 0.6);
  --g3-shadow-soft: 0 22px 55px -28px rgba(84, 76, 199, 0.35);
  --g3-shadow-lift: 0 34px 75px -26px rgba(84, 76, 199, 0.48);
  --r-md: 18px;
  --r-lg: 26px;
  --r-xl: 34px;
  background: linear-gradient(155deg, #eef0ff 0%, #fdf1f8 46%, #e9fbff 100%);
  color: var(--g3-ink);
  font-family: 'Noto Sans SC', 'Manrope', system-ui, -apple-system, sans-serif;
  font-size: 15px;
  line-height: 1.75;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  -webkit-font-smoothing: antialiased;
  letter-spacing: 0.01em;
}
.g3-page * {
  box-sizing: border-box;
}

/* —— 背景光斑 —— */
.g3-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}
.g3-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
}
.g3-orb-1 {
  width: 560px;
  height: 560px;
  top: -160px;
  left: -120px;
  background: radial-gradient(circle, rgba(124, 108, 246, 0.42), transparent 70%);
  animation: g3-drift-a 24s ease-in-out infinite;
}
.g3-orb-2 {
  width: 520px;
  height: 520px;
  top: 18%;
  right: -180px;
  background: radial-gradient(circle, rgba(236, 95, 168, 0.3), transparent 70%);
  animation: g3-drift-b 28s ease-in-out infinite;
}
.g3-orb-3 {
  width: 620px;
  height: 620px;
  bottom: -230px;
  left: 26%;
  background: radial-gradient(circle, rgba(31, 182, 212, 0.28), transparent 70%);
  animation: g3-drift-a 32s ease-in-out infinite reverse;
}
@keyframes g3-drift-a {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(60px, 70px) scale(1.08); }
}
@keyframes g3-drift-b {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-70px, 50px) scale(0.94); }
}
.g3-dots {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(35, 42, 77, 0.07) 1px, transparent 1px);
  background-size: 26px 26px;
  mask-image: radial-gradient(ellipse 75% 65% at 50% 35%, #000 30%, transparent 78%);
  -webkit-mask-image: radial-gradient(ellipse 75% 65% at 50% 35%, #000 30%, transparent 78%);
}

/* 内容层位于背景之上 */
.g3-nav,
main,
.g3-footer,
.g3-backtop,
.g3-modal {
  position: relative;
  z-index: 2;
}

/* —— 悬浮玻璃导航 —— */
.g3-nav {
  position: sticky;
  top: 14px;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin: 14px clamp(16px, 4vw, 40px) 0;
  padding: 9px 10px 9px 18px;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid var(--g3-line);
  border-radius: 999px;
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  box-shadow: 0 12px 40px -18px rgba(84, 76, 199, 0.3);
  transition: background 0.3s ease, box-shadow 0.3s ease;
}
.g3-nav.is-scrolled {
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 16px 44px -16px rgba(84, 76, 199, 0.38);
}
.g3-brand {
  display: inline-flex;
  align-items: center;
  gap: 11px;
  text-decoration: none;
  color: var(--g3-ink);
}
.g3-brand-mark {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  font-weight: 800;
  font-size: 15px;
  color: #fff;
  background: var(--g3-grad);
  border-radius: 12px;
  box-shadow: 0 10px 24px -8px rgba(124, 108, 246, 0.7);
}
.g3-brand-name {
  font-weight: 700;
  font-size: 15px;
  letter-spacing: 0.02em;
}
.g3-nav-links {
  display: flex;
  gap: 30px;
}
.g3-nav-links a {
  font-size: 14px;
  font-weight: 500;
  color: var(--g3-ink-mute);
  text-decoration: none;
  position: relative;
  transition: color 0.2s ease;
}
.g3-nav-links a::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -6px;
  height: 2px;
  border-radius: 2px;
  background: var(--g3-grad);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}
.g3-nav-links a:hover {
  color: var(--g3-ink);
}
.g3-nav-links a:hover::after {
  transform: scaleX(1);
}
.g3-nav-cta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  background: var(--g3-grad);
  border-radius: 999px;
  text-decoration: none;
  box-shadow: 0 12px 26px -10px rgba(124, 108, 246, 0.65);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.g3-nav-cta:hover {
  transform: translateY(-1px);
  box-shadow: 0 16px 32px -10px rgba(236, 95, 168, 0.6);
}

/* —— 通用按钮 —— */
.g3-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 13px 24px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  border-radius: 999px;
  border: 1px solid transparent;
  cursor: pointer;
  font-family: inherit;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease, color 0.2s ease, border-color 0.2s ease;
}
.g3-btn-primary {
  color: #fff;
  background: var(--g3-grad);
  box-shadow: 0 16px 36px -14px rgba(124, 108, 246, 0.65);
}
.g3-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 44px -14px rgba(236, 95, 168, 0.62);
}
.g3-btn-ghost {
  color: var(--g3-ink);
  background: var(--g3-glass);
  border-color: var(--g3-line);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}
.g3-btn-ghost:hover {
  border-color: var(--g3-violet);
  color: var(--g3-violet);
  transform: translateY(-2px);
}
.g3-btn-lg {
  padding: 16px 28px;
  font-size: 15px;
}

/* —— Hero —— */
.g3-hero {
  padding: clamp(56px, 9vw, 110px) clamp(20px, 5vw, 64px) clamp(64px, 8vw, 104px);
}
.g3-hero-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: clamp(36px, 6vw, 72px);
  align-items: center;
}
.g3-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 7px 16px;
  font-size: 13px;
  font-weight: 600;
  color: var(--g3-ink-soft);
  background: var(--g3-glass);
  border: 1px solid var(--g3-line);
  border-radius: 999px;
  margin-bottom: 26px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}
.g3-eyebrow-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--g3-violet);
  box-shadow: 0 0 10px var(--g3-violet);
  animation: g3-pulse 2s ease-in-out infinite;
}
@keyframes g3-pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}
.g3-title {
  font-size: clamp(38px, 5.4vw, 66px);
  font-weight: 900;
  line-height: 1.14;
  letter-spacing: -0.02em;
  margin: 0 0 22px;
  color: var(--g3-ink);
}
.g3-title em {
  font-style: normal;
  background: var(--g3-grad);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.g3-sub {
  font-size: 16.5px;
  color: var(--g3-ink-soft);
  margin: 0 0 34px;
  max-width: 520px;
  line-height: 1.9;
}
.g3-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-bottom: 42px;
}
.g3-stats {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}
.g3-stat {
  display: flex;
  align-items: baseline;
  gap: 9px;
  padding: 13px 20px;
  background: var(--g3-glass);
  border: 1px solid var(--g3-line-soft);
  border-radius: 18px;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}
.g3-stat strong {
  font-family: 'Manrope', 'Noto Sans SC', sans-serif;
  font-size: 26px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -0.02em;
  background: var(--g3-grad);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.g3-stat span {
  font-size: 13px;
  color: var(--g3-ink-mute);
}

/* —— Hero 3D 悬浮场景 —— */
.g3-stage {
  display: flex;
  justify-content: center;
  align-items: center;
  perspective: 1200px;
}
.g3-scene {
  position: relative;
  width: 100%;
  max-width: 440px;
  aspect-ratio: 1 / 1;
  transform-style: preserve-3d;
  transform: rotateX(10deg) rotateY(-16deg);
  transition: transform 0.45s ease-out;
}

/* 旋转玻璃立方：场景内不用 backdrop-filter（preserve-3d 下会被拍扁），用半透明渐变模拟玻璃 */
.g3-cube {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 158px;
  height: 158px;
  margin: -79px 0 0 -79px;
  transform-style: preserve-3d;
  animation: g3-cube-spin 22s linear infinite;
}
@keyframes g3-cube-spin {
  from { transform: rotateX(-18deg) rotateY(0deg); }
  to { transform: rotateX(-18deg) rotateY(360deg); }
}
.g3-cf {
  position: absolute;
  inset: 0;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.85);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.44), rgba(255, 255, 255, 0.16));
  box-shadow: inset 0 0 26px rgba(255, 255, 255, 0.38);
  opacity: 0.94;
}
.g3-cf-1 { transform: translateZ(79px); }
.g3-cf-2 {
  transform: rotateY(90deg) translateZ(79px);
  background: linear-gradient(135deg, rgba(255, 214, 238, 0.5), rgba(255, 255, 255, 0.18));
}
.g3-cf-3 { transform: rotateY(180deg) translateZ(79px); }
.g3-cf-4 {
  transform: rotateY(-90deg) translateZ(79px);
  background: linear-gradient(135deg, rgba(208, 244, 255, 0.5), rgba(255, 255, 255, 0.18));
}
.g3-cf-5 {
  transform: rotateX(90deg) translateZ(79px);
  background: linear-gradient(135deg, rgba(214, 208, 255, 0.5), rgba(255, 255, 255, 0.2));
}
.g3-cf-6 { transform: rotateX(-90deg) translateZ(79px); }

/* 玻璃球 */
.g3-sphere {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle at 32% 30%, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.28) 45%, rgba(255, 255, 255, 0.05) 72%);
  box-shadow: inset -8px -10px 24px rgba(124, 108, 246, 0.25), 0 18px 32px -14px rgba(84, 76, 199, 0.35);
}
.g3-sphere-a {
  width: 82px;
  height: 82px;
  left: 5%;
  top: 15%;
  --z: 40px;
  animation: g3-float-z 7s ease-in-out infinite;
}
.g3-sphere-b {
  width: 54px;
  height: 54px;
  right: 7%;
  bottom: 11%;
  --z: 60px;
  animation: g3-float-z 8.5s ease-in-out infinite 1.2s;
}

/* 漂浮名片（中央主卡） */
.g3-idcard {
  position: absolute;
  left: 50%;
  top: 50%;
  width: min(296px, 84%);
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 24px;
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.85), rgba(255, 255, 255, 0.6));
  border: 1px solid rgba(255, 255, 255, 0.95);
  border-radius: 26px;
  box-shadow: 0 34px 70px -26px rgba(84, 76, 199, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.95);
  transform: translate(-50%, -50%) translateZ(90px);
  animation: g3-idcard-float 6.5s ease-in-out infinite;
}
@keyframes g3-idcard-float {
  0%, 100% { transform: translate(-50%, -50%) translateZ(90px); }
  50% { transform: translate(-50%, calc(-50% - 12px)) translateZ(90px); }
}
.g3-avatar {
  display: grid;
  place-items: center;
  width: 54px;
  height: 54px;
  font-size: 22px;
  font-weight: 800;
  color: #fff;
  background: var(--g3-grad);
  border-radius: 18px;
  box-shadow: 0 14px 30px -10px rgba(124, 108, 246, 0.7);
}
.g3-idcard-text {
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.g3-idcard-text strong {
  font-size: 19px;
  font-weight: 800;
  letter-spacing: 0.01em;
}
.g3-idcard-text span {
  font-size: 13px;
  color: var(--g3-ink-mute);
}
.g3-id-status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  align-self: flex-start;
  padding: 6px 13px;
  font-size: 12px;
  font-weight: 600;
  color: #0e9f6e;
  background: rgba(52, 211, 153, 0.12);
  border: 1px solid rgba(52, 211, 153, 0.3);
  border-radius: 999px;
}
.g3-id-status i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 10px #34d399;
  animation: g3-pulse 2s ease-in-out infinite;
}

/* 漂浮玻璃徽章 */
.g3-fchip {
  position: absolute;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 15px;
  font-size: 12.5px;
  font-weight: 700;
  color: var(--g3-ink);
  background: rgba(255, 255, 255, 0.74);
  border: 1px solid rgba(255, 255, 255, 0.95);
  border-radius: 999px;
  box-shadow: 0 16px 34px -16px rgba(84, 76, 199, 0.42), inset 0 1px 0 rgba(255, 255, 255, 0.9);
  white-space: nowrap;
}
.g3-fchip :deep(svg) {
  color: var(--g3-violet);
}
.g3-fchip-1 {
  top: 8%;
  right: -3%;
  --z: 130px;
  animation: g3-float-z 5.5s ease-in-out infinite;
}
.g3-fchip-2 {
  bottom: 21%;
  left: -7%;
  --z: 110px;
  animation: g3-float-z 6.5s ease-in-out infinite 0.6s;
}
.g3-fchip-3 {
  bottom: -3%;
  right: 9%;
  --z: 150px;
  animation: g3-float-z 7.5s ease-in-out infinite 1.1s;
}
@keyframes g3-float-z {
  0%, 100% { transform: translateZ(var(--z, 60px)); }
  50% { transform: translateZ(var(--z, 60px)) translateY(-10px); }
}

/* —— 通用 section —— */
.g3-section {
  padding: clamp(70px, 9vw, 120px) clamp(20px, 5vw, 64px);
  position: relative;
}
.g3-section-tint {
  background: linear-gradient(180deg, transparent 0%, rgba(124, 108, 246, 0.05) 50%, transparent 100%);
}
.g3-sec-head {
  max-width: 720px;
  margin: 0 auto clamp(44px, 6vw, 72px);
  text-align: center;
}
.g3-sec-eyebrow {
  display: inline-block;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: var(--g3-violet);
  padding: 5px 14px;
  border: 1px solid rgba(124, 108, 246, 0.28);
  border-radius: 999px;
  background: rgba(124, 108, 246, 0.07);
  margin-bottom: 16px;
}
.g3-sec-title {
  font-size: clamp(28px, 4vw, 44px);
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.22;
  margin: 0 0 14px;
  color: var(--g3-ink);
}
.g3-sec-sub {
  font-size: 15px;
  color: var(--g3-ink-mute);
  margin: 0;
  line-height: 1.85;
}

/* —— 通用玻璃卡 —— */
.g3-card {
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.66), rgba(255, 255, 255, 0.4));
  border: 1px solid var(--g3-line);
  border-radius: var(--r-lg);
  backdrop-filter: blur(18px) saturate(150%);
  -webkit-backdrop-filter: blur(18px) saturate(150%);
  box-shadow: var(--g3-shadow-soft);
}

/* —— 作品网格：3D 倾斜跟随 —— */
.g3-works {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 22px;
}
.g3-work-wrap-featured {
  grid-column: span 2;
}
.g3-work {
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transform-style: preserve-3d;
  will-change: transform;
  /* transform 由 tilt 脚本逐帧接管（rAF 插值），此处只过渡阴影，避免双重缓动导致手感发闷 */
  transition: box-shadow 0.35s ease;
}
.g3-work:hover {
  box-shadow: var(--g3-shadow-lift);
}
.g3-work:focus-visible {
  outline: 2px solid var(--g3-violet);
  outline-offset: 3px;
}
.g3-work-cover {
  position: relative;
  aspect-ratio: 16 / 10;
  overflow: hidden;
  background: rgba(238, 240, 255, 0.6);
}
.g3-work-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}
.g3-work:hover .g3-work-cover img {
  transform: scale(1.05);
}
.g3-work-cat {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 12px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #fff;
  background: rgba(35, 42, 77, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 999px;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}
.g3-work-year {
  position: absolute;
  bottom: 12px;
  right: 12px;
  padding: 4px 11px;
  font-size: 11px;
  font-weight: 600;
  color: var(--g3-ink);
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.95);
  border-radius: 999px;
  font-family: 'Manrope', sans-serif;
}
.g3-work-body {
  padding: 20px 24px 24px;
  display: flex;
  flex-direction: column;
}
.g3-work-title {
  font-size: 19px;
  font-weight: 800;
  margin: 0 0 8px;
  color: var(--g3-ink);
  letter-spacing: -0.01em;
}
.g3-work-desc {
  font-size: 13.5px;
  color: var(--g3-ink-mute);
  line-height: 1.75;
  margin: 0 0 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.g3-work-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 14px;
}
.g3-mini-chip {
  padding: 4px 11px;
  font-size: 12px;
  font-weight: 500;
  color: var(--g3-ink-soft);
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid var(--g3-line-soft);
  border-radius: 999px;
  font-family: 'Manrope', 'Noto Sans SC', sans-serif;
}
.g3-work-cta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--g3-violet);
  transition: gap 0.2s ease;
  margin-top: auto;
}
.g3-work:hover .g3-work-cta {
  gap: 10px;
}

/* 旗舰横向卡 */
.g3-work-featured {
  display: grid;
  grid-template-columns: 1.08fr 1fr;
}
.g3-work-cover-featured {
  aspect-ratio: auto;
  height: 100%;
  min-height: 300px;
}
.g3-work-featured .g3-work-desc {
  -webkit-line-clamp: 4;
  line-clamp: 4;
}
.g3-work-featured .g3-work-body {
  padding: 28px 28px 26px;
  justify-content: center;
}
.g3-work-featured .g3-work-title {
  font-size: 24px;
}

/* 倾斜时的高光跟随 */
.g3-glare {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: radial-gradient(420px circle at var(--gx, 50%) var(--gy, 50%), rgba(255, 255, 255, 0.3), transparent 45%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}
.g3-work:hover .g3-glare {
  opacity: 1;
}

/* —— 技能 —— */
.g3-skills {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}
.g3-skill {
  padding: 26px 28px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.g3-skill:hover {
  transform: translateY(-4px);
  box-shadow: var(--g3-shadow-lift);
}
.g3-skill-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px solid rgba(35, 42, 77, 0.08);
}
.g3-skill-cn {
  font-size: 18px;
  font-weight: 800;
  color: var(--g3-ink);
}
.g3-skill-en {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--g3-ink-faint);
  font-family: 'Manrope', sans-serif;
}
.g3-skill-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.g3-skill-list li {
  padding: 7px 14px;
  font-size: 13px;
  color: var(--g3-ink-soft);
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid var(--g3-line-soft);
  border-radius: 999px;
  font-family: 'Manrope', 'Noto Sans SC', sans-serif;
  transition: color 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}
.g3-skill-list li:hover {
  color: var(--g3-violet);
  border-color: rgba(124, 108, 246, 0.4);
  background: rgba(124, 108, 246, 0.08);
}

/* —— 关于我 —— */
.g3-about {
  max-width: 1080px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: clamp(36px, 6vw, 64px);
  align-items: center;
}
.g3-about-lead {
  font-size: 17px;
  color: var(--g3-ink-soft);
  line-height: 2;
  margin: 0 0 22px;
}
.g3-about-meta {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  flex-wrap: wrap;
  font-size: 14px;
  color: var(--g3-violet);
  font-weight: 600;
  margin: 0;
}
.g3-dot-sep {
  font-style: normal;
  color: var(--g3-ink-faint);
  margin: 0 3px;
}
.g3-about-cards {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
}
.g3-statcard {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 26px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.g3-statcard:hover {
  transform: translateY(-3px);
  box-shadow: var(--g3-shadow-lift);
}
.g3-statcard strong {
  font-family: 'Manrope', sans-serif;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1;
  background: var(--g3-grad);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.g3-statcard span {
  font-size: 13px;
  color: var(--g3-ink-mute);
}

/* —— 历程时间线（左轨） —— */
.g3-timeline {
  max-width: 880px;
  margin: 0 auto;
  list-style: none;
  padding: 0 0 0 34px;
  position: relative;
}
.g3-timeline::before {
  content: '';
  position: absolute;
  left: 9px;
  top: 8px;
  bottom: 8px;
  width: 2px;
  border-radius: 2px;
  background: linear-gradient(180deg, rgba(124, 108, 246, 0.75), rgba(236, 95, 168, 0.55), rgba(31, 182, 212, 0.65));
}
.g3-tl {
  position: relative;
  padding-bottom: 26px;
}
.g3-tl:last-child {
  padding-bottom: 0;
}
.g3-tl-dot {
  position: absolute;
  left: -30px;
  top: 26px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #fff;
  border: 3px solid var(--g3-violet);
  box-shadow: 0 0 0 5px rgba(124, 108, 246, 0.14), 0 8px 18px -6px rgba(124, 108, 246, 0.55);
}
.g3-tl-card {
  padding: 22px 26px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.g3-tl-card:hover {
  transform: translateX(4px);
  box-shadow: var(--g3-shadow-lift);
}
.g3-tl-period {
  display: inline-block;
  font-size: 12px;
  font-weight: 700;
  color: var(--g3-violet);
  background: rgba(124, 108, 246, 0.09);
  border: 1px solid rgba(124, 108, 246, 0.22);
  padding: 3px 11px;
  border-radius: 999px;
  margin-bottom: 12px;
  font-family: 'Manrope', sans-serif;
  letter-spacing: 0.05em;
}
.g3-tl-title {
  font-size: 18px;
  font-weight: 800;
  margin: 0 0 5px;
  color: var(--g3-ink);
}
.g3-tl-org {
  font-size: 13px;
  font-weight: 600;
  color: var(--g3-cyan);
}
.g3-tl-desc {
  font-size: 14px;
  color: var(--g3-ink-mute);
  line-height: 1.75;
  margin: 10px 0 0;
}

/* —— 联系 —— */
.g3-contact {
  max-width: 1080px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: clamp(32px, 5vw, 56px);
  padding: clamp(36px, 5vw, 60px);
  position: relative;
  overflow: hidden;
  border-radius: var(--r-xl);
}
.g3-contact::before {
  content: '';
  position: absolute;
  top: -40%;
  right: -15%;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(124, 108, 246, 0.16), transparent 65%);
  pointer-events: none;
}
.g3-contact-title {
  font-size: clamp(26px, 3.4vw, 38px);
  font-weight: 800;
  color: var(--g3-ink);
  margin: 0 0 16px;
  letter-spacing: -0.02em;
  line-height: 1.28;
}
.g3-contact-sub {
  font-size: 15px;
  color: var(--g3-ink-mute);
  line-height: 1.85;
  margin: 0 0 26px;
  max-width: 420px;
}
.g3-contact-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 13px;
}
.g3-contact-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  color: var(--g3-ink-soft);
}
.g3-contact-list li :deep(svg) {
  color: var(--g3-violet);
  flex-shrink: 0;
}
.g3-contact-list a {
  color: var(--g3-ink-soft);
  text-decoration: none;
  transition: color 0.2s ease;
  font-family: 'Manrope', 'Noto Sans SC', sans-serif;
}
.g3-contact-list a:hover {
  color: var(--g3-violet);
}
.g3-contact-right {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 14px;
  position: relative;
  z-index: 1;
}
.g3-social {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 11px 18px;
  font-size: 14px;
  font-weight: 600;
  color: var(--g3-ink-soft);
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid var(--g3-line-soft);
  border-radius: 999px;
  text-decoration: none;
  transition: color 0.2s ease, border-color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}
.g3-social:hover {
  color: var(--g3-violet);
  border-color: rgba(124, 108, 246, 0.4);
  background: rgba(124, 108, 246, 0.08);
  transform: translateX(4px);
}
.g3-social :deep(svg) {
  color: var(--g3-violet);
}

/* —— 页脚 —— */
.g3-footer {
  padding: 32px clamp(20px, 5vw, 64px) 36px;
  border-top: 1px solid rgba(35, 42, 77, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
}
.g3-footer-brand {
  font-weight: 800;
  color: var(--g3-ink);
  letter-spacing: 0.02em;
}
.g3-footer-copy {
  font-size: 13px;
  color: var(--g3-ink-faint);
}

/* —— 回到顶部 —— */
.g3-backtop {
  position: fixed;
  right: 26px;
  bottom: 26px;
  z-index: 60;
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  color: var(--g3-ink);
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.95);
  border-radius: 50%;
  cursor: pointer;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  box-shadow: 0 16px 36px -16px rgba(84, 76, 199, 0.4);
  transition: transform 0.2s ease, color 0.2s ease, border-color 0.2s ease;
}
.g3-backtop:hover {
  transform: translateY(-3px);
  color: var(--g3-violet);
  border-color: rgba(124, 108, 246, 0.4);
}

/* —— 作品弹窗 —— */
.g3-modal {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: none;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.g3-modal.is-open {
  display: flex;
}
.modal-overlay {
  position: absolute;
  inset: 0;
  background: rgba(46, 51, 96, 0.4);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}
.g3-modal-card {
  position: relative;
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  max-width: 920px;
  width: 100%;
  max-height: 88vh;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid #fff;
  border-radius: 30px;
  overflow: hidden;
  box-shadow: 0 40px 90px -30px rgba(46, 51, 96, 0.45);
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
  color: var(--g3-ink-soft);
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid var(--g3-line);
  border-radius: 50%;
  cursor: pointer;
  transition: transform 0.2s ease, background 0.2s ease, color 0.2s ease;
}
.modal-close:hover {
  background: rgba(124, 108, 246, 0.15);
  color: var(--g3-violet);
  transform: rotate(90deg);
}
.g3-modal-cover {
  position: relative;
  overflow: hidden;
  min-height: 300px;
  background: rgba(238, 240, 255, 0.8);
}
.g3-modal-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.g3-modal-year {
  position: absolute;
  bottom: 14px;
  left: 14px;
  padding: 5px 13px;
  font-size: 12px;
  font-weight: 600;
  color: var(--g3-ink);
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.95);
  border-radius: 999px;
  font-family: 'Manrope', sans-serif;
}
.g3-modal-body {
  padding: 38px 38px 34px;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.g3-modal-cat {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--g3-violet);
  margin-bottom: 12px;
}
.g3-modal-title {
  font-size: 26px;
  font-weight: 800;
  color: var(--g3-ink);
  margin: 0 0 14px;
  line-height: 1.3;
  letter-spacing: -0.01em;
}
.g3-modal-desc {
  font-size: 14px;
  color: var(--g3-ink-mute);
  line-height: 1.9;
  margin: 0 0 22px;
}
.g3-modal-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 26px;
}
.g3-modal-link {
  align-self: flex-start;
  margin-top: auto;
}

/* —— 滚动渐入（带轻微 3D 入场）—— */
.reveal {
  opacity: 0;
  transform: translateY(30px) rotateX(6deg);
  transform-origin: 50% 0;
  transition: opacity 0.8s cubic-bezier(0.22, 1, 0.36, 1),
    transform 0.8s cubic-bezier(0.22, 1, 0.36, 1);
  transition-delay: calc(var(--i, 0) * 90ms);
}
.reveal.is-visible {
  opacity: 1;
  transform: none;
}

/* —— 减少动效 —— */
@media (prefers-reduced-motion: reduce) {
  .g3-page *,
  .g3-page *::before,
  .g3-page *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* —— 响应式 —— */
@media (max-width: 960px) {
  .g3-hero-inner {
    grid-template-columns: 1fr;
  }
  .g3-stage {
    max-width: 360px;
    margin: 0 auto;
    width: 100%;
  }
  .g3-works {
    grid-template-columns: repeat(2, 1fr);
  }
  .g3-work-wrap-featured {
    grid-column: span 2;
  }
  .g3-work-featured {
    grid-template-columns: 1fr;
  }
  .g3-work-cover-featured {
    aspect-ratio: 16 / 9;
    min-height: 0;
    height: auto;
  }
  .g3-skills,
  .g3-about,
  .g3-contact {
    grid-template-columns: 1fr;
  }
  .g3-nav-links {
    display: none;
  }
}
@media (max-width: 560px) {
  .g3-works {
    grid-template-columns: 1fr;
  }
  .g3-work-wrap-featured {
    grid-column: span 1;
  }
  .g3-modal-card {
    grid-template-columns: 1fr;
    max-height: 90vh;
    overflow-y: auto;
  }
  .g3-modal-cover {
    min-height: 200px;
    max-height: 220px;
  }
  .g3-modal-body {
    padding: 26px;
  }
  .g3-stat strong {
    font-size: 22px;
  }
  .g3-fchip-2 {
    left: -2%;
  }
}
</style>

<script setup lang="ts">
import { usePortfolio } from '@/composables/usePortfolio'
import { profile as defaultProfile } from '@/data/profile'

const portfolio = usePortfolio()
const profile = portfolio?.profile ?? defaultProfile

const scrollToAbout = () => {
  document.querySelector('#about')?.scrollIntoView({ behavior: 'smooth' })
}

const scrollToWorks = () => {
  document.querySelector('#works')?.scrollIntoView({ behavior: 'smooth' })
}

const scrollToContact = () => {
  document.querySelector('#contact')?.scrollIntoView({ behavior: 'smooth' })
}
</script>

<template>
  <section id="home" class="hero">
    <!-- 背景光晕 -->
    <div class="hero-glow" aria-hidden="true">
      <span class="glow glow-1"></span>
      <span class="glow glow-2"></span>
    </div>

    <!-- 装饰文字 -->
    <span class="hero-deco font-display" aria-hidden="true">Portfolio</span>

    <div class="container hero-content">
      <p class="hero-intro reveal">
        <span class="intro-line"></span>
        你好，我是 {{ profile.name }}
      </p>

      <h1 class="hero-name reveal reveal-delay-1">
        <span class="name-cn font-serif">{{ profile.name }}</span>
        <span class="name-en font-display">{{ profile.nameEn }}</span>
      </h1>

      <p class="hero-title reveal reveal-delay-2">{{ profile.title }}</p>

      <p class="hero-tagline reveal reveal-delay-3">{{ profile.tagline }}</p>

      <div class="hero-actions reveal reveal-delay-4">
        <a href="#works" class="btn btn-primary" @click.prevent="scrollToWorks">
          浏览作品
        </a>
        <a href="#contact" class="btn btn-ghost" @click.prevent="scrollToContact">
          合作邀约
        </a>
      </div>
    </div>

    <button class="scroll-hint" aria-label="向下滚动" @click="scrollToAbout">
      <span class="scroll-text font-mono">SCROLL</span>
      <span class="scroll-line"></span>
    </button>
  </section>
</template>

<style scoped>
.hero {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  overflow: hidden;
  padding: 8rem 0 4rem;
}

/* —— 背景光晕 —— */
.hero-glow {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

.glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: drift 18s var(--ease) infinite alternate;
}

.glow-1 {
  width: 50vw;
  height: 50vw;
  max-width: 640px;
  max-height: 640px;
  top: -10%;
  left: -8%;
  background: radial-gradient(circle, rgba(212, 165, 116, 0.35), transparent 70%);
}

.glow-2 {
  width: 40vw;
  height: 40vw;
  max-width: 520px;
  max-height: 520px;
  bottom: -12%;
  right: -6%;
  background: radial-gradient(circle, rgba(123, 168, 156, 0.22), transparent 70%);
  animation-delay: -6s;
  animation-duration: 22s;
}

@keyframes drift {
  0% {
    transform: translate(0, 0) scale(1);
  }
  100% {
    transform: translate(6%, 4%) scale(1.12);
  }
}

/* —— 装饰文字 —— */
.hero-deco {
  position: absolute;
  right: -2%;
  top: 18%;
  font-size: clamp(8rem, 22vw, 20rem);
  font-weight: 500;
  font-style: italic;
  color: var(--ink-line);
  opacity: 0.4;
  z-index: 0;
  user-select: none;
  pointer-events: none;
  white-space: nowrap;
}

/* —— 内容 —— */
.hero-content {
  position: relative;
  z-index: 1;
}

.hero-intro {
  display: inline-flex;
  align-items: center;
  gap: 1rem;
  font-size: var(--fs-small);
  color: var(--text-dim);
  letter-spacing: 0.05em;
  margin-bottom: 1.5rem;
}

.intro-line {
  width: 2.5rem;
  height: 1px;
  background: var(--accent);
}

.hero-name {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  margin-bottom: 1.5rem;
  line-height: 0.95;
}

.name-cn {
  font-size: var(--fs-hero);
  font-weight: 900;
  letter-spacing: 0.08em;
  background: linear-gradient(180deg, var(--moon-white) 30%, var(--moon-dim) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.name-en {
  font-size: clamp(1.5rem, 3vw, 2.25rem);
  font-style: italic;
  font-weight: 400;
  color: var(--accent);
  letter-spacing: 0.04em;
}

.hero-title {
  font-family: var(--font-mono);
  font-size: var(--fs-small);
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--text-faint);
  margin-bottom: 1.5rem;
}

.hero-tagline {
  font-family: var(--font-serif);
  font-size: clamp(1.125rem, 2vw, 1.5rem);
  font-weight: 400;
  line-height: 1.7;
  color: var(--text-dim);
  max-width: 36ch;
  margin-bottom: 2.5rem;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

/* —— 滚动提示 —— */
.scroll-hint {
  position: absolute;
  bottom: 2.5rem;
  right: var(--container-pad);
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  color: var(--text-faint);
  transition: color var(--dur) var(--ease);
}

.scroll-hint:hover {
  color: var(--accent);
}

.scroll-text {
  font-size: var(--fs-tiny);
  letter-spacing: 0.3em;
  writing-mode: vertical-rl;
}

.scroll-line {
  width: 1px;
  height: 3rem;
  background: linear-gradient(180deg, currentColor, transparent);
  position: relative;
  overflow: hidden;
}

.scroll-line::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 40%;
  background: var(--accent);
  animation: scrollDown 2s var(--ease) infinite;
}

@keyframes scrollDown {
  0% {
    transform: translateY(-100%);
  }
  100% {
    transform: translateY(250%);
  }
}

@media (max-width: 768px) {
  .hero {
    padding: 7rem 0 3rem;
  }

  .hero-deco {
    opacity: 0.25;
    top: 8%;
  }

  .scroll-hint {
    display: none;
  }

  .hero-tagline {
    max-width: 100%;
  }
}
</style>

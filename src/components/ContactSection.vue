<script setup lang="ts">
import { Github, Mail, ArrowUpRight } from 'lucide-vue-next'
import { usePortfolio } from '@/composables/usePortfolio'
import { profile as defaultProfile } from '@/data/profile'

const portfolio = usePortfolio()
const profile = portfolio?.profile ?? defaultProfile
</script>

<template>
  <section id="contact" class="section contact">
    <div class="container">
      <div class="contact-inner">
        <span class="section-label reveal">联系方式 · Contact</span>

        <h2 class="contact-title font-serif reveal reveal-delay-1">
          有想法？<br />让我们聊聊。
        </h2>

        <p class="contact-sub reveal reveal-delay-2">
          无论是商业合作、项目咨询，还是单纯想交流设计与技术，<br />
          都欢迎随时来信。通常会在 24 小时内回复。
        </p>

        <a
          :href="`mailto:${profile.email}`"
          class="contact-mail reveal reveal-delay-3"
        >
          <span class="mail-text font-display">{{ profile.email }}</span>
          <span class="mail-icon">
            <ArrowUpRight :size="20" :stroke-width="1.5" />
          </span>
        </a>

        <div class="contact-socials reveal reveal-delay-4">
          <a
            v-for="social in profile.socials"
            :key="social.name"
            :href="social.url"
            target="_blank"
            rel="noopener noreferrer"
            class="social-link"
          >
            <Github v-if="social.icon === 'github'" :size="18" :stroke-width="1.5" />
            <Mail v-else-if="social.icon === 'mail'" :size="18" :stroke-width="1.5" />
            <span v-else class="social-text">{{ social.name }}</span>
            <span class="social-name">{{ social.name }}</span>
          </a>
        </div>
      </div>
    </div>

    <!-- 装饰大字 -->
    <span class="contact-deco font-display" aria-hidden="true">Hello</span>
  </section>
</template>

<style scoped>
.contact {
  position: relative;
  overflow: hidden;
}

.contact-inner {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  max-width: 720px;
  margin: 0 auto;
}

.contact-title {
  font-size: clamp(2.5rem, 6vw, 4.5rem);
  font-weight: 500;
  line-height: 1.15;
  margin-bottom: 1.5rem;
  background: linear-gradient(180deg, var(--moon-white), var(--moon-dim));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.contact-sub {
  font-size: 1.0625rem;
  line-height: 1.9;
  color: var(--text-dim);
  margin-bottom: 2.5rem;
}

.contact-mail {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 2rem;
  margin-bottom: 3rem;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  transition: all var(--dur) var(--ease);
}

.contact-mail:hover {
  border-color: var(--accent);
  background: rgba(212, 165, 116, 0.04);
  transform: translateY(-2px);
}

.mail-text {
  font-size: clamp(1.25rem, 3vw, 1.75rem);
  font-style: italic;
  color: var(--text);
  transition: color var(--dur) var(--ease);
}

.contact-mail:hover .mail-text {
  color: var(--accent);
}

.mail-icon {
  display: grid;
  place-items: center;
  width: 2rem;
  height: 2rem;
  color: var(--accent);
  transition: transform var(--dur) var(--ease);
}

.contact-mail:hover .mail-icon {
  transform: rotate(45deg);
}

.contact-socials {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 1rem;
}

.social-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1.125rem;
  font-size: var(--fs-small);
  color: var(--text-dim);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  transition: all var(--dur) var(--ease);
}

.social-link:hover {
  color: var(--accent);
  border-color: var(--accent);
  transform: translateY(-2px);
}

.social-text {
  font-family: var(--font-serif);
  font-weight: 500;
}

/* —— 装饰大字 —— */
.contact-deco {
  position: absolute;
  left: 50%;
  bottom: -3rem;
  transform: translateX(-50%);
  font-size: clamp(8rem, 24vw, 22rem);
  font-style: italic;
  font-weight: 500;
  color: var(--ink-line);
  opacity: 0.35;
  z-index: 0;
  user-select: none;
  pointer-events: none;
  white-space: nowrap;
  line-height: 1;
}

@media (max-width: 640px) {
  .contact-sub br {
    display: none;
  }

  .contact-deco {
    bottom: -1.5rem;
    opacity: 0.2;
  }
}
</style>

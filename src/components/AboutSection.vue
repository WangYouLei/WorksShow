<script setup lang="ts">
import { MapPin, Mail } from 'lucide-vue-next'
import { usePortfolio } from '@/composables/usePortfolio'
import { profile as defaultProfile } from '@/data/profile'

const portfolio = usePortfolio()
const profile = portfolio?.profile ?? defaultProfile
</script>

<template>
  <section id="about" class="section about">
    <div class="container">
      <div class="about-grid">
        <!-- 左：头像 -->
        <div class="about-portrait reveal">
          <div class="portrait-frame">
            <img :src="profile.avatar" :alt="profile.name" loading="lazy" />
            <span class="portrait-tag font-mono">{{ profile.nameEn }}</span>
          </div>
          <div class="portrait-meta">
            <p class="meta-item">
              <MapPin :size="14" :stroke-width="1.5" />
              {{ profile.location }}
            </p>
            <p class="meta-item meta-avail">
              <span class="avail-dot"></span>
              {{ profile.availability }}
            </p>
          </div>
        </div>

        <!-- 右：简介 -->
        <div class="about-content">
          <span class="section-label reveal">关于我 · About</span>
          <h2 class="section-title reveal reveal-delay-1">
            在功能与美感之间，<br />寻找恰到好处的平衡。
          </h2>
          <p class="about-bio reveal reveal-delay-2">{{ profile.bio }}</p>

          <div class="about-stats">
            <div
              v-for="(stat, i) in profile.stats"
              :key="stat.label"
              class="stat-card reveal"
              :class="`reveal-delay-${i + 1}`"
            >
              <span class="stat-value font-display">{{ stat.value }}</span>
              <span class="stat-label">{{ stat.label }}</span>
            </div>
          </div>

          <a
            :href="`mailto:${profile.email}`"
            class="about-mail reveal reveal-delay-4"
          >
            <Mail :size="16" :stroke-width="1.5" />
            {{ profile.email }}
          </a>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.about-grid {
  display: grid;
  grid-template-columns: 0.85fr 1.15fr;
  gap: clamp(2rem, 5vw, 5rem);
  align-items: start;
}

/* —— 头像 —— */
.portrait-frame {
  position: relative;
  aspect-ratio: 4 / 5;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--border);
}

.portrait-frame::before {
  content: '';
  position: absolute;
  inset: -1px;
  z-index: 2;
  border: 1px solid var(--accent);
  border-radius: var(--radius-md);
  opacity: 0;
  transform: scale(1.04);
  transition: all 0.6s var(--ease);
  pointer-events: none;
}

.portrait-frame:hover::before {
  opacity: 0.5;
  transform: scale(1);
}

.portrait-frame img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: grayscale(0.15) contrast(1.05);
  transition: filter 0.8s var(--ease);
}

.portrait-frame:hover img {
  filter: grayscale(0) contrast(1);
}

.portrait-tag {
  position: absolute;
  bottom: 1rem;
  left: 1rem;
  z-index: 2;
  font-size: var(--fs-tiny);
  letter-spacing: 0.2em;
  color: var(--moon-white);
  background: rgba(10, 10, 15, 0.6);
  padding: 0.375rem 0.75rem;
  border-radius: var(--radius-sm);
  backdrop-filter: blur(4px);
}

.portrait-meta {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-top: 1.25rem;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: var(--fs-small);
  color: var(--text-faint);
}

.meta-avail {
  color: var(--celadon);
}

.avail-dot {
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
  background: var(--celadon);
  box-shadow: 0 0 0 0 rgba(123, 168, 156, 0.5);
  animation: pulse 2s var(--ease) infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(123, 168, 156, 0.5);
  }
  70% {
    box-shadow: 0 0 0 8px rgba(123, 168, 156, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(123, 168, 156, 0);
  }
}

/* —— 内容 —— */
.about-bio {
  font-size: 1.0625rem;
  line-height: 2;
  color: var(--text-dim);
  margin-bottom: 2.5rem;
  max-width: 52ch;
}

.about-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 2.5rem;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
  padding: 1.5rem 1.25rem;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  transition: all var(--dur) var(--ease);
}

.stat-card:hover {
  border-color: var(--accent);
  transform: translateY(-4px);
}

.stat-value {
  font-size: 2.25rem;
  font-weight: 500;
  color: var(--accent);
  line-height: 1;
}

.stat-label {
  font-size: var(--fs-tiny);
  color: var(--text-faint);
  letter-spacing: 0.1em;
}

.about-mail {
  display: inline-flex;
  align-items: center;
  gap: 0.625rem;
  font-family: var(--font-mono);
  font-size: var(--fs-small);
  color: var(--text-dim);
  transition: color var(--dur) var(--ease);
}

.about-mail:hover {
  color: var(--accent);
}

@media (max-width: 1024px) {
  .about-grid {
    grid-template-columns: 1fr;
    gap: 3rem;
  }

  .about-portrait {
    max-width: 360px;
  }
}

@media (max-width: 560px) {
  .about-stats {
    grid-template-columns: 1fr;
  }
}
</style>

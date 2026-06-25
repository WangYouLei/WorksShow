<script setup lang="ts">
import type { Work } from '@/data/types'
import { ArrowUpRight } from 'lucide-vue-next'
import { usePortfolio } from '@/composables/usePortfolio'
import { works as defaultWorks } from '@/data/works'

const portfolio = usePortfolio()
const works = portfolio?.works ?? defaultWorks

const emit = defineEmits<{ select: [work: Work] }>()
</script>

<template>
  <section id="works" class="section works">
    <div class="container">
      <div class="works-head">
        <div>
          <span class="section-label reveal">精选作品 · Works</span>
          <h2 class="section-title reveal reveal-delay-1">
            每一件作品，<br />都是一次完整的表达。
          </h2>
        </div>
        <p class="works-count reveal reveal-delay-2">
          <span class="count-num font-display">{{ works.length }}</span>
          <span class="count-label">个精选项目</span>
        </p>
      </div>

      <div class="works-grid">
        <article
          v-for="(work, i) in works"
          :key="work.id"
          class="work-card reveal"
          :class="[`reveal-delay-${(i % 3) + 1}`, { 'is-wide': work.highlight }]"
          :data-work-id="work.id"
          @click="emit('select', work)"
        >
          <div class="work-cover">
            <img :src="work.cover" :alt="work.title" loading="lazy" />
            <span class="work-year font-mono">{{ work.year }}</span>
            <span class="work-arrow">
              <ArrowUpRight :size="20" :stroke-width="1.5" />
            </span>
          </div>

          <div class="work-info">
            <span class="work-category font-mono">{{ work.category }}</span>
            <h3 class="work-title font-serif">{{ work.title }}</h3>
            <div class="work-tags">
              <span v-for="tag in work.tags.slice(0, 3)" :key="tag" class="work-tag">{{ tag }}</span>
            </div>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.works-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 2rem;
  margin-bottom: 3.5rem;
}

.works-count {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.25rem;
  flex-shrink: 0;
}

.count-num {
  font-size: 3rem;
  font-weight: 500;
  color: var(--accent);
  line-height: 1;
}

.count-label {
  font-size: var(--fs-tiny);
  color: var(--text-faint);
  letter-spacing: 0.1em;
}

.works-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 2rem;
}

/* —— 作品卡片 —— */
.work-card {
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.work-cover {
  position: relative;
  aspect-ratio: 16 / 10;
  overflow: hidden;
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  background: var(--ink-soft);
  margin-bottom: 1.25rem;
}

.work-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.8s var(--ease);
}

.work-card:hover .work-cover img {
  transform: scale(1.06);
}

.work-year {
  position: absolute;
  top: 1rem;
  left: 1rem;
  font-size: var(--fs-tiny);
  letter-spacing: 0.15em;
  color: var(--moon-white);
  background: rgba(10, 10, 15, 0.7);
  padding: 0.375rem 0.75rem;
  border-radius: var(--radius-sm);
  backdrop-filter: blur(4px);
}

.work-arrow {
  position: absolute;
  top: 1rem;
  right: 1rem;
  display: grid;
  place-items: center;
  width: 2.5rem;
  height: 2.5rem;
  color: var(--ink-black);
  background: var(--accent);
  border-radius: 50%;
  opacity: 0;
  transform: translate(8px, -8px) scale(0.8);
  transition: all var(--dur) var(--ease);
}

.work-card:hover .work-arrow {
  opacity: 1;
  transform: translate(0, 0) scale(1);
}

.work-info {
  display: flex;
  flex-direction: column;
  gap: 0.625rem;
}

.work-category {
  font-size: var(--fs-tiny);
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--accent);
}

.work-title {
  font-size: var(--fs-h3);
  font-weight: 500;
  line-height: 1.3;
  transition: color var(--dur) var(--ease);
}

.work-card:hover .work-title {
  color: var(--accent);
}

.work-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.25rem;
}

.work-tag {
  font-size: var(--fs-tiny);
  font-family: var(--font-mono);
  color: var(--text-faint);
  padding: 0.25rem 0.625rem;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
}

@media (max-width: 768px) {
  .works-grid {
    grid-template-columns: 1fr;
    gap: 2.5rem;
  }

  .works-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .works-count {
    flex-direction: row;
    align-items: baseline;
    gap: 0.5rem;
  }
}
</style>

<script setup lang="ts">
import { ArrowUp } from 'lucide-vue-next'
import { usePortfolio } from '@/composables/usePortfolio'
import { profile as defaultProfile } from '@/data/profile'

const portfolio = usePortfolio()
const profile = portfolio?.profile ?? defaultProfile

const backToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
  <footer class="footer">
    <div class="container footer-inner">
      <div class="footer-left">
        <span class="footer-mark">墨</span>
        <p class="footer-copyright">
          © {{ new Date().getFullYear() }} {{ profile.name }} · {{ profile.nameEn }} Studio
        </p>
      </div>

      <button class="back-top" aria-label="回到顶部" @click="backToTop">
        <span>回到顶部</span>
        <ArrowUp :size="16" :stroke-width="1.5" />
      </button>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  border-top: 1px solid var(--border);
  padding: 2.5rem 0;
  background: var(--ink-deep);
}

.footer-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.footer-mark {
  display: grid;
  place-items: center;
  width: 1.75rem;
  height: 1.75rem;
  font-family: var(--font-serif);
  font-size: 0.9375rem;
  font-weight: 700;
  color: var(--ink-black);
  background: var(--accent);
  border-radius: var(--radius-sm);
}

.footer-copyright {
  font-size: var(--fs-small);
  color: var(--text-dim);
}

.back-top {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: var(--fs-tiny);
  letter-spacing: 0.15em;
  text-transform: uppercase;
  color: var(--text-faint);
  transition: color var(--dur) var(--ease);
}

.back-top:hover {
  color: var(--accent);
}

@media (max-width: 640px) {
  .footer-inner {
    flex-direction: column;
    align-items: flex-start;
    gap: 1.5rem;
  }
}
</style>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Menu, X } from 'lucide-vue-next'
import { usePortfolio } from '@/composables/usePortfolio'
import { profile as defaultProfile } from '@/data/profile'

const portfolio = usePortfolio()
const profile = portfolio?.profile ?? defaultProfile

const scrolled = ref(false)
const menuOpen = ref(false)

const navItems = [
  { label: '首页', href: '#home' },
  { label: '关于', href: '#about' },
  { label: '作品', href: '#works' },
  { label: '经历', href: '#skills' },
]

const onScroll = () => {
  scrolled.value = window.scrollY > 50
}

const closeMenu = () => {
  menuOpen.value = false
}

const handleNavClick = (e: Event, href: string) => {
  e.preventDefault()
  closeMenu()
  const target = document.querySelector(href)
  if (target) {
    target.scrollIntoView({ behavior: 'smooth' })
  }
}

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <header class="navbar" :class="{ 'is-scrolled': scrolled }">
    <div class="container nav-inner">
      <a
        href="#home"
        class="logo"
        @click="handleNavClick($event, '#home')"
      >
        <span class="logo-mark">{{ (profile.name || '墨').charAt(0) }}</span>
        <span class="logo-text">{{ profile.nameEn || 'Mobai' }}</span>
      </a>

      <nav class="nav-links" :class="{ 'is-open': menuOpen }">
        <a
          v-for="item in navItems"
          :key="item.href"
          :href="item.href"
          class="nav-link"
          @click="handleNavClick($event, item.href)"
        >
          {{ item.label }}
        </a>
        <a
          href="#contact"
          class="nav-cta"
          @click="handleNavClick($event, '#contact')"
        >
          联系
        </a>
      </nav>

      <button
        class="menu-toggle"
        :aria-label="menuOpen ? '关闭菜单' : '打开菜单'"
        @click="menuOpen = !menuOpen"
      >
        <Menu v-if="!menuOpen" :size="22" :stroke-width="1.5" />
        <X v-else :size="22" :stroke-width="1.5" />
      </button>
    </div>
  </header>
</template>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  transition: all var(--dur) var(--ease);
  padding: 1.5rem 0;
}

.navbar.is-scrolled {
  padding: 0.875rem 0;
  background: rgba(10, 10, 15, 0.72);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid var(--border);
}

.nav-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  font-family: var(--font-display);
  font-size: 1.375rem;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.logo-mark {
  display: grid;
  place-items: center;
  width: 2rem;
  height: 2rem;
  font-family: var(--font-serif);
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--ink-black);
  background: var(--accent);
  border-radius: var(--radius-sm);
  transition: transform var(--dur) var(--ease);
}

.logo:hover .logo-mark {
  transform: rotate(-8deg) scale(1.05);
}

.logo-text {
  color: var(--text);
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.nav-link {
  position: relative;
  font-size: var(--fs-small);
  color: var(--text-dim);
  transition: color var(--dur) var(--ease);
}

.nav-link::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -6px;
  width: 0;
  height: 1px;
  background: var(--accent);
  transition: width var(--dur) var(--ease);
}

.nav-link:hover {
  color: var(--text);
}

.nav-link:hover::after {
  width: 100%;
}

.nav-cta {
  padding: 0.5rem 1.25rem;
  font-size: var(--fs-small);
  color: var(--accent);
  border: 1px solid var(--accent);
  border-radius: var(--radius-sm);
  transition: all var(--dur) var(--ease);
}

.nav-cta:hover {
  background: var(--accent);
  color: var(--ink-black);
}

.menu-toggle {
  display: none;
  width: 2.75rem;
  height: 2.75rem;
  align-items: center;
  justify-content: center;
  color: var(--text);
}

@media (max-width: 768px) {
  .menu-toggle {
    display: flex;
  }

  .nav-links {
    position: fixed;
    inset: 0;
    flex-direction: column;
    justify-content: center;
    gap: 2rem;
    background: rgba(10, 10, 15, 0.96);
    backdrop-filter: blur(24px);
    -webkit-backdrop-filter: blur(24px);
    opacity: 0;
    pointer-events: none;
    transform: translateY(-8px);
    transition: opacity var(--dur) var(--ease), transform var(--dur) var(--ease);
  }

  .nav-links.is-open {
    opacity: 1;
    pointer-events: auto;
    transform: translateY(0);
  }

  .nav-link {
    font-size: 1.5rem;
    font-family: var(--font-serif);
  }

  .nav-cta {
    font-size: 1rem;
    padding: 0.75rem 2rem;
  }
}
</style>

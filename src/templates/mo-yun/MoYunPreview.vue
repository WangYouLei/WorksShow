<script setup lang="ts">
import { ref, provide } from 'vue'
import NavBar from '@/components/NavBar.vue'
import HeroSection from '@/components/HeroSection.vue'
import AboutSection from '@/components/AboutSection.vue'
import WorksSection from '@/components/WorksSection.vue'
import SkillsSection from '@/components/SkillsSection.vue'
import ContactSection from '@/components/ContactSection.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import WorkModal from '@/components/WorkModal.vue'
import { PORTFOLIO_KEY } from '@/composables/usePortfolio'
import type { PortfolioData } from '@/templates/types'
import type { Work } from '@/data/types'

const props = defineProps<{ data: PortfolioData }>()
provide(PORTFOLIO_KEY, props.data)

const selectedWork = ref<Work | null>(null)
const onSelectWork = (w: Work) => {
  selectedWork.value = w
}
const onCloseModal = () => {
  selectedWork.value = null
}
</script>

<template>
  <NavBar />
  <main>
    <HeroSection />
    <AboutSection />
    <WorksSection @select="onSelectWork" />
    <SkillsSection />
    <ContactSection />
  </main>
  <SiteFooter />
  <WorkModal :work="selectedWork" @close="onCloseModal" />
</template>

<style scoped>
main {
  min-height: 100vh;
}
</style>

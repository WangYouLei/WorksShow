<script setup lang="ts">
import { Briefcase, GraduationCap } from 'lucide-vue-next'
import { usePortfolio } from '@/composables/usePortfolio'
import { skills as defaultSkills } from '@/data/skills'
import { experiences as defaultExperiences } from '@/data/experiences'

const portfolio = usePortfolio()
const skills = portfolio?.skills ?? defaultSkills
const experiences = portfolio?.experiences ?? defaultExperiences
</script>

<template>
  <section id="skills" class="section skills">
    <div class="container">
      <div class="skills-head">
        <span class="section-label reveal">技能与经历 · Skills</span>
        <h2 class="section-title reveal reveal-delay-1">
          八年沉淀，<br />从代码到设计的完整能力闭环。
        </h2>
      </div>

      <div class="skills-grid">
        <!-- 左：技能标签云 -->
        <div class="skills-cloud reveal">
          <h3 class="block-title font-serif">技能矩阵</h3>
          <div class="cloud-groups">
            <div v-for="skill in skills" :key="skill.id" class="cloud-group">
              <p class="cloud-cat">
                <span class="cat-cn">{{ skill.category }}</span>
                <span class="cat-en font-mono">{{ skill.categoryEn }}</span>
              </p>
              <div class="cloud-items">
                <span v-for="item in skill.items" :key="item" class="cloud-tag">
                  {{ item }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右：时间轴 -->
        <div class="skills-timeline reveal reveal-delay-1">
          <h3 class="block-title font-serif">职业历程</h3>
          <ol class="timeline">
            <li
              v-for="exp in experiences"
              :key="exp.id"
              class="timeline-item"
            >
              <span class="timeline-node">
                <component
                  :is="exp.type === 'work' ? Briefcase : GraduationCap"
                  :size="14"
                  :stroke-width="1.5"
                />
              </span>
              <div class="timeline-content">
                <span class="timeline-period font-mono">{{ exp.period }}</span>
                <h4 class="timeline-title">{{ exp.title }}</h4>
                <p class="timeline-org">{{ exp.organization }}</p>
                <p class="timeline-desc">{{ exp.description }}</p>
              </div>
            </li>
          </ol>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.skills-head {
  margin-bottom: 3.5rem;
}

.skills-grid {
  display: grid;
  grid-template-columns: 0.9fr 1.1fr;
  gap: clamp(2rem, 5vw, 4rem);
  align-items: start;
}

.block-title {
  font-size: var(--fs-h3);
  font-weight: 500;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border);
}

/* —— 技能标签云 —— */
.cloud-groups {
  display: flex;
  flex-direction: column;
  gap: 1.75rem;
}

.cloud-cat {
  display: flex;
  align-items: baseline;
  gap: 0.75rem;
  margin-bottom: 0.875rem;
}

.cat-cn {
  font-family: var(--font-serif);
  font-size: 1.0625rem;
  font-weight: 500;
  color: var(--text);
}

.cat-en {
  font-size: var(--fs-tiny);
  letter-spacing: 0.15em;
  text-transform: uppercase;
  color: var(--text-faint);
}

.cloud-items {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.cloud-tag {
  padding: 0.4375rem 0.875rem;
  font-size: var(--fs-small);
  color: var(--text-dim);
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  transition: all var(--dur) var(--ease);
}

.cloud-tag:hover {
  color: var(--accent);
  border-color: var(--accent);
  background: rgba(212, 165, 116, 0.06);
  transform: translateY(-2px);
}

/* —— 时间轴 —— */
.timeline {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 2rem;
  padding-left: 0.5rem;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 0.875rem;
  top: 0.5rem;
  bottom: 0.5rem;
  width: 1px;
  background: linear-gradient(180deg, var(--border), transparent);
}

.timeline-item {
  position: relative;
  display: flex;
  gap: 1.5rem;
}

.timeline-node {
  position: relative;
  z-index: 1;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  width: 1.75rem;
  height: 1.75rem;
  color: var(--accent);
  background: var(--bg);
  border: 1px solid var(--accent);
  border-radius: 50%;
  margin-top: 0.125rem;
}

.timeline-content {
  flex: 1;
  padding-bottom: 0.5rem;
}

.timeline-period {
  display: inline-block;
  font-size: var(--fs-tiny);
  letter-spacing: 0.1em;
  color: var(--accent);
  margin-bottom: 0.5rem;
}

.timeline-title {
  font-family: var(--font-serif);
  font-size: 1.125rem;
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.timeline-org {
  font-size: var(--fs-small);
  color: var(--celadon);
  margin-bottom: 0.625rem;
}

.timeline-desc {
  font-size: var(--fs-small);
  color: var(--text-dim);
  line-height: 1.8;
}

@media (max-width: 1024px) {
  .skills-grid {
    grid-template-columns: 1fr;
    gap: 3rem;
  }
}
</style>

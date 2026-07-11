<script setup lang="ts">
import { usePortfolioStore } from '@/composables/usePortfolioStore'

const props = defineProps<{ templateId: string }>()
const store = usePortfolioStore(props.templateId)

const addExp = () => {
  store.experiences.unshift({
    id: 'e' + Date.now(),
    period: '2025 — 至今',
    title: '新职位',
    organization: '机构',
    description: '',
    type: 'work',
  })
}
const removeExp = (i: number) => store.experiences.splice(i, 1)
const move = (i: number, dir: -1 | 1) => {
  const j = i + dir
  if (j < 0 || j >= store.experiences.length) return
  const arr = store.experiences
  ;[arr[i], arr[j]] = [arr[j], arr[i]]
}
</script>

<template>
  <div class="ef-panel">
    <div class="ef-section-head">
      <h4>经历时间轴</h4>
      <button class="ef-add-btn" type="button" @click="addExp">+ 添加经历</button>
    </div>

    <details v-for="(exp, i) in store.experiences" :key="exp.id" class="ef-item">
      <summary>
        <span class="ef-item-title">{{ exp.title || '未命名' }}</span>
        <span class="ef-item-meta">{{ exp.period }}</span>
      </summary>
      <div class="ef-item-body">
        <div class="ef-row-two">
          <label class="ef-field"><span>时间段</span><input v-model="exp.period" class="ef-input" type="text" /></label>
          <label class="ef-field"><span>类型</span>
            <select v-model="exp.type" class="ef-select">
              <option value="work">工作</option>
              <option value="education">教育</option>
            </select>
          </label>
        </div>
        <div class="ef-row-two">
          <label class="ef-field"><span>职位 / 学位</span><input v-model="exp.title" class="ef-input" type="text" /></label>
          <label class="ef-field"><span>机构</span><input v-model="exp.organization" class="ef-input" type="text" /></label>
        </div>
        <label class="ef-field"><span>描述</span><textarea v-model="exp.description" class="ef-textarea"></textarea></label>
        <div class="ef-item-actions">
          <button class="ef-move-btn" type="button" @click="move(i, -1)">上移</button>
          <button class="ef-move-btn" type="button" @click="move(i, 1)">下移</button>
          <button class="ef-remove-btn" type="button" @click="removeExp(i)">删除</button>
        </div>
      </div>
    </details>

    <p v-if="!store.experiences.length" class="ef-empty">暂无经历，点击右上角添加</p>
  </div>
</template>

<script setup lang="ts">
import { usePortfolioStore } from '@/composables/usePortfolioStore'

const props = defineProps<{ templateId: string }>()
const store = usePortfolioStore(props.templateId)

const addSkill = () => {
  store.skills.push({
    id: 's' + Date.now(),
    category: '新分类',
    categoryEn: 'New',
    items: ['技能1'],
  })
}
const removeSkill = (i: number) => store.skills.splice(i, 1)
const move = (i: number, dir: -1 | 1) => {
  const j = i + dir
  if (j < 0 || j >= store.skills.length) return
  const arr = store.skills
  ;[arr[i], arr[j]] = [arr[j], arr[i]]
}
/** items 数组 ↔ 逗号分隔字符串 */
const itemsText = (i: number) => store.skills[i].items.join('、')
const setItems = (i: number, val: string) => {
  store.skills[i].items = val
    .split(/[、,，]/)
    .map((s) => s.trim())
    .filter(Boolean)
}
</script>

<template>
  <div class="ef-panel">
    <div class="ef-section-head">
      <h4>技能分类</h4>
      <button class="ef-add-btn" type="button" @click="addSkill">+ 添加分类</button>
    </div>

    <details v-for="(skill, i) in store.skills" :key="skill.id" class="ef-item" open>
      <summary>
        <span class="ef-item-title">{{ skill.category || '未命名' }}</span>
        <span class="ef-item-meta">{{ skill.items.length }} 项</span>
      </summary>
      <div class="ef-item-body">
        <div class="ef-row-two">
          <label class="ef-field"><span>分类（中文）</span><input v-model="skill.category" class="ef-input" type="text" /></label>
          <label class="ef-field"><span>分类（英文）</span><input v-model="skill.categoryEn" class="ef-input" type="text" /></label>
        </div>
        <label class="ef-field"><span>技能项（用、或 , 分隔）</span><input :value="itemsText(i)" class="ef-input" type="text" @input="setItems(i, ($event.target as HTMLInputElement).value)" /></label>
        <div class="ef-item-actions">
          <button class="ef-move-btn" type="button" @click="move(i, -1)">上移</button>
          <button class="ef-move-btn" type="button" @click="move(i, 1)">下移</button>
          <button class="ef-remove-btn" type="button" @click="removeSkill(i)">删除</button>
        </div>
      </div>
    </details>

    <p v-if="!store.skills.length" class="ef-empty">暂无技能分类，点击右上角添加</p>
  </div>
</template>

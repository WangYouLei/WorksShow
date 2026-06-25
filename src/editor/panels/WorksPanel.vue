<script setup lang="ts">
import { usePortfolioStore } from '@/composables/usePortfolioStore'

const props = defineProps<{ templateId: string }>()
const store = usePortfolioStore(props.templateId)

const addWork = () => {
  store.works.unshift({
    id: 'w' + Date.now(),
    title: '新作品',
    category: '分类',
    cover: '',
    description: '',
    year: String(new Date().getFullYear()),
    tags: [],
    highlight: false,
  })
}
const removeWork = (i: number) => store.works.splice(i, 1)
const move = (i: number, dir: -1 | 1) => {
  const j = i + dir
  if (j < 0 || j >= store.works.length) return
  const arr = store.works
  ;[arr[i], arr[j]] = [arr[j], arr[i]]
}
const tagsText = (i: number) => store.works[i].tags.join('、')
const setTags = (i: number, val: string) => {
  store.works[i].tags = val
    .split(/[、,，]/)
    .map((s) => s.trim())
    .filter(Boolean)
}
</script>

<template>
  <div class="ef-panel">
    <div class="ef-section-head">
      <h4>作品列表</h4>
      <button class="ef-add-btn" type="button" @click="addWork">+ 添加作品</button>
    </div>

    <details v-for="(work, i) in store.works" :key="work.id" class="ef-item">
      <summary>
        <span class="ef-item-title">{{ work.title || '未命名' }}</span>
        <span class="ef-item-meta">{{ work.category }} · {{ work.year }}</span>
      </summary>
      <div class="ef-item-body">
        <div class="ef-row-two">
          <label class="ef-field"><span>标题</span><input v-model="work.title" class="ef-input" type="text" /></label>
          <label class="ef-field"><span>分类</span><input v-model="work.category" class="ef-input" type="text" /></label>
        </div>
        <label class="ef-field"><span>封面图 URL</span><input v-model="work.cover" class="ef-input" type="text" /></label>
        <label class="ef-field"><span>描述</span><textarea v-model="work.description" class="ef-textarea"></textarea></label>
        <div class="ef-row-two">
          <label class="ef-field"><span>年份</span><input v-model="work.year" class="ef-input" type="text" /></label>
          <label class="ef-field"><span>外链（可选）</span><input v-model="work.link" class="ef-input" type="text" /></label>
        </div>
        <label class="ef-field"><span>标签（用、或 , 分隔）</span><input :value="tagsText(i)" class="ef-input" type="text" @input="setTags(i, ($event.target as HTMLInputElement).value)" /></label>
        <label class="ef-checkbox"><input v-model="work.highlight" type="checkbox" /> 标记为高亮（占双列宽）</label>
        <div class="ef-item-actions">
          <button class="ef-move-btn" type="button" @click="move(i, -1)">上移</button>
          <button class="ef-move-btn" type="button" @click="move(i, 1)">下移</button>
          <button class="ef-remove-btn" type="button" @click="removeWork(i)">删除</button>
        </div>
      </div>
    </details>

    <p v-if="!store.works.length" class="ef-empty">暂无作品，点击右上角添加</p>
  </div>
</template>

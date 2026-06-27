<script setup lang="ts">
import { usePortfolioStore } from '@/composables/usePortfolioStore'

const props = defineProps<{ templateId: string }>()
const store = usePortfolioStore(props.templateId)

const addStat = () => {
  store.profile.stats.push({ label: '新统计', value: '0' })
}
const removeStat = (i: number) => store.profile.stats.splice(i, 1)

const addSocial = () => {
  store.profile.socials.push({ name: '新链接', url: 'https://', icon: 'mail' })
}
const removeSocial = (i: number) => store.profile.socials.splice(i, 1)
</script>

<template>
  <div class="ef-panel">
    <div class="ef-section">
      <h4 style="margin:0 0 14px;font-size:13px;color:#d4a574;font-weight:600;letter-spacing:1px">基本信息</h4>
      <label class="ef-field"><span>姓名</span><input v-model="store.profile.name" class="ef-input" type="text" /></label>
      <label class="ef-field"><span>英文名</span><input v-model="store.profile.nameEn" class="ef-input" type="text" /></label>
      <label class="ef-field"><span>职位 / 方向</span><input v-model="store.profile.title" class="ef-input" type="text" /></label>
      <label class="ef-field"><span>标语</span><input v-model="store.profile.tagline" class="ef-input" type="text" /></label>
      <label class="ef-field"><span>个人简介</span><textarea v-model="store.profile.bio" class="ef-textarea"></textarea></label>
      <div class="ef-row-two">
        <label class="ef-field"><span>邮箱</span><input v-model="store.profile.email" class="ef-input" type="text" /></label>
        <label class="ef-field"><span>所在地</span><input v-model="store.profile.location" class="ef-input" type="text" /></label>
      </div>
      <label class="ef-field"><span>状态（如：寻找实习机会）</span><input v-model="store.profile.availability" class="ef-input" type="text" /></label>
    </div>

    <div class="ef-section">
      <div class="ef-section-head">
        <h4>数据统计</h4>
        <button class="ef-add-btn" type="button" @click="addStat">+ 添加</button>
      </div>
      <div v-for="(s, i) in store.profile.stats" :key="i" class="ef-row">
        <input v-model="s.label" class="ef-input" type="text" placeholder="标签（如：项目交付）" />
        <input v-model="s.value" class="ef-input" type="text" placeholder="值（如：60+）" />
        <button class="ef-remove-btn" type="button" @click="removeStat(i)">删除</button>
      </div>
      <p v-if="!store.profile.stats.length" class="ef-empty">暂无统计项</p>
    </div>

    <div class="ef-section">
      <div class="ef-section-head">
        <h4>社交链接</h4>
        <button class="ef-add-btn" type="button" @click="addSocial">+ 添加</button>
      </div>
      <div v-for="(s, i) in store.profile.socials" :key="i" class="ef-row">
        <input v-model="s.name" class="ef-input" type="text" placeholder="名称" />
        <input v-model="s.url" class="ef-input" type="text" placeholder="链接" />
        <select v-model="s.icon" class="ef-select" style="width:auto">
          <option value="github">github</option>
          <option value="mail">mail</option>
          <option value="custom">自定义</option>
        </select>
        <button class="ef-remove-btn" type="button" @click="removeSocial(i)">删除</button>
      </div>
      <p v-if="!store.profile.socials.length" class="ef-empty">暂无社交链接</p>
    </div>
  </div>
</template>

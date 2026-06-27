<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { getCareerIntention, saveCareerIntention } from '@/api/career'
import type { CareerIntention } from '@/api/career'

/**
 * 求职意向编辑面板
 * <p>
 * 数据为用户级别(不绑定具体模板/简历),独立于 usePortfolioStore。
 * - 挂载时从服务端拉取,有则填充,无则使用默认空值
 * - 深度 watch + 800ms 防抖,自动调用 PUT /career-intention upsert
 */
const defaultData = (): CareerIntention => ({
  jobStatus: 0,
  expectedPosition: '',
  positionType: '',
  expectedIndustry: [],
  expectedCity: [],
  workType: 0,
  salaryMin: null,
  salaryMax: null,
  arrivalTime: '',
  selfEvaluation: '',
})

const form = reactive<CareerIntention>(defaultData())
const loading = ref(true)
const saving = ref(false)
const lastSaved = ref('')

// 期望行业/城市用逗号分隔输入,内部仍以数组存储
const industryText = ref('')
const cityText = ref('')

// 求职状态选项
const jobStatusOptions = [
  { value: 0, label: '离职-随时到岗' },
  { value: 1, label: '在职-暂未到岗' },
  { value: 2, label: '在职-月内到岗' },
]

// 工作性质选项
const workTypeOptions = [
  { value: 0, label: '全职' },
  { value: 1, label: '兼职' },
  { value: 2, label: '实习' },
]

const syncIndustryToForm = () => {
  form.expectedIndustry = industryText.value
    .split(/[,，]/)
    .map((s) => s.trim())
    .filter(Boolean)
}
const syncCityToForm = () => {
  form.expectedCity = cityText.value
    .split(/[,，]/)
    .map((s) => s.trim())
    .filter(Boolean)
}

const load = async () => {
  loading.value = true
  try {
    const data = await getCareerIntention()
    if (data) {
      Object.assign(form, data)
      industryText.value = form.expectedIndustry.join(', ')
      cityText.value = form.expectedCity.join(', ')
    }
  } catch (e) {
    console.warn('[career] 加载求职意向失败', e)
  } finally {
    loading.value = false
  }
}

const save = async () => {
  if (saving.value) return
  saving.value = true
  try {
    await saveCareerIntention(form)
    lastSaved.value = new Date().toLocaleTimeString()
  } catch (e) {
    console.error('[career] 保存求职意向失败', e)
  } finally {
    saving.value = false
  }
}

onMounted(load)

// 深度监听,800ms 防抖保存
let saveTimer: ReturnType<typeof setTimeout> | null = null
watch(
  form,
  () => {
    if (loading.value) return
    if (saveTimer) clearTimeout(saveTimer)
    saveTimer = setTimeout(save, 800)
  },
  { deep: true },
)
</script>

<template>
  <div class="ef-panel">
    <p v-if="loading" class="ef-empty">加载中…</p>

    <template v-else>
      <div class="ef-section">
        <h4 style="margin:0 0 14px;font-size:13px;color:#d4a574;font-weight:600;letter-spacing:1px">求职意向</h4>
        <label class="ef-field">
          <span>求职状态</span>
          <select v-model.number="form.jobStatus" class="ef-select">
            <option v-for="opt in jobStatusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </label>
        <label class="ef-field">
          <span>期望职位</span>
          <input v-model="form.expectedPosition" class="ef-input" type="text" placeholder="如:前端工程师" />
        </label>
        <label class="ef-field">
          <span>职位类型</span>
          <input v-model="form.positionType" class="ef-input" type="text" placeholder="如:技术/研发" />
        </label>
        <label class="ef-field">
          <span>期望行业(逗号分隔)</span>
          <input v-model="industryText" class="ef-input" type="text" placeholder="如:互联网, 金融" @blur="syncIndustryToForm" />
        </label>
        <label class="ef-field">
          <span>期望城市(逗号分隔)</span>
          <input v-model="cityText" class="ef-input" type="text" placeholder="如:北京, 上海" @blur="syncCityToForm" />
        </label>
        <label class="ef-field">
          <span>工作性质</span>
          <select v-model.number="form.workType" class="ef-select">
            <option v-for="opt in workTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </label>
        <div class="ef-row-two">
          <label class="ef-field">
            <span>期望薪资下限(元)</span>
            <input v-model.number="form.salaryMin" class="ef-input" type="number" min="0" placeholder="如:15000" />
          </label>
          <label class="ef-field">
            <span>期望薪资上限(元)</span>
            <input v-model.number="form.salaryMax" class="ef-input" type="number" min="0" placeholder="如:25000" />
          </label>
        </div>
        <label class="ef-field">
          <span>到岗时间</span>
          <input v-model="form.arrivalTime" class="ef-input" type="date" />
        </label>
      </div>

      <div class="ef-section">
        <h4 style="margin:0 0 14px;font-size:13px;color:#d4a574;font-weight:600;letter-spacing:1px">自我评价</h4>
        <label class="ef-field">
          <textarea v-model="form.selfEvaluation" class="ef-textarea" placeholder="简要描述你的核心能力、项目经验与职业优势"></textarea>
        </label>
      </div>

      <div class="career-status">
        <span v-if="saving" class="status-saving">保存中…</span>
        <span v-else-if="lastSaved" class="status-saved">已保存 · {{ lastSaved }}</span>
      </div>
    </template>
  </div>
</template>

<style scoped>
.career-status {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid rgba(212, 165, 116, 0.1);
  font-size: 12px;
  text-align: right;
}
.status-saved {
  color: rgba(212, 165, 116, 0.6);
}
.status-saving {
  color: rgba(245, 241, 232, 0.5);
}
</style>

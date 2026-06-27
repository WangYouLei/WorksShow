<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getTemplateMeta } from '@/templates/registry'
import { usePortfolioStore, resetPortfolioStore, getSaveStatus, saveNow } from '@/composables/usePortfolioStore'
import { downloadPortfolioHtml } from '@/composables/useExporter'
import PreviewFrame from './PreviewFrame.vue'
import ProfilePanel from './panels/ProfilePanel.vue'
import WorksPanel from './panels/WorksPanel.vue'
import SkillsPanel from './panels/SkillsPanel.vue'
import ExperiencesPanel from './panels/ExperiencesPanel.vue'
import CareerIntentionPanel from './panels/CareerIntentionPanel.vue'

const props = defineProps<{ templateId: string }>()
const router = useRouter()
const meta = computed(() => getTemplateMeta(props.templateId))
const store = usePortfolioStore(props.templateId)
const saveStatus = getSaveStatus(props.templateId)

// 保存状态文案
const saveStatusText = computed(() => {
  if (saveStatus.saving) return '保存中…'
  if (saveStatus.saveError) return '保存失败,请重试'
  if (saveStatus.lastSaved) {
    const t = new Date(saveStatus.lastSaved)
    return `已保存 · ${t.getHours().toString().padStart(2, '0')}:${t.getMinutes().toString().padStart(2, '0')}:${t.getSeconds().toString().padStart(2, '0')}`
  }
  return ''
})

const onManualSave = async () => {
  await saveNow(props.templateId)
}

const tabs = [
  { id: 'profile', label: '个人信息' },
  { id: 'works', label: '作品' },
  { id: 'skills', label: '技能' },
  { id: 'experiences', label: '经历' },
  { id: 'career', label: '求职意向' },
] as const
const activeTab = ref<typeof tabs[number]['id']>('profile')

const onReset = () => {
  if (confirm('确定重置为模板默认数据？这将覆盖你的所有简历内容（所有模板共享同一份档案）。')) {
    resetPortfolioStore(props.templateId)
  }
}

const exporting = ref(false)
const onExport = async () => {
  if (exporting.value) return
  exporting.value = true
  try {
    await downloadPortfolioHtml(store, props.templateId)
  } catch (e) {
    console.error(e)
    alert('导出失败：' + (e as Error).message)
  } finally {
    exporting.value = false
  }
}
</script>

<template>
  <div class="editor">
    <header class="editor-topbar">
      <button class="back-btn" type="button" @click="router.push('/')">
        ← 返回模板
      </button>
      <div class="topbar-title">
        <span v-if="meta" class="title-name">{{ meta.name }}</span>
        <span v-else class="title-name">模板未找到</span>
      </div>
      <div class="topbar-actions">
        <span v-if="saveStatusText" class="save-status" :class="{ 'is-error': saveStatus.saveError, 'is-saving': saveStatus.saving }">{{ saveStatusText }}</span>
        <button class="save-btn" type="button" :disabled="saveStatus.saving || !saveStatus.loaded" @click="onManualSave">
          <span v-if="saveStatus.saving" class="save-btn-spinner" aria-hidden="true"></span>
          {{ saveStatus.saving ? '保存中…' : '保存' }}
        </button>
        <button class="account-btn" type="button" @click="router.push('/account')">账号管理</button>
        <button class="reset-btn" type="button" @click="onReset">重置</button>
        <button class="export-btn" type="button" :disabled="exporting" @click="onExport">
          {{ exporting ? '导出中…' : '下载 HTML' }}
        </button>
      </div>
    </header>

    <div class="editor-body">
      <aside class="editor-form">
        <div class="ef-tabs">
          <button
            v-for="t in tabs"
            :key="t.id"
            class="ef-tab"
            :class="{ 'is-active': activeTab === t.id }"
            type="button"
            @click="activeTab = t.id"
          >
            {{ t.label }}
          </button>
        </div>
        <div class="editor-form-content">
          <ProfilePanel v-if="activeTab === 'profile'" :template-id="props.templateId" />
          <WorksPanel v-else-if="activeTab === 'works'" :template-id="props.templateId" />
          <SkillsPanel v-else-if="activeTab === 'skills'" :template-id="props.templateId" />
          <ExperiencesPanel v-else-if="activeTab === 'experiences'" :template-id="props.templateId" />
          <CareerIntentionPanel v-else />
        </div>
      </aside>
      <section class="editor-preview">
        <PreviewFrame :data="store" :template-id="props.templateId" />
      </section>
    </div>
  </div>
</template>

<style scoped>
.editor {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #0a0a0f;
  color: #f5f1e8;
}
.editor-topbar {
  height: 56px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: rgba(255, 255, 255, 0.03);
  border-bottom: 1px solid rgba(212, 165, 116, 0.15);
}
.back-btn {
  padding: 6px 12px;
  font-size: 13px;
  color: rgba(245, 241, 232, 0.7);
  background: transparent;
  border: 1px solid rgba(245, 241, 232, 0.2);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.back-btn:hover {
  color: #f5f1e8;
  border-color: #d4a574;
}
.topbar-title {
  font-size: 14px;
  color: rgba(245, 241, 232, 0.5);
}
.title-name {
  color: #d4a574;
  font-weight: 600;
}
.topbar-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
.save-status {
  font-size: 12px;
  color: rgba(245, 241, 232, 0.4);
  white-space: nowrap;
}
.save-status.is-saving {
  color: #d4a574;
}
.save-status.is-error {
  color: #c8553d;
}
.save-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  font-size: 13px;
  color: #0a0a0f;
  background: #d4a574;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s ease;
}
.save-btn-spinner {
  width: 12px;
  height: 12px;
  border: 2px solid rgba(10, 10, 15, 0.3);
  border-top-color: #0a0a0f;
  border-radius: 50%;
  animation: save-btn-spin 0.6s linear infinite;
}
@keyframes save-btn-spin {
  to {
    transform: rotate(360deg);
  }
}
.save-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.save-btn:not(:disabled):hover {
  background: #e3b885;
}
.account-btn {
  padding: 6px 12px;
  font-size: 13px;
  color: #d4a574;
  background: transparent;
  border: 1px solid rgba(212, 165, 116, 0.3);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.account-btn:hover {
  background: rgba(212, 165, 116, 0.1);
  border-color: #d4a574;
}
.reset-btn {
  padding: 8px 14px;
  font-size: 13px;
  color: rgba(245, 241, 232, 0.7);
  background: transparent;
  border: 1px solid rgba(245, 241, 232, 0.2);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.reset-btn:hover {
  color: #c8553d;
  border-color: #c8553d;
}
.export-btn {
  padding: 8px 18px;
  font-size: 13px;
  color: #0a0a0f;
  background: #d4a574;
  border: none;
  border-radius: 4px;
  cursor: not-allowed;
  opacity: 0.5;
}
.export-btn:not(:disabled) {
  cursor: pointer;
  opacity: 1;
  transition: background 0.2s ease;
}
.export-btn:not(:disabled):hover {
  background: #e3b885;
}
.editor-body {
  flex: 1;
  display: grid;
  grid-template-columns: 380px 1fr;
  overflow: hidden;
}
.editor-form {
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(212, 165, 116, 0.15);
  background: rgba(255, 255, 255, 0.02);
  overflow: hidden;
}
.editor-form-content {
  flex: 1;
  overflow-y: auto;
}
.editor-preview {
  background: #0a0a0f;
  overflow: hidden;
  position: relative;
}
</style>

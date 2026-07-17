<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import {
  getEdgeOneConfig,
  saveEdgeOneConfig,
  deleteEdgeOneConfig,
  type EdgeOneConfigVO,
} from '@/api/edgeoneConfig'

const config = ref<EdgeOneConfigVO | null>(null)
const loading = ref(false)
const error = ref('')
const success = ref('')
const form = reactive({
  apiToken: '',
  projectName: '',
})

const loadConfig = async () => {
  loading.value = true
  error.value = ''
  try {
    config.value = await getEdgeOneConfig()
    form.projectName = config.value?.projectName ?? ''
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

const onSave = async () => {
  // 首次配置必须填写 apiToken;更新时 apiToken 可留空(仅更新 projectName)
  const isFirstTime = !config.value
  if (isFirstTime && !form.apiToken.trim()) {
    error.value = '首次配置必须填写 API Token'
    return
  }
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    await saveEdgeOneConfig({
      // apiToken 留空时传 undefined,后端仅更新 projectName
      apiToken: form.apiToken.trim() || undefined,
      projectName: form.projectName.trim() || undefined,
    })
    form.apiToken = ''
    success.value = '保存成功'
    await loadConfig()
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

const onDelete = async () => {
  if (!confirm('确定删除 EdgeOne 配置?删除后将无法部署。')) return
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    await deleteEdgeOneConfig()
    config.value = null
    form.apiToken = ''
    form.projectName = ''
    success.value = '已删除'
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

onMounted(loadConfig)
</script>

<template>
  <div class="edgeone-config">
    <!-- 已配置状态提示 -->
    <div v-if="config" class="config-status">
      <p class="status-line">当前 Token: <span class="token-masked">{{ config.apiTokenMasked }}</span></p>
      <p v-if="config.projectName" class="status-line">默认项目名: <span class="project-name">{{ config.projectName }}</span></p>
      <button type="button" class="delete-btn" :disabled="loading" @click="onDelete">删除配置</button>
    </div>
    <div v-else class="config-status">
      <p class="status-line hint">尚未配置,请填写下方信息。API Token 可在 EdgeOne 控制台「API Token」页创建。</p>
    </div>

    <form class="form" @submit.prevent="onSave">
      <label class="field">
        <span class="field-label">API Token{{ config ? '(留空则不修改,填写则更新)' : '' }}</span>
        <input v-model="form.apiToken" type="password" class="field-input" placeholder="请输入 EdgeOne Pages API Token" autocomplete="off" />
      </label>
      <label class="field">
        <span class="field-label">默认项目名(可选)</span>
        <input v-model="form.projectName" type="text" class="field-input" placeholder="部署时的默认 EdgeOne 项目名" />
      </label>

      <p v-if="error" class="msg-error">{{ error }}</p>
      <p v-if="success" class="msg-success">{{ success }}</p>

      <button type="submit" class="submit-btn" :disabled="loading">
        {{ loading ? '保存中…' : (config ? '更新配置' : '保存配置') }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.edgeone-config {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.config-status {
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(212, 165, 116, 0.15);
  border-radius: 6px;
}
.status-line {
  margin: 0 0 6px;
  font-size: 13px;
  color: rgba(245, 241, 232, 0.7);
}
.status-line:last-child {
  margin-bottom: 0;
}
.status-line.hint {
  line-height: 1.6;
}
.token-masked {
  color: #d4a574;
  font-family: monospace;
}
.project-name {
  color: #f5f1e8;
}
.delete-btn {
  margin-top: 10px;
  padding: 6px 12px;
  font-size: 12px;
  color: #c8553d;
  background: transparent;
  border: 1px solid rgba(200, 85, 61, 0.4);
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}
.delete-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.delete-btn:not(:disabled):hover {
  background: rgba(200, 85, 61, 0.1);
}
.form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.field-label {
  font-size: 13px;
  color: rgba(245, 241, 232, 0.7);
}
.field-input {
  padding: 10px 12px;
  font-size: 14px;
  color: #f5f1e8;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(245, 241, 232, 0.15);
  border-radius: 6px;
  outline: none;
  transition: border-color 0.2s;
}
.field-input:focus {
  border-color: #d4a574;
}
.msg-error {
  margin: 0;
  padding: 8px 12px;
  font-size: 13px;
  color: #c8553d;
  background: rgba(200, 85, 61, 0.1);
  border-radius: 4px;
}
.msg-success {
  margin: 0;
  padding: 8px 12px;
  font-size: 13px;
  color: #6abf69;
  background: rgba(106, 191, 105, 0.1);
  border-radius: 4px;
}
.submit-btn {
  margin-top: 6px;
  padding: 11px;
  font-size: 14px;
  color: #0a0a0f;
  background: #d4a574;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}
.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.submit-btn:not(:disabled):hover {
  background: #e3b885;
}
</style>

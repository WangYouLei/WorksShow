<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { usePortfolioStore, ensurePortfolioId } from '@/composables/usePortfolioStore'
import { getTemplateExporter } from '@/templates/registry'
import {
  deploy,
  DEPLOY_STATUS,
  type DeploymentVO,
} from '@/api/deployment'
import { getEdgeOneConfig, type EdgeOneConfigVO } from '@/api/edgeoneConfig'

const props = defineProps<{
  modelValue: boolean
  templateId: string
}>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const router = useRouter()

const loading = ref(false)
const deploying = ref(false)
const config = ref<EdgeOneConfigVO | null>(null)
const result = ref<DeploymentVO | null>(null)
const error = ref('')
const copied = ref(false)
const form = reactive({
  projectName: '',
  description: '',
})

// 状态派生:四个互斥视图
const showForm = computed(() => !loading.value && !deploying.value && !result.value && !error.value)
const showDeploying = computed(() => deploying.value)
const showSuccess = computed(() => !!result.value && result.value.status === DEPLOY_STATUS.SUCCESS && !error.value)
const showError = computed(() => !!error.value && !deploying.value)

const close = () => {
  emit('update:modelValue', false)
}

const goConfig = () => {
  close()
  router.push({ path: '/account', query: { tab: 'edgeone' } })
}

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

const onDeploy = async () => {
  error.value = ''
  result.value = null

  // 1. 确认 EdgeOne 已配置
  if (!config.value) {
    error.value = '请先配置 EdgeOne API Token'
    return
  }

  // 2. 获取 portfolioId(确保简历实例已创建)
  const portfolioId = await ensurePortfolioId(props.templateId)
  if (!portfolioId) {
    error.value = '简历实例尚未创建完成,请稍后重试或刷新页面'
    return
  }

  // 3. 生成完整 HTML(复用导出逻辑)
  const exporter = getTemplateExporter(props.templateId)
  if (!exporter) {
    error.value = `模板 ${props.templateId} 未注册导出函数`
    return
  }

  deploying.value = true
  try {
    const html = await exporter(usePortfolioStore(props.templateId))
    // 4. 调用部署 API(同步,后端 CLI 执行)
    const res = await deploy({
      portfolioId,
      html,
      projectName: form.projectName.trim() || undefined,
      description: form.description.trim() || undefined,
    })
    result.value = res
    if (res.status !== DEPLOY_STATUS.SUCCESS) {
      error.value = res.errorMessage || '部署失败,未知原因'
    }
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    deploying.value = false
  }
}

const copyUrl = async () => {
  if (!result.value?.deployUrl) return
  try {
    await navigator.clipboard.writeText(result.value.deployUrl)
    copied.value = true
    setTimeout(() => (copied.value = false), 2000)
  } catch {
    // 剪贴板失败时提示用户手动复制
    error.value = '复制失败,请手动选中链接复制'
  }
}

const resetToForm = () => {
  error.value = ''
  result.value = null
}

// 弹窗打开时加载配置,关闭时重置
watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      result.value = null
      error.value = ''
      copied.value = false
      form.description = ''
      loadConfig()
    }
  },
)
</script>

<template>
  <Teleport to="body">
    <div v-if="modelValue" class="deploy-overlay" @click.self="close">
      <div class="deploy-dialog">
        <!-- 头部 -->
        <div class="dialog-header">
          <h2 class="dialog-title">部署到 EdgeOne Pages</h2>
          <button class="close-btn" type="button" @click="close">×</button>
        </div>

        <div class="dialog-body">
          <!-- 加载配置中 -->
          <div v-if="loading" class="state-block">
            <p class="state-text">加载配置中…</p>
          </div>

          <!-- 部署中 -->
          <div v-else-if="showDeploying" class="state-block">
            <div class="spinner" aria-hidden="true"></div>
            <p class="state-text">正在部署,请耐心等待(通常 10-60 秒)…</p>
          </div>

          <!-- 部署成功 -->
          <div v-else-if="showSuccess" class="state-block">
            <p class="success-text">✓ 部署成功</p>
            <p class="state-hint">访问链接(含访问凭证,请完整保存):</p>
            <div class="url-box">
              <input :value="result?.deployUrl ?? ''" readonly class="url-input" />
              <button type="button" class="copy-btn" @click="copyUrl">
                {{ copied ? '已复制' : '复制' }}
              </button>
            </div>
            <div class="action-row">
              <a
                v-if="result?.deployUrl"
                :href="result.deployUrl"
                target="_blank"
                rel="noopener"
                class="btn-primary"
              >访问页面</a>
              <button type="button" class="btn-secondary" @click="close">关闭</button>
            </div>
          </div>

          <!-- 部署失败 -->
          <div v-else-if="showError" class="state-block">
            <p class="error-text">✗ 部署失败</p>
            <p class="error-detail">{{ error }}</p>
            <div class="action-row">
              <button type="button" class="btn-primary" @click="resetToForm">重试</button>
              <button type="button" class="btn-secondary" @click="close">关闭</button>
            </div>
          </div>

          <!-- 未配置 EdgeOne -->
          <div v-else-if="!config" class="state-block">
            <p class="state-text">尚未配置 EdgeOne API Token</p>
            <p class="state-hint">部署前需要先配置 EdgeOne 凭证,请前往「账号管理 → 部署配置」。</p>
            <button type="button" class="btn-primary" @click="goConfig">前往配置</button>
          </div>

          <!-- 部署表单 -->
          <form v-else class="deploy-form" @submit.prevent="onDeploy">
            <div class="config-info">
              当前 Token: <span class="token-masked">{{ config.apiTokenMasked }}</span>
            </div>
            <label class="field">
              <span class="field-label">项目名(留空则用默认)</span>
              <input
                v-model="form.projectName"
                type="text"
                class="field-input"
                placeholder="EdgeOne Pages 项目名"
              />
            </label>
            <label class="field">
              <span class="field-label">部署描述(可选)</span>
              <textarea
                v-model="form.description"
                rows="2"
                class="field-input"
                placeholder="备注本次部署内容"
              ></textarea>
            </label>
            <div class="action-row">
              <button type="submit" class="btn-primary">开始部署</button>
              <button type="button" class="btn-secondary" @click="close">取消</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.deploy-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(2px);
}
.deploy-dialog {
  width: 92%;
  max-width: 460px;
  max-height: 90vh;
  overflow-y: auto;
  background: #15151f;
  border: 1px solid rgba(212, 165, 116, 0.2);
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}
.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(245, 241, 232, 0.08);
}
.dialog-title {
  margin: 0;
  font-size: 16px;
  color: #f5f1e8;
}
.close-btn {
  font-size: 22px;
  line-height: 1;
  color: rgba(245, 241, 232, 0.5);
  background: none;
  border: none;
  cursor: pointer;
  transition: color 0.2s;
}
.close-btn:hover {
  color: #f5f1e8;
}
.dialog-body {
  padding: 20px;
}
.state-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  padding: 24px 8px;
  text-align: center;
}
.state-text {
  margin: 0;
  font-size: 14px;
  color: rgba(245, 241, 232, 0.8);
}
.state-hint {
  margin: 0;
  font-size: 13px;
  color: rgba(245, 241, 232, 0.5);
  line-height: 1.6;
}
.success-text {
  margin: 0;
  font-size: 16px;
  color: #6abf69;
  font-weight: 600;
}
.error-text {
  margin: 0;
  font-size: 16px;
  color: #c8553d;
  font-weight: 600;
}
.error-detail {
  margin: 0;
  padding: 10px 12px;
  font-size: 13px;
  color: #c8553d;
  background: rgba(200, 85, 61, 0.1);
  border-radius: 6px;
  line-height: 1.6;
  word-break: break-all;
  max-height: 160px;
  overflow-y: auto;
}
.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid rgba(212, 165, 116, 0.2);
  border-top-color: #d4a574;
  border-radius: 50%;
  animation: deploy-spin 0.7s linear infinite;
}
@keyframes deploy-spin {
  to {
    transform: rotate(360deg);
  }
}
.url-box {
  display: flex;
  gap: 8px;
  width: 100%;
}
.url-input {
  flex: 1;
  padding: 8px 10px;
  font-size: 12px;
  color: #f5f1e8;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(245, 241, 232, 0.15);
  border-radius: 6px;
  outline: none;
  font-family: monospace;
}
.copy-btn {
  padding: 8px 14px;
  font-size: 13px;
  color: #d4a574;
  background: rgba(212, 165, 116, 0.1);
  border: 1px solid rgba(212, 165, 116, 0.3);
  border-radius: 6px;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s;
}
.copy-btn:hover {
  background: rgba(212, 165, 116, 0.2);
}
.action-row {
  display: flex;
  gap: 10px;
  justify-content: center;
}
.deploy-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.config-info {
  padding: 10px 12px;
  font-size: 13px;
  color: rgba(245, 241, 232, 0.6);
  background: rgba(255, 255, 255, 0.03);
  border-radius: 6px;
}
.token-masked {
  color: #d4a574;
  font-family: monospace;
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
  font-family: inherit;
}
.field-input:focus {
  border-color: #d4a574;
}
textarea.field-input {
  resize: vertical;
}
.btn-primary {
  padding: 10px 20px;
  font-size: 14px;
  color: #0a0a0f;
  background: #d4a574;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  text-decoration: none;
  transition: background 0.2s;
  display: inline-block;
}
.btn-primary:hover {
  background: #e3b885;
}
.btn-secondary {
  padding: 10px 20px;
  font-size: 14px;
  color: rgba(245, 241, 232, 0.7);
  background: transparent;
  border: 1px solid rgba(245, 241, 232, 0.2);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-secondary:hover {
  color: #f5f1e8;
  border-color: rgba(245, 241, 232, 0.4);
}
</style>

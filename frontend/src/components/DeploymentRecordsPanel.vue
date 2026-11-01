<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  listDeployments,
  deploy,
  offlineDeployment,
  deleteDeployment,
  DEPLOY_STATUS,
  type DeploymentVO,
} from '@/api/deployment'
import { getTemplateExporter } from '@/templates/registry'
import { usePortfolioStore, getSaveStatus } from '@/composables/usePortfolioStore'
import { useThemeStore } from '@/composables/useThemeStore'

const records = ref<DeploymentVO[]>([])
const loading = ref(false)
/** 正在执行操作的记录ID(按钮防重入) */
const busyId = ref<number | null>(null)
const error = ref('')
const success = ref('')

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    records.value = await listDeployments()
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

/** 等待全局简历 store 从服务端加载完成(直接进入本页时 store 可能尚未初始化) */
const waitForStoreLoaded = async (templateId: string) => {
  const store = usePortfolioStore(templateId)
  const status = getSaveStatus(templateId)
  for (let i = 0; i < 50 && !status.loaded; i++) {
    await new Promise((resolve) => setTimeout(resolve, 100))
  }
  if (!status.loaded) {
    throw new Error('简历数据加载超时,请稍后重试')
  }
  return store
}

/**
 * 更新部署:用简历当前内容重新渲染,部署到原项目名。
 * 同一项目重新部署即覆盖更新,线上 URL 不变。
 */
const onUpdate = async (record: DeploymentVO) => {
  const exporter = getTemplateExporter(record.templateId)
  if (!exporter) {
    error.value = `模板 ${record.templateId} 不支持重新渲染,请在编辑器中重新部署`
    return
  }
  busyId.value = record.id
  error.value = ''
  success.value = ''
  try {
    const store = await waitForStoreLoaded(record.templateId)
    const html = await exporter(store, useThemeStore(record.templateId).value)
    const res = await deploy({
      portfolioId: record.portfolioId,
      html,
      deployType: 'edgeone',
      // 历史记录的项目名可能含大写(如 Wang1),部署接口入参校验仅接受小写,统一转换
      projectName: record.projectName.trim().toLowerCase(),
      description: '更新部署',
    })
    if (res.status !== DEPLOY_STATUS.SUCCESS) {
      throw new Error(res.errorMessage || '部署失败,未知原因')
    }
    success.value = '更新成功,线上内容已覆盖(URL 不变)'
    await load()
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    busyId.value = null
  }
}

/** 下线站点:向原项目部署提示页覆盖内容(EdgeOne 无删除项目 API) */
const onOffline = async (record: DeploymentVO) => {
  if (!confirm(`确定下线站点「${record.projectName}」?\n原内容将被"站点已下线"提示页覆盖,URL 保持不变。`)) return
  busyId.value = record.id
  error.value = ''
  success.value = ''
  try {
    const res = await offlineDeployment(record.id)
    if (res.status !== DEPLOY_STATUS.SUCCESS) {
      throw new Error(res.errorMessage || '下线失败,未知原因')
    }
    success.value = '站点已下线,原内容已被提示页覆盖'
    await load()
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    busyId.value = null
  }
}

/** 删除记录:仅删除本地部署记录,不影响线上站点 */
const onDelete = async (record: DeploymentVO) => {
  if (!confirm('确定删除该部署记录?\n仅删除本地记录,线上站点不受影响。')) return
  busyId.value = record.id
  error.value = ''
  success.value = ''
  try {
    await deleteDeployment(record.id)
    success.value = '记录已删除'
    await load()
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    busyId.value = null
  }
}

/** 该记录是否可更新/下线(成功的 EdgeOne 部署) */
const isLiveEdgeOne = (record: DeploymentVO) =>
  record.deployType === 'edgeone' && record.status === DEPLOY_STATUS.SUCCESS

const statusText = (status: number | null) => {
  if (status === DEPLOY_STATUS.SUCCESS) return '成功'
  if (status === DEPLOY_STATUS.FAILED) return '失败'
  return '部署中'
}

onMounted(load)
</script>

<template>
  <div class="deploy-records">
    <!-- 操作说明 -->
    <div class="records-guide">
      <p class="guide-text">
        「更新部署」用简历当前内容覆盖线上页面(URL 不变);「下线站点」以提示页覆盖原内容。
        EdgeOne 暂不提供删除项目的 API,如需彻底删除站点,请前往
        <a
          class="guide-link"
          href="https://console.cloud.tencent.com/edgeone/makers"
          target="_blank"
          rel="noopener"
        >EdgeOne 控制台</a>手动删除。
      </p>
      <ul class="guide-list">
        <li>为什么常要点「更新部署」:线上页面是部署那一刻的快照,之后在编辑器里修改的简历内容不会自动同步,需手动点「更新部署」覆盖到线上。</li>
        <li>若点击「访问」打开失败,说明该链接已过期:点一次「更新部署」重新部署,即可获得当前有效链接。</li>
      </ul>
    </div>

    <p v-if="loading" class="state-text">加载中…</p>
    <p v-else-if="records.length === 0" class="state-text">暂无部署记录</p>

    <p v-if="error" class="msg-error">{{ error }}</p>
    <p v-if="success" class="msg-success">{{ success }}</p>

    <!-- 记录列表 -->
    <div v-if="records.length > 0" class="record-list">
      <div v-for="record in records" :key="record.id" class="record-item">
        <div class="record-head">
          <span class="record-project">{{ record.projectName || '(未命名)' }}</span>
          <span :class="['status-tag', `is-${statusText(record.status) === '成功' ? 'success' : statusText(record.status) === '失败' ? 'failed' : 'deploying'}`]">
            {{ statusText(record.status) }}
          </span>
        </div>
        <div class="record-meta">
          <span>{{ record.deployType === 'local' ? '本服务器' : 'EdgeOne' }}</span>
          <span>模板 {{ record.templateId }}</span>
          <span>{{ record.createTime }}</span>
        </div>
        <a v-if="record.deployUrl && record.status === DEPLOY_STATUS.SUCCESS"
          :href="record.deployUrl" target="_blank" rel="noopener" class="record-url"
        >{{ record.deployUrl }}</a>
        <p v-if="record.description" class="record-desc">{{ record.description }}</p>
        <p v-if="record.status === DEPLOY_STATUS.FAILED && record.errorMessage" class="record-error">
          {{ record.errorMessage }}
        </p>
        <div class="record-actions">
          <a v-if="record.deployUrl && record.status === DEPLOY_STATUS.SUCCESS"
            :href="record.deployUrl" target="_blank" rel="noopener" class="act-btn"
            title="若打开失败,请点击「更新部署」获取最新链接"
          >访问</a>
          <button
            v-if="isLiveEdgeOne(record)"
            type="button"
            class="act-btn primary"
            :disabled="busyId === record.id"
            @click="onUpdate(record)"
          >{{ busyId === record.id ? '部署中…' : '更新部署' }}</button>
          <button
            v-if="isLiveEdgeOne(record)"
            type="button"
            class="act-btn warn"
            :disabled="busyId === record.id"
            @click="onOffline(record)"
          >{{ busyId === record.id ? '处理中…' : '下线站点' }}</button>
          <button
            type="button"
            class="act-btn danger"
            :disabled="busyId === record.id"
            @click="onDelete(record)"
          >删除记录</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.deploy-records {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.records-guide {
  padding: 12px 14px;
  background: rgba(212, 165, 116, 0.06);
  border: 1px dashed rgba(212, 165, 116, 0.35);
  border-radius: 6px;
}
.guide-text {
  margin: 0;
  font-size: 12px;
  line-height: 1.7;
  color: rgba(245, 241, 232, 0.65);
}
.guide-link {
  color: #d4a574;
}
.guide-link:hover {
  text-decoration: underline;
}
.guide-list {
  margin: 8px 0 0;
  padding-left: 18px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.guide-list li {
  font-size: 12px;
  line-height: 1.7;
  color: rgba(245, 241, 232, 0.65);
}
.state-text {
  margin: 0;
  padding: 24px 0;
  font-size: 13px;
  color: rgba(245, 241, 232, 0.5);
  text-align: center;
}
.msg-error {
  margin: 0;
  padding: 8px 12px;
  font-size: 13px;
  color: #c8553d;
  background: rgba(200, 85, 61, 0.1);
  border-radius: 4px;
  word-break: break-all;
}
.msg-success {
  margin: 0;
  padding: 8px 12px;
  font-size: 13px;
  color: #6abf69;
  background: rgba(106, 191, 105, 0.1);
  border-radius: 4px;
}
.record-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.record-item {
  padding: 14px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(245, 241, 232, 0.1);
  border-radius: 6px;
}
.record-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.record-project {
  font-size: 14px;
  font-weight: 600;
  color: #f5f1e8;
  word-break: break-all;
}
.status-tag {
  flex-shrink: 0;
  padding: 2px 8px;
  font-size: 11px;
  border-radius: 10px;
}
.status-tag.is-success {
  color: #6abf69;
  background: rgba(106, 191, 105, 0.12);
}
.status-tag.is-failed {
  color: #c8553d;
  background: rgba(200, 85, 61, 0.12);
}
.status-tag.is-deploying {
  color: #d4a574;
  background: rgba(212, 165, 116, 0.12);
}
.record-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 14px;
  margin-top: 6px;
  font-size: 12px;
  color: rgba(245, 241, 232, 0.5);
}
.record-url {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  color: #d4a574;
  font-family: monospace;
  word-break: break-all;
}
.record-url:hover {
  text-decoration: underline;
}
.record-desc {
  margin: 6px 0 0;
  font-size: 12px;
  color: rgba(245, 241, 232, 0.55);
}
.record-error {
  margin: 6px 0 0;
  font-size: 12px;
  color: rgba(200, 85, 61, 0.9);
  word-break: break-all;
}
.record-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}
.act-btn {
  padding: 6px 12px;
  font-size: 12px;
  color: rgba(245, 241, 232, 0.75);
  background: transparent;
  border: 1px solid rgba(245, 241, 232, 0.2);
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
  transition: all 0.2s;
}
.act-btn:hover {
  color: #f5f1e8;
  border-color: rgba(245, 241, 232, 0.4);
}
.act-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.act-btn.primary {
  color: #d4a574;
  border-color: rgba(212, 165, 116, 0.4);
}
.act-btn.primary:not(:disabled):hover {
  background: rgba(212, 165, 116, 0.12);
}
.act-btn.warn {
  color: #d9a441;
  border-color: rgba(217, 164, 65, 0.4);
}
.act-btn.warn:not(:disabled):hover {
  background: rgba(217, 164, 65, 0.12);
}
.act-btn.danger {
  color: #c8553d;
  border-color: rgba(200, 85, 61, 0.4);
}
.act-btn.danger:not(:disabled):hover {
  background: rgba(200, 85, 61, 0.1);
}
</style>

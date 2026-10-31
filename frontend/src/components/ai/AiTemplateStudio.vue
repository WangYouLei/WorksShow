<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { X, Sparkles, Download, Trash2, Rocket, Eye } from 'lucide-vue-next'
import {
  generateAiTemplate,
  listAiTemplates,
  getAiTemplate,
  deleteAiTemplate,
  ensureAiTemplatePortfolio,
  type AiTemplateItem,
} from '@/api/aiTemplate'
import { deploy, type DeploymentVO } from '@/api/deployment'

const visible = defineModel<boolean>({ default: false })

// ---------- 生成 ----------
const requirement = ref('')
const tplName = ref('')
const generating = ref(false)
const progressChars = ref(0)
const errorText = ref('')
/** 最近一次生成的预览(完成后拉取详情) */
const previewHtml = ref('')
const previewName = ref('')

const presets = [
  '科技感 · 深色渐变背景',
  '极简黑白 · 大面积留白',
  '杂志排版 · 衬线字体',
  '清新柔和 · 粉彩配色',
  '暗黑赛博 · 霓虹高亮',
]

const canGenerate = computed(
  () => requirement.value.trim().length > 0 && !generating.value,
)

const onGenerate = async () => {
  if (!canGenerate.value) return
  generating.value = true
  errorText.value = ''
  progressChars.value = 0
  previewHtml.value = ''
  try {
    const done = await generateAiTemplate(requirement.value.trim(), tplName.value.trim() || undefined, {
      onDelta: (text) => {
        progressChars.value += text.length
      },
    })
    // 拉取清洗后的完整 HTML 做预览
    const detail = await getAiTemplate(done.id)
    previewHtml.value = detail.html
    previewName.value = detail.name
    await loadList()
  } catch (e) {
    errorText.value = (e as Error).message
  } finally {
    generating.value = false
  }
}

// ---------- 我的模板 ----------
type ListItem = AiTemplateItem & { deploying?: boolean }
const list = ref<ListItem[]>([])
const listLoaded = ref(false)
const expandedId = ref<number | null>(null)
const expandedHtml = ref('')
const deployResults = ref<Record<number, string>>({})

watch(visible, (v) => {
  if (v && !listLoaded.value) loadList()
})

async function loadList() {
  try {
    list.value = await listAiTemplates()
    listLoaded.value = true
  } catch {
    // 列表加载失败时保持空列表,用户可切回生成页
  }
}

const onPreview = async (item: ListItem) => {
  if (expandedId.value === item.id) {
    expandedId.value = null
    return
  }
  expandedHtml.value = ''
  expandedId.value = item.id
  try {
    const detail = await getAiTemplate(item.id)
    expandedHtml.value = detail.html
  } catch (e) {
    errorText.value = (e as Error).message
    expandedId.value = null
  }
}

const onDownload = async (item: ListItem) => {
  try {
    const detail = await getAiTemplate(item.id)
    const blob = new Blob([detail.html], { type: 'text/html;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${item.name}.html`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    errorText.value = (e as Error).message
  }
}

/** 本服务器部署:懒建 portfolio 实例后复用现有部署接口 */
const onDeploy = async (item: ListItem) => {
  if (item.deploying) return
  item.deploying = true
  errorText.value = ''
  try {
    const portfolioId = await ensureAiTemplatePortfolio(item.id)
    const detail = await getAiTemplate(item.id)
    const vo: DeploymentVO = await deploy({ portfolioId, html: detail.html, deployType: 'local' })
    deployResults.value[item.id] = vo.deployUrl || '部署成功'
  } catch (e) {
    errorText.value = (e as Error).message
  } finally {
    item.deploying = false
  }
}

const onDelete = async (item: ListItem) => {
  if (!confirm(`确定删除模板「${item.name}」?已部署的站点不受影响。`)) return
  try {
    await deleteAiTemplate(item.id)
    if (expandedId.value === item.id) expandedId.value = null
    await loadList()
  } catch (e) {
    errorText.value = (e as Error).message
  }
}

const activeTab = ref<'generate' | 'mine'>('generate')
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="ats-overlay" @click.self="visible = false">
      <div class="ats-dialog">
        <header class="ats-header">
          <span class="ats-title"><Sparkles :size="16" :stroke-width="2" /> AI 定制模板</span>
          <button class="ats-close" type="button" @click="visible = false">
            <X :size="18" :stroke-width="2" />
          </button>
        </header>

        <div class="ats-tabs">
          <button class="ats-tab" :class="{ 'is-active': activeTab === 'generate' }" type="button" @click="activeTab = 'generate'">生成模板</button>
          <button class="ats-tab" :class="{ 'is-active': activeTab === 'mine' }" type="button" @click="activeTab = 'mine'">
            我的模板<span v-if="list.length" class="ats-count">{{ list.length }}</span>
          </button>
        </div>

        <p v-if="errorText" class="ats-error">{{ errorText }}</p>

        <!-- 生成 -->
        <div v-if="activeTab === 'generate'" class="ats-body">
          <label class="ats-field">
            <span class="ats-label">模板名称(可选)</span>
            <input v-model="tplName" class="ats-input" maxlength="50" placeholder="留空按需求自动命名" />
          </label>
          <label class="ats-field">
            <span class="ats-label">描述你想要的模板</span>
            <textarea
              v-model="requirement"
              class="ats-textarea"
              rows="3"
              maxlength="1000"
              placeholder="例如:深色科技感,顶部大标题加渐变,技能用进度条展示,作品卡片带悬停效果…"
            ></textarea>
          </label>
          <div class="ats-presets">
            <button v-for="p in presets" :key="p" class="ats-chip" type="button" @click="requirement = p">{{ p }}</button>
          </div>
          <button class="ats-generate" type="button" :disabled="!canGenerate" @click="onGenerate">
            <span v-if="generating" class="ats-spinner" aria-hidden="true"></span>
            {{ generating ? `生成中 · 已输出 ${progressChars} 字` : '开始生成' }}
          </button>
          <p class="ats-tip">生成约需 30-60 秒;将自动填入你的简历数据,每天限 5 次。生成的模板不可表单编辑,可随时按新需求重新生成。</p>

          <div v-if="previewHtml" class="ats-preview">
            <div class="ats-preview-head">
              <span>{{ previewName }} · 预览</span>
              <span class="ats-preview-tip">已保存到「我的模板」</span>
            </div>
            <iframe class="ats-frame" :srcdoc="previewHtml" sandbox="allow-scripts" title="AI 模板预览"></iframe>
          </div>
        </div>

        <!-- 我的模板 -->
        <div v-else class="ats-body">
          <p v-if="!list.length && listLoaded" class="ats-empty">还没有 AI 模板,去「生成模板」创建一个吧。</p>
          <div v-for="item in list" :key="item.id" class="ats-card">
            <div class="ats-card-main">
              <span class="ats-card-name">{{ item.name }}</span>
              <span class="ats-card-meta">{{ item.createTime.slice(0, 16).replace('T', ' ') }}</span>
            </div>
            <p class="ats-card-req">{{ item.requirement || '—' }}</p>
            <div class="ats-card-actions">
              <button class="ats-act" type="button" @click="onPreview(item)"><Eye :size="13" :stroke-width="2" />预览</button>
              <button class="ats-act" type="button" @click="onDownload(item)"><Download :size="13" :stroke-width="2" />下载</button>
              <button class="ats-act is-primary" type="button" :disabled="item.deploying" @click="onDeploy(item)">
                <Rocket :size="13" :stroke-width="2" />{{ item.deploying ? '部署中…' : '部署' }}
              </button>
              <button class="ats-act is-danger" type="button" @click="onDelete(item)"><Trash2 :size="13" :stroke-width="2" />删除</button>
            </div>
            <a v-if="deployResults[item.id]" class="ats-deploy-url" :href="deployResults[item.id]" target="_blank" rel="noopener">{{ deployResults[item.id] }}</a>
            <iframe
              v-if="expandedId === item.id"
              class="ats-frame"
              :srcdoc="expandedHtml"
              sandbox="allow-scripts"
              :title="item.name + ' 预览'"
            ></iframe>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.ats-overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
}
.ats-dialog {
  display: flex;
  flex-direction: column;
  width: min(720px, 100%);
  max-height: 90vh;
  background: #101018;
  border: 1px solid rgba(212, 165, 116, 0.25);
  border-radius: 12px;
  overflow: hidden;
}
.ats-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid rgba(245, 241, 232, 0.08);
}
.ats-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #d4a574;
}
.ats-close {
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  color: rgba(245, 241, 232, 0.6);
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.ats-close:hover {
  color: #f5f1e8;
  background: rgba(255, 255, 255, 0.06);
}
.ats-tabs {
  flex-shrink: 0;
  display: flex;
  gap: 4px;
  padding: 10px 18px 0;
}
.ats-tab {
  padding: 8px 14px;
  font-size: 13px;
  color: rgba(245, 241, 232, 0.6);
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
}
.ats-tab.is-active {
  color: #d4a574;
  border-bottom-color: #d4a574;
}
.ats-count {
  margin-left: 6px;
  padding: 1px 7px;
  font-size: 11px;
  color: #0a0a0f;
  background: #d4a574;
  border-radius: 999px;
}
.ats-error {
  margin: 10px 18px 0;
  padding: 8px 12px;
  font-size: 12px;
  color: #c8553d;
  background: rgba(200, 85, 61, 0.1);
  border: 1px solid rgba(200, 85, 61, 0.3);
  border-radius: 8px;
}
.ats-body {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px 18px 18px;
}
.ats-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.ats-label {
  font-size: 12px;
  color: rgba(245, 241, 232, 0.55);
}
.ats-input,
.ats-textarea {
  padding: 9px 12px;
  font-size: 13px;
  font-family: inherit;
  color: #f5f1e8;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(245, 241, 232, 0.15);
  border-radius: 8px;
}
.ats-input:focus,
.ats-textarea:focus {
  outline: none;
  border-color: rgba(212, 165, 116, 0.6);
}
.ats-textarea {
  resize: vertical;
}
.ats-presets {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.ats-chip {
  padding: 5px 11px;
  font-size: 12px;
  color: rgba(245, 241, 232, 0.7);
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(245, 241, 232, 0.15);
  border-radius: 999px;
  cursor: pointer;
}
.ats-chip:hover {
  color: #d4a574;
  border-color: rgba(212, 165, 116, 0.5);
}
.ats-generate {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 18px;
  font-size: 14px;
  color: #0a0a0f;
  background: #d4a574;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.ats-generate:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.ats-generate:not(:disabled):hover {
  background: #e3b885;
}
.ats-spinner {
  width: 13px;
  height: 13px;
  border: 2px solid rgba(10, 10, 15, 0.3);
  border-top-color: #0a0a0f;
  border-radius: 50%;
  animation: ats-spin 0.7s linear infinite;
}
@keyframes ats-spin {
  to {
    transform: rotate(360deg);
  }
}
.ats-tip {
  font-size: 12px;
  line-height: 1.6;
  color: rgba(245, 241, 232, 0.4);
}
.ats-preview {
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(212, 165, 116, 0.3);
  border-radius: 10px;
  overflow: hidden;
}
.ats-preview-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  font-size: 12px;
  color: #d4a574;
  background: rgba(212, 165, 116, 0.08);
}
.ats-preview-tip {
  color: rgba(122, 179, 130, 0.9);
}
.ats-frame {
  width: 100%;
  height: 360px;
  border: none;
  background: #fff;
}
.ats-empty {
  padding: 30px 0;
  font-size: 13px;
  color: rgba(245, 241, 232, 0.45);
  text-align: center;
}
.ats-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(245, 241, 232, 0.1);
  border-radius: 10px;
}
.ats-card-main {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}
.ats-card-name {
  font-size: 14px;
  font-weight: 600;
  color: #f5f1e8;
}
.ats-card-meta {
  font-size: 12px;
  color: rgba(245, 241, 232, 0.4);
}
.ats-card-req {
  margin: 0;
  font-size: 12px;
  line-height: 1.5;
  color: rgba(245, 241, 232, 0.55);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.ats-card-actions {
  display: flex;
  gap: 8px;
}
.ats-act {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  font-size: 12px;
  color: rgba(245, 241, 232, 0.75);
  background: transparent;
  border: 1px solid rgba(245, 241, 232, 0.2);
  border-radius: 6px;
  cursor: pointer;
}
.ats-act:hover {
  color: #d4a574;
  border-color: rgba(212, 165, 116, 0.5);
}
.ats-act.is-primary {
  color: #0a0a0f;
  background: #d4a574;
  border-color: #d4a574;
}
.ats-act.is-primary:hover {
  background: #e3b885;
}
.ats-act.is-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.ats-act.is-danger:hover {
  color: #c8553d;
  border-color: #c8553d;
}
.ats-deploy-url {
  font-size: 12px;
  color: #7ab382;
  word-break: break-all;
}
</style>

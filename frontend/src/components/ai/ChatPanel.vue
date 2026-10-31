<script setup lang="ts">
import { nextTick, onMounted, reactive, ref, watch } from 'vue'
import { X, Trash2, Send, Undo2, Check } from 'lucide-vue-next'
import { getAiHistory, clearAiHistory, streamAiChat, type AiAction, type AiMessage } from '@/api/ai'
import { usePortfolioStore, ensurePortfolioId } from '@/composables/usePortfolioStore'
import { useThemeStore } from '@/composables/useThemeStore'
import { validatePortfolioData, type PortfolioData, type ThemeConfig } from '@/templates/types'

const props = defineProps<{ templateId: string }>()
const emit = defineEmits<{ close: [] }>()

const store = usePortfolioStore(props.templateId)
const theme = useThemeStore(props.templateId)

/** 面板内消息(含流式与交互状态) */
interface ChatMessage extends AiMessage {
  /** 本次会话内是否已应用过动作 */
  applied?: boolean
  /** 是否正在流式接收 */
  streaming?: boolean
}

const messages = reactive<ChatMessage[]>([])
const input = ref('')
const sending = ref(false)
const errorText = ref('')
const portfolioId = ref<number | null>(null)
const listEl = ref<HTMLElement>()

/** 快捷指令 */
const quickCommands = [
  '润色我的个人简介',
  '让作品描述更专业',
  '把技能按熟练度重新分类',
  '推荐一套适合程序员的主题配色',
]

/** 应用前的快照(单级撤销:撤销最近一次应用) */
let lastSnapshot: { data: PortfolioData; theme: ThemeConfig } | null = null
/** 最近一次应用动作的消息(撤销按钮仅出现在该消息上) */
const lastApplied = ref<ChatMessage | null>(null)
/** AI 生成项的 id 序列(避免 v-for key 重复) */
let aiIdSeq = 0

onMounted(async () => {
  const id = await ensurePortfolioId(props.templateId)
  if (id == null) {
    errorText.value = '简历实例未就绪,请稍后重试'
    return
  }
  portfolioId.value = id
  try {
    const history = await getAiHistory(id)
    messages.push(...history)
    scrollBottom()
  } catch {
    // 历史加载失败不阻塞新对话
  }
})

const onSend = async (preset?: string) => {
  const content = (preset ?? input.value).trim()
  if (!content || sending.value || portfolioId.value == null) return
  errorText.value = ''
  if (!preset) input.value = ''
  messages.push({ role: 'user', content })
  const aiMsg = reactive<ChatMessage>({ role: 'assistant', content: '', streaming: true })
  messages.push(aiMsg)
  sending.value = true
  scrollBottom()
  try {
    await streamAiChat(portfolioId.value, content, theme.value, {
      onDelta: (text) => {
        aiMsg.content += text
        scrollBottom()
      },
      onActions: (actions) => {
        aiMsg.actions = actions
      },
    })
  } catch (e) {
    errorText.value = (e as Error).message
    // 没有任何正文时移除空的 assistant 气泡
    if (!aiMsg.content) messages.splice(messages.indexOf(aiMsg), 1)
  } finally {
    aiMsg.streaming = false
    sending.value = false
    scrollBottom()
  }
}

/** 应用一组动作:先拍快照,数据与主题分别合入;只标记当前消息 */
const onApply = (msg: ChatMessage) => {
  if (!msg.actions?.length) return
  lastSnapshot = { data: JSON.parse(JSON.stringify(store)), theme: JSON.parse(JSON.stringify(theme.value)) }
  for (const action of msg.actions) {
    if (action.type === 'update_theme') applyThemePatch(action)
    else if (action.type === 'update_data') applyDataPatch(action)
  }
  msg.applied = true
  lastApplied.value = msg
}

/** 撤销:恢复最近一次应用前的数据与主题(快照包含更早的应用,其余卡片状态保持) */
const onUndo = () => {
  if (!lastSnapshot || !lastApplied.value) return
  store.profile = lastSnapshot.data.profile
  store.works = lastSnapshot.data.works
  store.experiences = lastSnapshot.data.experiences
  store.skills = lastSnapshot.data.skills
  theme.value = lastSnapshot.theme
  lastApplied.value.applied = false
  lastApplied.value = null
  lastSnapshot = null
}

/** update_theme 动作:浅合并入主题状态 */
function applyThemePatch(action: AiAction) {
  const patch = action.patch as {
    palette?: Record<string, string>
    fontFamily?: ThemeConfig['fontFamily']
    radius?: ThemeConfig['radius']
    density?: ThemeConfig['density']
  }
  const next: ThemeConfig = { ...theme.value }
  if (patch.palette) next.palette = { ...next.palette, ...patch.palette }
  if (patch.fontFamily) next.fontFamily = patch.fontFamily
  if (patch.radius) next.radius = patch.radius
  if (patch.density) next.density = patch.density
  theme.value = next
}

/** update_data 动作:profile 浅合并,数组整体替换(经 validate 补全默认字段) */
function applyDataPatch(action: AiAction) {
  const patch = action.patch as Partial<PortfolioData>
  if (patch.profile && typeof patch.profile === 'object') {
    Object.assign(store.profile, patch.profile)
  }
  if (Array.isArray(patch.works)) {
    store.works = validatePortfolioData({ works: patch.works }).works.map((w) => ({
      ...w,
      id: w.id || `ai-w-${++aiIdSeq}`,
    }))
  }
  if (Array.isArray(patch.experiences)) {
    store.experiences = validatePortfolioData({ experiences: patch.experiences }).experiences.map((e) => ({
      ...e,
      id: e.id || `ai-e-${++aiIdSeq}`,
    }))
  }
  if (Array.isArray(patch.skills)) {
    store.skills = validatePortfolioData({ skills: patch.skills }).skills.map((s) => ({
      ...s,
      id: s.id || `ai-s-${++aiIdSeq}`,
    }))
  }
}

const DATA_LABELS: Record<string, string> = {
  profile: '个人信息',
  works: '作品',
  experiences: '经历',
  skills: '技能',
}
const PALETTE_LABELS: Record<string, string> = {
  primary: '主色',
  secondary: '次强调色',
  tertiary: '点缀色',
  bg: '背景',
  bgSoft: '次级背景',
  card: '卡片底色',
  ink: '正文色',
  inkSoft: '次文字色',
  border: '描边',
}

/** 动作摘要:变更卡片上展示的一行说明 */
function describeAction(action: AiAction): string {
  if (action.type === 'update_theme') {
    const parts: string[] = []
    const palette = (action.patch.palette ?? {}) as Record<string, string>
    const colors = Object.keys(palette).map((k) => PALETTE_LABELS[k] ?? k)
    if (colors.length) parts.push(`配色(${colors.join('/')})`)
    if (action.patch.fontFamily) parts.push('字体')
    if (action.patch.radius) parts.push('圆角')
    if (action.patch.density) parts.push('密度')
    return `调整主题:${parts.join(' + ')}` || '调整主题'
  }
  const keys = Object.keys(action.patch)
  return `修改内容:${keys.map((k) => DATA_LABELS[k] ?? k).join(' + ')}`
}

const onClear = async () => {
  if (!confirm('确定清空当前对话记录?')) return
  if (portfolioId.value != null) {
    try {
      await clearAiHistory(portfolioId.value)
    } catch {
      // 清空失败仍保留本地移除,下次进入会重新拉取
    }
  }
  messages.splice(0, messages.length)
  errorText.value = ''
  lastSnapshot = null
  lastApplied.value = null
}

/** Enter 发送;输入法组合期(选词/上屏确认)不触发,避免中文输入误发 */
const onEnterKey = (e: KeyboardEvent) => {
  if (e.isComposing || e.keyCode === 229) return
  onSend()
}

/** 流式输出时自动滚到底部 */
async function scrollBottom() {
  await nextTick()
  const el = listEl.value
  if (el) el.scrollTop = el.scrollHeight
}

watch(
  () => messages.map((m) => m.content).join(''),
  () => scrollBottom(),
)
</script>

<template>
  <aside class="ai-panel">
    <header class="ai-header">
      <span class="ai-title">AI 助理</span>
      <div class="ai-header-actions">
        <button class="ai-icon-btn" type="button" title="清空对话" @click="onClear">
          <Trash2 :size="15" :stroke-width="2" />
        </button>
        <button class="ai-icon-btn" type="button" title="收起面板" @click="emit('close')">
          <X :size="16" :stroke-width="2" />
        </button>
      </div>
    </header>

    <div ref="listEl" class="ai-messages">
      <p v-if="!messages.length" class="ai-empty">
        告诉我你想怎么改:润色文案、补全内容,或者换个配色风格。
        修改会先给你确认,不会直接生效。
      </p>

      <template v-for="(msg, i) in messages" :key="msg.id ?? `local-${i}`">
        <div v-if="msg.role === 'user'" class="ai-bubble is-user">{{ msg.content }}</div>
        <div v-else class="ai-bubble is-assistant">
          <span class="ai-text">{{ msg.content }}</span>
          <span v-if="msg.streaming" class="ai-cursor" aria-hidden="true"></span>

          <div v-if="msg.actions?.length" class="ai-actions" :class="{ 'is-applied': msg.applied }">
            <div class="ai-action-summary">
              <div v-for="(a, j) in msg.actions" :key="j" class="ai-action-line">{{ describeAction(a) }}</div>
            </div>
            <template v-if="!msg.applied">
              <button class="ai-apply" type="button" @click="onApply(msg)">应用变更</button>
            </template>
            <template v-else>
              <span class="ai-applied-tag"><Check :size="12" :stroke-width="2.5" />已应用</span>
              <button v-if="msg === lastApplied" class="ai-undo" type="button" @click="onUndo">
                <Undo2 :size="12" :stroke-width="2.5" />撤销
              </button>
            </template>
          </div>
        </div>
      </template>

      <p v-if="errorText" class="ai-error">{{ errorText }}</p>
    </div>

    <div v-if="!messages.length && !sending" class="ai-quick">
      <button v-for="q in quickCommands" :key="q" class="ai-chip" type="button" @click="onSend(q)">{{ q }}</button>
    </div>

    <div class="ai-input-row">
      <textarea
        v-model="input"
        class="ai-input"
        rows="2"
        placeholder="描述你想要的修改…"
        :disabled="sending || portfolioId == null"
        @keydown.enter.exact.prevent="onEnterKey"
      ></textarea>
      <button class="ai-send" type="button" :disabled="sending || !input.trim() || portfolioId == null" @click="onSend()">
        <Send :size="15" :stroke-width="2" />
      </button>
    </div>
  </aside>
</template>

<style scoped>
.ai-panel {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
  display: flex;
  flex-direction: column;
  width: 340px;
  background: rgba(12, 12, 18, 0.97);
  border-left: 1px solid rgba(212, 165, 116, 0.2);
  backdrop-filter: blur(8px);
}
.ai-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-bottom: 1px solid rgba(245, 241, 232, 0.08);
}
.ai-title {
  font-size: 14px;
  font-weight: 600;
  color: #d4a574;
}
.ai-header-actions {
  display: flex;
  gap: 6px;
}
.ai-icon-btn {
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  color: rgba(245, 241, 232, 0.6);
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.ai-icon-btn:hover {
  color: #d4a574;
  background: rgba(212, 165, 116, 0.1);
}
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.ai-empty {
  margin: 24px 8px;
  font-size: 13px;
  line-height: 1.7;
  color: rgba(245, 241, 232, 0.45);
  text-align: center;
}
.ai-bubble {
  max-width: 92%;
  padding: 9px 12px;
  font-size: 13px;
  line-height: 1.65;
  border-radius: 10px;
  word-break: break-word;
  white-space: pre-wrap;
}
.ai-bubble.is-user {
  align-self: flex-end;
  color: #0a0a0f;
  background: #d4a574;
}
.ai-bubble.is-assistant {
  align-self: flex-start;
  color: rgba(245, 241, 232, 0.9);
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(245, 241, 232, 0.08);
}
.ai-cursor {
  display: inline-block;
  width: 7px;
  height: 14px;
  margin-left: 2px;
  vertical-align: -2px;
  background: #d4a574;
  animation: ai-blink 0.8s step-end infinite;
}
@keyframes ai-blink {
  50% {
    opacity: 0;
  }
}
.ai-actions {
  margin-top: 10px;
  padding: 10px;
  border: 1px dashed rgba(212, 165, 116, 0.45);
  border-radius: 8px;
}
.ai-actions.is-applied {
  border-style: solid;
  border-color: rgba(122, 179, 130, 0.5);
}
.ai-action-summary {
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.ai-action-line {
  font-size: 12px;
  color: #d4a574;
}
.ai-actions.is-applied .ai-action-line {
  color: rgba(122, 179, 130, 0.9);
}
.ai-apply {
  margin-top: 8px;
  padding: 5px 14px;
  font-size: 12px;
  color: #0a0a0f;
  background: #d4a574;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.ai-apply:hover {
  background: #e3b885;
}
.ai-applied-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  margin-right: 8px;
  font-size: 12px;
  color: rgba(122, 179, 130, 0.95);
}
.ai-undo {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding: 4px 10px;
  font-size: 12px;
  color: rgba(245, 241, 232, 0.7);
  background: transparent;
  border: 1px solid rgba(245, 241, 232, 0.25);
  border-radius: 6px;
  cursor: pointer;
}
.ai-undo:hover {
  color: #c8553d;
  border-color: #c8553d;
}
.ai-error {
  align-self: center;
  padding: 8px 12px;
  font-size: 12px;
  color: #c8553d;
  background: rgba(200, 85, 61, 0.1);
  border: 1px solid rgba(200, 85, 61, 0.3);
  border-radius: 8px;
}
.ai-quick {
  flex-shrink: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 0 14px 10px;
}
.ai-chip {
  padding: 5px 10px;
  font-size: 12px;
  color: rgba(245, 241, 232, 0.7);
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(245, 241, 232, 0.15);
  border-radius: 999px;
  cursor: pointer;
}
.ai-chip:hover {
  color: #d4a574;
  border-color: rgba(212, 165, 116, 0.5);
}
.ai-input-row {
  flex-shrink: 0;
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 12px 14px;
  border-top: 1px solid rgba(245, 241, 232, 0.08);
}
.ai-input {
  flex: 1;
  resize: none;
  padding: 8px 10px;
  font-size: 13px;
  font-family: inherit;
  line-height: 1.5;
  color: #f5f1e8;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(245, 241, 232, 0.15);
  border-radius: 8px;
}
.ai-input:focus {
  outline: none;
  border-color: rgba(212, 165, 116, 0.6);
}
.ai-input:disabled {
  opacity: 0.5;
}
.ai-send {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  color: #0a0a0f;
  background: #d4a574;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.ai-send:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.ai-send:not(:disabled):hover {
  background: #e3b885;
}
</style>

import { reactive, watch, readonly } from 'vue'
import type { PortfolioData } from '@/templates/types'
import { getTemplateDefaultData } from '@/templates/registry'
import {
  listPortfolios,
  createPortfolio,
  type PortfolioRequest,
} from '@/api/portfolio'
import {
  getUserProfile,
  saveUserProfile,
  type BackendUserProfile,
} from '@/api/userProfile'

/**
 * 全局缓存状态(所有模板共享同一份用户档案)。
 * <p>
 * 核心变化:简历内容(profile/works/experiences/skills)从"按模板隔离"
 * 改为"用户级共享"。所有模板共用同一个 reactive data 对象,
 * 编辑任一模板的内容会同步影响所有模板。
 */
interface CacheState {
  /** 可响应的简历数据(供面板双向绑定,所有模板共享) */
  data: PortfolioData
  /** 服务端数据是否加载完成(成功或失败均置 true) */
  loaded: boolean
  /** 是否正在保存中(防止并发) */
  saving: boolean
  /** 上次保存成功时间戳(毫秒),null 表示从未保存 */
  lastSaved: number | null
  /** 上次保存是否失败 */
  saveError: boolean
  /** 加载失败标志:加载失败时置 true,阻止后续 saveToServer 避免覆盖服务端数据 */
  loadError: boolean
}

/** 全局单例 state(null 表示未初始化) */
let state: CacheState | null = null
/** 防抖定时器引用(非响应式) */
let saveTimer: ReturnType<typeof setTimeout> | null = null
/** 保存期间有新变更的标志:saveToServer 在 saving=true 时置 true,finally 中检查并重新调度 */
let pendingSave = false
/** 各模板的 portfolio 实例 ID(templateId -> portfolioId) */
const portfolioIds: Record<string, number> = {}
/** 已确保创建过实例的模板集合(避免重复创建) */
const ensuredTemplates = new Set<string>()

function clone<T>(v: T): T {
  return JSON.parse(JSON.stringify(v))
}

/**
 * 将后端返回的用户档案(id: number)normalize 为前端类型(id: string)。
 * <p>
 * 前端 Work/Experience/Skill 的 id 用于 v-for key,统一转为 string。
 */
function normalizeUserProfile(backend: BackendUserProfile): PortfolioData {
  return {
    profile: backend.profile,
    works: (backend.works || []).map((w) => ({ ...w, id: String(w.id ?? '') })),
    experiences: (backend.experiences || []).map((e) => ({ ...e, id: String(e.id ?? '') })),
    skills: (backend.skills || []).map((s) => ({ ...s, id: String(s.id ?? '') })),
  }
}

/**
 * 获取共享的用户简历数据(响应式 + 后端自动同步)。
 * <p>
 * 所有模板共享同一份数据,编辑任一模板的内容会影响所有模板。
 * 行为:
 * <ul>
 *   <li>同步:立即返回模板默认数据(首次调用时),避免阻塞渲染</li>
 *   <li>异步:从服务端加载用户档案,加载完成后覆盖默认数据</li>
 *   <li>监听:深度 watch,800ms 防抖后自动保存到用户档案</li>
 *   <li>实例:首次进入某模板时自动创建 portfolio 实例(用于"我的简历"列表)</li>
 * </ul>
 *
 * @param templateId 模板ID(用于确保 portfolio 实例存在,数据本身与模板无关)
 */
export function usePortfolioStore(templateId: string): PortfolioData {
  ensureCache(templateId)
  return state!.data
}

/**
 * 初始化全局缓存(单例),加载用户档案 + 确保 portfolio 实例存在。
 */
function ensureCache(templateId: string) {
  if (state) {
    // 已有缓存,确保当前模板的 portfolio 实例存在
    ensurePortfolioInstance(templateId)
    return
  }

  // 首次调用:用第一个模板的默认数据初始化(仅作占位,异步加载后会覆盖)
  const def = getTemplateDefaultData(templateId)
  if (!def) throw new Error(`模板 ${templateId} 未注册默认数据`)
  const data = reactive(clone(def)) as PortfolioData
  state = reactive({ data, loaded: false, saving: false, lastSaved: null, saveError: false, loadError: false })

  // 异步加载用户档案
  loadFromServer()

  // 深度监听,防抖保存到用户档案
  watch(
    data,
    () => {
      // 加载未完成时不保存,避免覆盖服务端数据
      if (!state!.loaded) return
      if (saveTimer) clearTimeout(saveTimer)
      saveTimer = setTimeout(() => {
        saveToServer()
      }, 800)
    },
    { deep: true },
  )

  // 确保当前模板的 portfolio 实例存在
  ensurePortfolioInstance(templateId)
}

/**
 * 从服务端加载用户档案,覆盖默认数据。
 * 失败时回退到默认数据并置 loadError=true,阻止后续 saveToServer 避免覆盖服务端数据。
 * loaded 仍置 true(让 UI 可编辑),用户刷新页面可重试加载。
 */
async function loadFromServer() {
  try {
    const profile = await getUserProfile()
    const normalized = normalizeUserProfile(profile)
    state!.data.profile = normalized.profile
    state!.data.works = normalized.works
    state!.data.experiences = normalized.experiences
    state!.data.skills = normalized.skills
    state!.loadError = false
  } catch (e) {
    console.warn('[store] 从服务端加载用户档案失败,使用默认数据(保存将被阻止直到加载成功)', e)
    state!.loadError = true
  } finally {
    state!.loaded = true
  }
}

/**
 * 确保指定模板的 portfolio 实例存在(用于"我的简历"列表)。
 * 已确保过的模板不重复创建。
 */
async function ensurePortfolioInstance(templateId: string) {
  if (ensuredTemplates.has(templateId)) return
  ensuredTemplates.add(templateId)
  try {
    const list = await listPortfolios()
    const mine = list.find((p) => p.templateId === templateId)
    if (mine) {
      portfolioIds[templateId] = mine.id
    } else {
      // 创建 portfolio 实例(仅 templateId + name)
      const req: PortfolioRequest = {
        templateId,
        name: `${templateId} 简历`,
      }
      const created = await createPortfolio(req)
      portfolioIds[templateId] = created.id
    }
  } catch (e) {
    console.warn(`[store] 确保 portfolio 实例失败: templateId=${templateId}`, e)
  }
}

/**
 * 保存用户档案到服务端(整体替换)。
 * - 加载失败时(loadError=true)跳过保存,避免用默认数据覆盖服务端档案
 * - 保存期间若有新变更,通过 pendingSave 标志在完成后重新调度,避免变更丢失
 * 失败时仅打印日志,不阻断后续编辑(下次变更会再次触发保存)。
 */
async function saveToServer() {
  if (!state) return
  // 保存中:标记有新变更待保存,由当前保存的 finally 重新调度
  if (state.saving) {
    pendingSave = true
    return
  }
  // 加载失败时不保存,避免用默认数据覆盖服务端档案
  if (state.loadError) {
    console.warn('[store] 加载失败,跳过保存以避免覆盖服务端数据')
    return
  }
  state.saving = true
  pendingSave = false
  try {
    // 保存时不传 id(后端整体替换,会重新分配 id)
    const { data } = state
    await saveUserProfile({
      profile: data.profile,
      works: data.works.map(({ id: _id, ...rest }) => rest),
      experiences: data.experiences.map(({ id: _id, ...rest }) => rest),
      skills: data.skills.map(({ id: _id, ...rest }) => rest),
    })
    state.saveError = false
    state.lastSaved = Date.now()
  } catch (e) {
    state.saveError = true
    console.error('[store] 保存用户档案失败', e)
  } finally {
    state.saving = false
    // 保存期间若有新变更,重新调度保存,避免变更丢失
    if (pendingSave) {
      pendingSave = false
      if (saveTimer) clearTimeout(saveTimer)
      saveTimer = setTimeout(() => {
        saveToServer()
      }, 800)
    }
  }
}

/**
 * 重置为模板默认数据(保留响应式对象引用)。
 * <p>
 * 注意:由于所有模板共享同一份档案,重置会影响所有模板的内容。
 * 触发 watch 后会自动保存到用户档案(覆盖服务端数据)。
 */
export function resetPortfolioStore(templateId: string): PortfolioData {
  const def = getTemplateDefaultData(templateId)
  if (!def) throw new Error(`模板 ${templateId} 未注册默认数据`)
  const fresh = clone(def)
  if (state) {
    state.data.profile = fresh.profile
    state.data.works = fresh.works
    state.data.experiences = fresh.experiences
    state.data.skills = fresh.skills
    return state.data
  }
  return usePortfolioStore(templateId)
}

/** 简历保存状态(供视图层显示) */
export interface PortfolioSaveStatus {
  /** 是否正在保存 */
  saving: boolean
  /** 上次保存成功时间,null 表示从未保存 */
  lastSaved: number | null
  /** 上次保存是否失败 */
  saveError: boolean
  /** 服务端数据是否已加载完成 */
  loaded: boolean
}

/**
 * 获取保存状态(响应式,供视图层 watch 渲染)。
 * @param _templateId 模板ID(保留参数以兼容旧接口,实际与模板无关)
 */
export function getSaveStatus(_templateId: string): PortfolioSaveStatus {
  if (!state) {
    return reactive({ saving: false, lastSaved: null, saveError: false, loaded: false })
  }
  return readonly({
    get saving() { return state!.saving },
    get lastSaved() { return state!.lastSaved },
    get saveError() { return state!.saveError },
    get loaded() { return state!.loaded },
  }) as PortfolioSaveStatus
}

/**
 * 手动立即保存(取消防抖延迟,直接触发保存)。
 * 如果数据正在保存中则跳过,如果服务端数据尚未加载完成也跳过。
 * @param _templateId 模板ID(保留参数以兼容旧接口,实际与模板无关)
 */
export async function saveNow(_templateId: string): Promise<void> {
  if (!state || !state.loaded || state.saving) return
  if (saveTimer) {
    clearTimeout(saveTimer)
    saveTimer = null
  }
  await saveToServer()
}

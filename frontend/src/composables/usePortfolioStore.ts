import { reactive, watch, readonly, effectScope, type EffectScope } from 'vue'
import type { PortfolioData } from '@/templates/types'
import { getTemplateDefaultData, templates } from '@/templates/registry'
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
import type { Profile } from '@/data/types'

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
/** 自动保存 watcher 的独立作用域(detached,不随组件卸载销毁) */
let saveScope: EffectScope | null = null
/** 各模板的 portfolio 实例 ID(templateId -> portfolioId) */
let portfolioIds: Record<string, number> = {}
/** 已确保创建过实例的模板集合(避免重复创建) */
const ensuredTemplates = new Set<string>()
/** 正在从服务端应用数据(loadFromServer 赋值期间为 true)。
 *  赋值期间 watcher 不应触发保存:此时服务端刚返回的新值覆盖了模板默认数据,
 *  触发保存属于冗余回写,且加载前用户可能已输入内容会被判定为"新数据"提交 */
let applyingFromServer = false

/** profile 锁定标志:锁定时编辑器中的 profile 修改不会保存到服务端 */
let profileLocked = false
/** profile 快照:锁定时保存使用此快照而非当前 data.profile,解锁时恢复 */
let profileSnapshot: Profile | null = null

function clone<T>(v: T): T {
  return JSON.parse(JSON.stringify(v))
}

/**
 * 将后端返回的用户档案(id: number)normalize 为前端类型(id: string)。
 * <p>
 * 前端 Work/Experience/Skill 的 id 用于 v-for key,统一转为 string。
 * 后端懒创建:用户从未保存过档案时 profile 各字段为 null。null 视为"未设置",
 * 回退到 fallback(模板默认数据),避免模板渲染崩溃(如 profile.name.slice);
 * 用户主动清空的空字符串会原样返回,不会被默认值覆盖。
 */
function normalizeUserProfile(backend: BackendUserProfile, fallback: Profile): PortfolioData {
  const p = backend.profile
  return {
    profile: {
      name: p.name ?? fallback.name,
      nameEn: p.nameEn ?? fallback.nameEn,
      title: p.title ?? fallback.title,
      tagline: p.tagline ?? fallback.tagline,
      bio: p.bio ?? fallback.bio,
      email: p.email ?? fallback.email,
      location: p.location ?? fallback.location,
      availability: p.availability ?? fallback.availability,
      stats: p.stats ?? fallback.stats,
      socials: p.socials ?? fallback.socials,
    },
    // tags/items 为后端 JSON 列,可能为 null,兜底为数组避免模板中 .slice 崩溃
    works: (backend.works || []).map((w) => ({ ...w, id: String(w.id ?? ''), tags: w.tags ?? [] })),
    experiences: (backend.experiences || []).map((e) => ({ ...e, id: String(e.id ?? '') })),
    skills: (backend.skills || []).map((s) => ({ ...s, id: String(s.id ?? ''), items: s.items ?? [] })),
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
 * @param templateId 模板ID(用于确保 portfolio 实例存在,数据本身与模板无关)。设置页等不需要创建实例的场景可不传。
 */
export function usePortfolioStore(templateId?: string): PortfolioData {
  ensureCache(templateId)
  return state!.data
}

/**
 * 初始化全局缓存(单例),加载用户档案 + 确保 portfolio 实例存在。
 */
function ensureCache(templateId?: string) {
  if (state) {
    // 已有缓存,确保当前模板的 portfolio 实例存在
    if (templateId) ensurePortfolioInstance(templateId)
    return
  }

  // 首次调用:用模板的默认数据初始化(仅作占位,异步加载后会覆盖)
  const tid = templateId || templates[0]?.id
  if (!tid) throw new Error('没有已注册的模板')
  const def = getTemplateDefaultData(tid)
  if (!def) throw new Error(`模板 ${tid} 未注册默认数据`)
  const data = reactive(clone(def)) as PortfolioData
  state = reactive({ data, loaded: false, saving: false, lastSaved: null, saveError: false, loadError: false })

  // 异步加载用户档案
  loadFromServer()

  // 深度监听,防抖保存到用户档案
  // 必须放在 detached 的 effectScope 中:首次调用发生在组件 setup 上下文,
  // 普通 watch 会绑定该组件作用域,组件卸载时被销毁,导致自动保存静默失效
  saveScope = effectScope(true)
  saveScope.run(() => {
    watch(
      data,
      () => {
        // 加载未完成 / 正在应用服务端数据时不保存:
        // - !loaded:初始阶段,还没拿到服务端数据,保存会用模板默认数据覆盖
        // - applyingFromServer:赋值刚从服务端返回,触发保存是冗余回写
        if (!state!.loaded || applyingFromServer) return
        if (saveTimer) clearTimeout(saveTimer)
        saveTimer = setTimeout(() => {
          saveToServer()
        }, 800)
      },
      { deep: true },
    )
  })

  // 确保当前模板的 portfolio 实例存在
  if (templateId) ensurePortfolioInstance(templateId)
}

/**
 * 从服务端加载用户档案,覆盖默认数据。
 * 失败时回退到默认数据并置 loadError=true,阻止后续 saveToServer 避免覆盖服务端数据。
 * loaded 仍置 true(让 UI 可编辑),用户刷新页面可重试加载。
 */
async function loadFromServer() {
  // 持有本地引用:await 期间 store 可能被 destroy(state 置 null),写孤儿对象无害
  const s = state
  if (!s) return
  try {
    const profile = await getUserProfile()
    // fallback 传当前数据(初始为模板默认数据):服务端 null 字段保留默认值
    const normalized = normalizeUserProfile(profile, s.data.profile)
    // 赋值期间加 applyingFromServer 标志,防止 watcher 触发冗余回写:
    // 刚从服务端拿到的旧数据覆盖了可能已被用户编辑过的模板默认数据,
    // 触发保存等于立即把刚读到的数据又发回去,且会把用户输入的变更吞掉
    applyingFromServer = true
    try {
      s.data.profile = normalized.profile
      s.data.works = normalized.works
      s.data.experiences = normalized.experiences
      s.data.skills = normalized.skills
    } finally {
      applyingFromServer = false
    }
    s.loadError = false
    // 更新 profile 快照(用于编辑器锁定时恢复)
    profileSnapshot = clone(normalized.profile)
  } catch (e) {
    console.warn('[store] 从服务端加载用户档案失败,使用默认数据(保存将被阻止直到加载成功)', e)
    s.loadError = true
  } finally {
    s.loaded = true
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
    // 失败时回滚标记,允许后续重试
    // (标记在异步操作前加入,若失败后仍保留,该模板将永远无法再创建实例)
    ensuredTemplates.delete(templateId)
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
  // 持有本地引用:await 期间 store 可能被 destroy(state 置 null),写孤儿对象无害
  const s = state
  // 保存中:标记有新变更待保存,由当前保存的 finally 重新调度
  if (s.saving) {
    pendingSave = true
    return
  }
  // 加载失败时不保存,避免用默认数据覆盖服务端档案
  if (s.loadError) {
    console.warn('[store] 加载失败,跳过保存以避免覆盖服务端数据')
    return
  }
  s.saving = true
  pendingSave = false
  try {
    // 保存时不传 id(后端整体替换,会重新分配 id)
    const { data } = s
    // profile 锁定时使用快照(编辑器中的 profile 修改不持久化)
    const profileToSave = profileLocked && profileSnapshot ? profileSnapshot : data.profile
    await saveUserProfile({
      profile: profileToSave,
      works: data.works.map(({ id: _id, ...rest }) => rest),
      experiences: data.experiences.map(({ id: _id, ...rest }) => rest),
      skills: data.skills.map(({ id: _id, ...rest }) => rest),
    })
    s.saveError = false
    s.lastSaved = Date.now()
    // 未锁定时更新快照为当前 profile(设置页保存后快照同步)
    if (!profileLocked) {
      profileSnapshot = clone(data.profile)
    }
  } catch (e) {
    s.saveError = true
    console.error('[store] 保存用户档案失败', e)
  } finally {
    s.saving = false
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
 * 销毁全局 store(登出/登录态失效时调用)。
 * <p>
 * store 为模块级单例:SPA 内登出后换账号登录(无整页刷新)时,
 * 若不重置单例,下一个用户会看到并编辑上一个用户的档案(跨账号数据污染)。
 * 销毁后再次调用 usePortfolioStore 会按模板默认数据重新初始化。
 */
export function destroyPortfolioStore(): void {
  if (saveTimer) {
    clearTimeout(saveTimer)
    saveTimer = null
  }
  saveScope?.stop()
  saveScope = null
  pendingSave = false
  applyingFromServer = false
  state = null
  portfolioIds = {}
  ensuredTemplates.clear()
  profileLocked = false
  profileSnapshot = null
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

/**
 * 锁定/解锁 profile 持久化。
 * <p>
 * 编辑器使用:进入时锁定( profile 修改不保存到服务端,仅用于预览),
 * 离开时解锁并从快照恢复 profile(丢弃编辑器中的临时修改)。
 * 设置页不调用此函数(默认未锁定,修改会正常保存)。
 *
 * @param locked true=锁定(进入编辑器),false=解锁(离开编辑器)
 */
export function setProfileLocked(locked: boolean): void {
  if (locked) {
    // 锁定时:如果数据已加载,拍下当前 profile 快照
    if (state && state.loaded && !state.loadError && state.data.profile) {
      profileSnapshot = clone(state.data.profile)
    }
    profileLocked = true
  } else {
    // 解锁时:从快照恢复 profile(丢弃编辑器中的临时修改)
    if (state && profileSnapshot) {
      state.data.profile = clone(profileSnapshot)
    }
    profileLocked = false
  }
}

/**
 * 确保指定模板的 portfolio 实例已创建,并返回其 ID。
 * <p>
 * 部署功能需要 portfolioId 关联简历实例。首次进入编辑器时
 * ensurePortfolioInstance 已异步创建实例,但调用方无法感知完成时机,
 * 此函数封装等待逻辑:幂等调用,完成后返回 portfolioId(失败返回 undefined)。
 *
 * @param templateId 模板ID
 * @return portfolioId,实例未就绪或创建失败时返回 undefined
 */
export async function ensurePortfolioId(templateId: string): Promise<number | undefined> {
  await ensurePortfolioInstance(templateId)
  return portfolioIds[templateId]
}

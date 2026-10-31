import { ref, type Ref } from 'vue'
import type { ThemeConfig } from '@/templates/types'
import { getTemplateDefaultTheme } from '@/templates/registry'

/** 每个模板一份主题状态，模块级共享（EditorView / ThemePanel / DeployDialog 共用） */
const themeMap = new Map<string, Ref<ThemeConfig>>()

function clone<T>(v: T): T {
  return JSON.parse(JSON.stringify(v))
}

export function useThemeStore(templateId: string): Ref<ThemeConfig> {
  let theme = themeMap.get(templateId)
  if (!theme) {
    theme = ref<ThemeConfig>(clone(getTemplateDefaultTheme(templateId) ?? {}))
    themeMap.set(templateId, theme)
  }
  return theme
}

/** 重置为模板默认主题；模板未提供默认主题时清空（回落到模板样式内联默认值） */
export function resetThemeStore(templateId: string): void {
  const theme = useThemeStore(templateId)
  theme.value = clone(getTemplateDefaultTheme(templateId) ?? {})
}

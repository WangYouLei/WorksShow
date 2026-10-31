import type { ThemeConfig } from '@/templates/types'

/** fontFamily 枚举 → 字体栈（预览与导出共用同一映射） */
export const FONT_STACKS: Record<NonNullable<ThemeConfig['fontFamily']>, string> = {
  sans: `'Noto Sans SC', system-ui, -apple-system, 'Segoe UI', sans-serif`,
  serif: `'Noto Serif SC', 'Songti SC', 'SimSun', serif`,
  rounded: `'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Noto Sans SC', sans-serif`,
}

/** radius 枚举 → 圆角缩放系数（模板圆角 = 基准值 × 系数） */
export const RADIUS_SCALE: Record<NonNullable<ThemeConfig['radius']>, string> = {
  sharp: '0.4',
  soft: '1',
  round: '1.6',
}

/** fontFamily 枚举 → 导出 HTML 需要的 Google Fonts 链接 */
export const FONT_LINKS: Record<NonNullable<ThemeConfig['fontFamily']>, string> = {
  sans: 'https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@300;400;500;700;800;900&display=swap',
  serif: 'https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;500;700;900&display=swap',
  rounded: 'https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@300;400;500;700;800;900&display=swap',
}

/** ThemeConfig → CSS 变量表（键为 --theme-* 变量名），预览侧用于 :style 绑定 */
export function themeToCssVars(theme?: ThemeConfig): Record<string, string> {
  const vars: Record<string, string> = {}
  if (!theme) return vars
  const p = theme.palette ?? {}
  if (p.primary) vars['--theme-primary'] = p.primary
  if (p.secondary) vars['--theme-secondary'] = p.secondary
  if (p.tertiary) vars['--theme-tertiary'] = p.tertiary
  if (p.bg) vars['--theme-bg'] = p.bg
  if (p.bgSoft) vars['--theme-bg-soft'] = p.bgSoft
  if (p.card) vars['--theme-card'] = p.card
  if (p.ink) vars['--theme-ink'] = p.ink
  if (p.inkSoft) vars['--theme-ink-soft'] = p.inkSoft
  if (p.border) vars['--theme-border'] = p.border
  if (theme.fontFamily) vars['--theme-font'] = FONT_STACKS[theme.fontFamily]
  if (theme.radius) vars['--theme-radius-scale'] = RADIUS_SCALE[theme.radius]
  return vars
}

/** ThemeConfig → :root 选择器内的 CSS 文本，导出侧用于注入 <style> */
export function themeToCssText(theme?: ThemeConfig): string {
  return Object.entries(themeToCssVars(theme))
    .map(([k, v]) => `  ${k}: ${v};`)
    .join('\n')
}

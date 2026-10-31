import type { ThemeConfig } from '../types'

/** 粉彩模板默认主题：与 PastelPreview 样式中的原始配色一致，作为编辑器重置与 AI 改色的基准 */
export const pastelDefaultTheme: ThemeConfig = {
  palette: {
    primary: '#e8a0a0',
    secondary: '#9fc7b4',
    tertiary: '#c3b1e1',
    bg: '#fbf7f2',
    bgSoft: '#f4ede4',
    card: '#ffffff',
    ink: '#3d3536',
    inkSoft: '#7a6f6a',
    border: '#ece2d6',
  },
  fontFamily: 'sans',
  radius: 'soft',
  density: 'normal',
}

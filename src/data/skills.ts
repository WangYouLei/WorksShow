import type { Skill } from './types'

export const skills: Skill[] = [
  {
    id: 's1',
    category: '前端工程',
    categoryEn: 'Frontend',
    items: ['Vue 3', 'React', 'TypeScript', 'Vite', 'Nuxt', 'CSS 动效', 'WebGL'],
  },
  {
    id: 's2',
    category: '设计能力',
    categoryEn: 'Design',
    items: ['UI 设计', 'UX 研究', '品牌视觉', '插画', '排版设计', '设计系统'],
  },
  {
    id: 's3',
    category: '工程化',
    categoryEn: 'Engineering',
    items: ['Node.js', 'Electron', 'Monorepo', 'CI/CD', '性能优化', '可访问性'],
  },
  {
    id: 's4',
    category: '工具与协作',
    categoryEn: 'Tools',
    items: ['Figma', 'Blender', 'Git', 'Notion', 'Linear', '设计走查'],
  },
]

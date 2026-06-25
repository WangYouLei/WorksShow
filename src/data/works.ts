import type { Work } from './types'

export const works: Work[] = [
  {
    id: 'w1',
    title: '云栖茶事',
    category: '品牌网站',
    cover:
      'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=elegant%20tea%20brand%20website%20design%2C%20dark%20background%2C%20chinese%20tea%20culture%2C%20minimal%20layout%2C%20amber%20gold%20accents%2C%20editorial%20style%2C%20high%20end%20aesthetic&image_size=landscape_16_9',
    description:
      '为高端茶文化品牌打造的沉浸式品牌网站。以"一片叶子的旅程"为叙事主线，融合滚动视差与水墨动画，展现从茶园到茶席的完整意境。整站采用暗色基调，以琥珀金点缀，传递东方茶道的沉静与克制。',
    year: '2025',
    link: 'https://example.com',
    tags: ['Vue 3', 'GSAP', 'WebGL', '品牌设计'],
    highlight: true,
  },
  {
    id: 'w2',
    title: '墨痕笔记',
    category: '效率工具',
    cover:
      'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=minimalist%20note%20taking%20app%20interface%2C%20dark%20theme%2C%20clean%20typography%2C%20markdown%20editor%2C%20warm%20amber%20highlights%2C%20productivity%20tool%20design&image_size=landscape_16_9',
    description:
      '一款面向写作者的极简笔记应用。支持 Markdown、双链关联、本地优先存储。设计上摒弃一切多余装饰，让文字回归纯粹。已服务超过 12 万创作者。',
    year: '2024',
    link: 'https://example.com',
    tags: ['Electron', 'TypeScript', 'SQLite', 'UX 设计'],
    highlight: true,
  },
  {
    id: 'w3',
    title: '青瓷数据看板',
    category: '数据可视化',
    cover:
      'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=data%20visualization%20dashboard%2C%20dark%20mode%2C%20elegant%20charts%2C%20celadon%20green%20and%20amber%20color%20scheme%2C%20analytics%20interface%2C%20modern%20clean%20design&image_size=landscape_16_9',
    description:
      '为金融科技团队定制的实时数据看板。以青瓷绿为主色，将复杂的金融数据转化为直观的视觉语言。支持自定义图表、实时流数据与多维度筛选。',
    year: '2024',
    tags: ['React', 'D3.js', 'WebSocket', '数据设计'],
  },
  {
    id: 'w4',
    title: '山行户外',
    category: '移动应用',
    cover:
      'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=outdoor%20hiking%20app%20design%2C%20mountain%20landscape%2C%20dark%20UI%2C%20map%20interface%2C%20adventure%20app%2C%20warm%20tones%2C%20modern%20mobile%20design&image_size=landscape_16_9',
    description:
      '面向户外爱好者的徒步路线规划与记录应用。整合地形数据、天气预警与社区分享。上线半年用户突破 50 万，获 App Store 年度推荐。',
    year: '2023',
    link: 'https://example.com',
    tags: ['React Native', 'Mapbox', 'Node.js', '产品设计'],
  },
  {
    id: 'w5',
    title: '观象历法',
    category: '文化装置',
    cover:
      'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chinese%20traditional%20calendar%20visualization%2C%2024%20solar%20terms%2C%20ink%20wash%20painting%20style%2C%20dark%20background%2C%20cultural%20art%20installation%2C%20elegant%20typography&image_size=landscape_16_9',
    description:
      '一个以二十四节气为主线的交互式文化网站。每个节气配以水墨动画、古诗词与物候数据，让传统历法在数字时代焕发新生。获 Awwwards 站点提名。',
    year: '2023',
    tags: ['Vue 3', 'Canvas', '动效设计', '文化研究'],
  },
  {
    id: 'w6',
    title: '简言翻译',
    category: '效率工具',
    cover:
      'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=translation%20app%20interface%2C%20minimal%20design%2C%20dark%20theme%2C%20clean%20text%20layout%2C%20language%20switching%2C%20modern%20productivity%20tool%2C%20amber%20accents&image_size=landscape_16_9',
    description:
      '一款主打"沉浸式翻译"的浏览器插件。支持双语对照、文档翻译与术语库管理。轻量、快速、不打断阅读节奏，日活用户 8 万+。',
    year: '2022',
    tags: ['TypeScript', 'WebExtension', 'NLP', '交互设计'],
  },
]

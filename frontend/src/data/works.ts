import type { Work } from './types'

export const works: Work[] = [
  {
    id: 'w1',
    title: 'AI 对话助手平台',
    category: 'AI 产品',
    cover:
      'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20AI%20chatbot%20platform%20interface%2C%20clean%20UI%2C%20conversation%20panel%2C%20soft%20pastel%20colors%2C%20productivity%20app%20design%2C%20friendly%20gradient%20background&image_size=landscape_16_9',
    description:
      '基于 GPT-4 的新一代智能对话平台，支持多模态输入、知识库检索与团队协作。内置 Prompt 管理、对话历史与精细化使用分析功能。获得 Product Hunt 当日第一，10 天内突破万名用户。',
    year: '2024',
    link: 'https://example.com',
    tags: ['Next.js', 'OpenAI API', 'Pinecone', 'Supabase'],
    highlight: true,
  },
  {
    id: 'w2',
    title: '自动化工作流引擎',
    category: '效率工具',
    cover:
      'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=no-code%20workflow%20automation%20platform%2C%20node%20editor%20interface%2C%20connected%20services%2C%20clean%20modern%20UI%2C%20mint%20green%20accents%2C%20productivity%20dashboard&image_size=landscape_16_9',
    description:
      '无代码编排平台，连接 200+ 服务，企业级稳定性。服务 30+ 企业客户，帮助团队节省 60% 的重复性工作时间。',
    year: '2024',
    tags: ['Node.js', 'GraphQL', 'Prisma', 'tRPC'],
  },
  {
    id: 'w3',
    title: '数据可视化平台',
    category: '数据工具',
    cover:
      'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=data%20visualization%20dashboard%2C%20real-time%20charts%2C%20business%20intelligence%2C%20clean%20analytics%20interface%2C%20pastel%20color%20scheme%2C%20modern%20design&image_size=landscape_16_9',
    description:
      '实时大屏 · 商业智能分析 · 100M+ 数据点处理能力。为金融科技团队提供决策支持，企业级方案持续服务中。',
    year: '2023',
    tags: ['React', 'D3.js', 'WebSocket', 'FastAPI'],
  },
  {
    id: 'w4',
    title: '开源组件库',
    category: '开源项目',
    cover:
      'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=open%20source%20react%20component%20library%20showcase%2C%20UI%20kit%20design%2C%20clean%20components%20grid%2C%20modern%20documentation%20site%2C%20soft%20pastel%20theme&image_size=landscape_16_9',
    description:
      '中文产品专属 React 组件生态系统。GitHub 首周获 500 Star，被多个头部项目采用，持续维护中。',
    year: '2023',
    tags: ['React', 'TypeScript', 'Tailwind CSS', 'Storybook'],
  },
  {
    id: 'w5',
    title: '独立 SaaS 产品',
    category: 'SaaS',
    cover:
      'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=saas%20subscription%20management%20dashboard%2C%20clean%20modern%20UI%2C%20analytics%20panel%2C%20pastel%20gradient%2C%20productivity%20app%20design&image_size=landscape_16_9',
    description:
      '订阅管理 + AI 内容生成一站式解决方案，持续盈利中。独立完成从产品研发到上线的全流程，首月实现正向现金流。',
    year: '2022',
    link: 'https://example.com',
    tags: ['Next.js', 'Stripe', 'OpenAI API', 'PostgreSQL'],
  },
]

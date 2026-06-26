import type { Profile } from './types'

export const profile: Profile = {
  name: '李明远',
  nameEn: 'Li Mingyuan',
  title: '独立开发者 · AI 产品创造者',
  tagline: '专注于 AI 应用、Web 产品与自动化工具开发。持续创造有价值的数字产品，服务真实用户需求。',
  bio: '我是一名独立开发者，专注于将 AI 技术转化为真实可用的产品。相信好的产品应该解决真实痛点，而不是单纯炫技。从一个产品的 0 到 1，我独立完成设计、开发、上线到运营的全流程，这让我对产品有着全链路的理解与掌控力。在 AI 浪潮中，我选择做一个产品创造者，而不只是工具的使用者。',
  avatar:
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=portrait%20of%20an%20asian%20indie%20developer%2C%20soft%20warm%20lighting%2C%20minimal%20cream%20background%2C%20friendly%20thoughtful%20expression%2C%20editorial%20photography%2C%20pastel%20tones&image_size=portrait_4_3',
  email: 'hello@example.com',
  location: '中国 · 北京',
  availability: '通常 24 小时内回复',
  stats: [
    { label: '上线产品', value: '12+' },
    { label: '服务用户', value: '50K+' },
    { label: '开源 Stars', value: '3.2K' },
  ],
  socials: [
    { name: 'GitHub', url: 'https://github.com', icon: 'github' },
    { name: '邮箱', url: 'mailto:hello@example.com', icon: 'mail' },
  ],
}

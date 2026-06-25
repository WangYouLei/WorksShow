import type { Profile } from './types'

export const profile: Profile = {
  name: '王佑磊',
  nameEn: 'Wang Youlei',
  title: 'Java后端/全栈',
  tagline: '以代码构建可靠后端，以学习拓展技术边界。',
  bio: 'Java后端开发方向，熟悉 Spring Boot、MyBatis、MySQL、Redis 等主流技术栈，具备良好的数据库设计与接口开发能力。对前端也有一定涉猎，能够使用 Vue 3 构建基础页面、完成 H5 页面开发，虽不算精通但能独立完成前后端联调。热爱技术，保持学习，追求代码的质量与可维护性。',
  avatar:
    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=portrait%20of%20an%20asian%20creative%20professional%2C%20soft%20warm%20lighting%2C%20minimal%20dark%20background%2C%20thoughtful%20expression%2C%20editorial%20photography%20style%2C%20amber%20tones&image_size=portrait_4_3',
  email: 'hello@mobai.studio',
  location: '广东 · 广州',
  availability: '寻找实习机会',
  stats: [
    { label: '项目交付', value: '60+' },
    { label: '从业年限', value: '8 年' },
    { label: '合作客户', value: '24 家' },
  ],
  socials: [
    { name: 'GitHub', url: 'https://github.com', icon: 'github' },
    { name: '邮箱', url: 'mailto:hello@mobai.studio', icon: 'mail' },
  ],
}

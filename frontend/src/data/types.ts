// 数据类型定义

export interface Profile {
  /** 头像URL(存于 user 表,模板优先显示,无则回落姓名首字母) */
  avatar?: string
  name: string
  nameEn: string
  title: string
  tagline: string
  bio: string
  email: string
  location: string
  availability: string
  stats: { id: string; label: string; value: string }[]
  socials: { id: string; name: string; url: string; icon: string }[]
}

export interface Work {
  id: string
  title: string
  category: string
  cover: string
  /** 作品视频URL(OSS直传,有视频时模板渲染video标签) */
  videoUrl?: string
  description: string
  year: string
  link?: string
  tags: string[]
  highlight?: boolean
}

export interface Experience {
  id: string
  period: string
  title: string
  organization: string
  description: string
  type: 'work' | 'education'
}

export interface Skill {
  id: string
  category: string
  categoryEn: string
  items: string[]
}

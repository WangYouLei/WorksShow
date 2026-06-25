// 数据类型定义

export interface Profile {
  name: string
  nameEn: string
  title: string
  tagline: string
  bio: string
  avatar: string
  email: string
  location: string
  availability: string
  stats: { label: string; value: string }[]
  socials: { name: string; url: string; icon: string }[]
}

export interface Work {
  id: string
  title: string
  category: string
  cover: string
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

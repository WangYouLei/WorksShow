import type { Skill } from './types'

export const skills: Skill[] = [
  {
    id: 's1',
    category: '前端开发',
    categoryEn: 'Frontend',
    items: ['React', 'Next.js', 'TypeScript', 'Tailwind CSS', 'Motion', 'Three.js'],
  },
  {
    id: 's2',
    category: '后端开发',
    categoryEn: 'Backend',
    items: ['Node.js', 'Python', 'FastAPI', 'GraphQL', 'Prisma', 'tRPC'],
  },
  {
    id: 's3',
    category: 'AI 开发',
    categoryEn: 'AI',
    items: ['OpenAI API', 'LangChain', 'Pinecone', 'Hugging Face', 'Replicate', 'Vercel AI SDK'],
  },
  {
    id: 's4',
    category: '基础设施',
    categoryEn: 'Infrastructure',
    items: ['Supabase', 'PostgreSQL', 'Redis', 'Docker', 'Cloudflare', 'Vercel'],
  },
]

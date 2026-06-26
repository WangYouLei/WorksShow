<script setup lang="ts">
import { useRouter } from 'vue-router'
import { templates } from '@/templates/registry'

const router = useRouter()

const useTemplate = (id: string) => {
  router.push({ name: 'editor', params: { templateId: id } })
}
</script>

<template>
  <div class="gallery">
    <header class="gallery-header">
      <h1>选择一个模板开始</h1>
      <p>
        选好模板后即可在线编辑你的个人作品集，最后导出为单文件 HTML
        下载到本地，双击即可打开。
      </p>
    </header>

    <div class="gallery-grid">
      <article
        v-for="t in templates"
        :key="t.id"
        class="template-card"
        @click="useTemplate(t.id)"
      >
        <div class="card-cover" :class="'cover-' + t.id">
          <img v-if="t.cover" :src="t.cover" :alt="t.name" loading="lazy" />
          <div v-else class="cover-placeholder">
            <span class="cover-name">{{ t.name }}</span>
          </div>
          <span class="card-badge">模板</span>
        </div>
        <div class="card-body">
          <h2>{{ t.name }}</h2>
          <p class="card-desc">{{ t.description }}</p>
          <div class="card-footer">
            <span class="author">作者：{{ t.author }}</span>
            <button class="use-btn" type="button">使用此模板 →</button>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>

<style scoped>
.gallery {
  min-height: 100vh;
  background: #0a0a0f;
  color: #f5f1e8;
  padding: 80px 24px 96px;
}
.gallery-header {
  max-width: 720px;
  margin: 0 auto 56px;
  text-align: center;
}
.gallery-header h1 {
  font-size: clamp(32px, 5vw, 52px);
  font-weight: 700;
  letter-spacing: 2px;
  margin: 0 0 16px;
  background: linear-gradient(120deg, #f5f1e8 0%, #d4a574 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.gallery-header p {
  margin: 0;
  font-size: 15px;
  line-height: 1.8;
  color: rgba(245, 241, 232, 0.6);
}
.gallery-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 28px;
}
.template-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(212, 165, 116, 0.15);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s ease, border-color 0.3s ease, box-shadow 0.3s ease;
}
.template-card:hover {
  transform: translateY(-4px);
  border-color: rgba(212, 165, 116, 0.5);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
}
.card-cover {
  position: relative;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background: #111;
}
.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.template-card:hover .card-cover img {
  transform: scale(1.05);
}
.cover-placeholder {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
}
.cover-name {
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 4px;
  opacity: 0.85;
}
.cover-mo-yun {
  background: linear-gradient(135deg, #0a0a0f 0%, #2a2418 55%, #d4a574 130%);
}
.cover-mo-yun .cover-name {
  color: #f5f1e8;
}
.cover-pastel {
  background: linear-gradient(135deg, #f6dcdc 0%, #f4ede4 50%, #dceee4 120%);
}
.cover-pastel .cover-name {
  color: #3d3536;
}
.cover-aurora {
  background:
    radial-gradient(circle at 75% 25%, rgba(129, 140, 248, 0.45), transparent 55%),
    radial-gradient(circle at 20% 80%, rgba(56, 189, 248, 0.35), transparent 55%),
    linear-gradient(135deg, #050507 0%, #0a0a0f 100%);
}
.cover-aurora .cover-name {
  color: #ffffff;
}
.card-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 10px;
  font-size: 12px;
  background: rgba(10, 10, 15, 0.7);
  border: 1px solid rgba(212, 165, 116, 0.4);
  border-radius: 4px;
  color: #d4a574;
  backdrop-filter: blur(4px);
}
.card-body {
  padding: 22px 22px 24px;
}
.card-body h2 {
  margin: 0 0 10px;
  font-size: 22px;
  color: #f5f1e8;
}
.card-desc {
  margin: 0 0 18px;
  font-size: 13px;
  line-height: 1.7;
  color: rgba(245, 241, 232, 0.55);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.author {
  font-size: 12px;
  color: rgba(245, 241, 232, 0.4);
}
.use-btn {
  padding: 8px 14px;
  font-size: 13px;
  color: #0a0a0f;
  background: #d4a574;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s ease;
  white-space: nowrap;
}
.use-btn:hover {
  background: #e3b885;
}
</style>

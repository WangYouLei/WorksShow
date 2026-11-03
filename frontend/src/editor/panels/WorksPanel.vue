<script setup lang="ts">
import { ref } from 'vue'
import { usePortfolioStore } from '@/composables/usePortfolioStore'
import OssUploader from '@/components/OssUploader.vue'
import { useOssUpload } from '@/composables/useOssUpload'

const props = defineProps<{ templateId: string }>()
const store = usePortfolioStore(props.templateId)

const addWork = () => {
  store.works.unshift({
    id: 'w' + crypto.randomUUID(),
    title: '新作品',
    category: '分类',
    cover: '',
    videoUrl: '',
    images: [],
    description: '',
    year: String(new Date().getFullYear()),
    tags: [],
    highlight: false,
  })
}
const removeWork = (i: number) => store.works.splice(i, 1)
const move = (i: number, dir: -1 | 1) => {
  const j = i + dir
  if (j < 0 || j >= store.works.length) return
  const arr = store.works
  ;[arr[i], arr[j]] = [arr[j], arr[i]]
}
const tagsText = (i: number) => store.works[i].tags.join('、')
const setTags = (i: number, val: string) => {
  store.works[i].tags = val
    .split(/[、,，]/)
    .map((s) => s.trim())
    .filter(Boolean)
}

// —— 图集多图上传(面板级单实例,逐个文件顺序上传) ——
const galleryInput = ref<HTMLInputElement | null>(null)
/** 正在为第几个作品上传图集(进度/错误提示定位用) */
const galleryWorkIndex = ref(-1)
const { uploading, progress, error, upload } = useOssUpload('image')

const pickImages = (i: number) => {
  error.value = ''
  galleryWorkIndex.value = i
  galleryInput.value?.click()
}

const onGalleryChange = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const files = Array.from(input.files ?? [])
  // 允许连续选择同一批文件重新上传
  input.value = ''
  const i = galleryWorkIndex.value
  if (!files.length || i < 0 || !store.works[i]) {
    galleryWorkIndex.value = -1
    return
  }
  const work = store.works[i]
  work.images = work.images ?? []
  for (const file of files) {
    const url = await upload(file)
    if (url) work.images.push(url)
  }
  galleryWorkIndex.value = -1
}

const removeImage = (i: number, j: number) => {
  store.works[i].images?.splice(j, 1)
}
</script>

<template>
  <div class="ef-panel">
    <div class="ef-section-head">
      <h4>作品列表</h4>
      <button class="ef-add-btn" type="button" @click="addWork">+ 添加作品</button>
    </div>

    <details v-for="(work, i) in store.works" :key="work.id" class="ef-item">
      <summary>
        <span class="ef-item-title">{{ work.title || '未命名' }}</span>
        <span class="ef-item-meta">{{ work.category }} · {{ work.year }}</span>
      </summary>
      <div class="ef-item-body">
        <div class="ef-row-two">
          <label class="ef-field"><span>标题</span><input v-model="work.title" class="ef-input" type="text" /></label>
          <label class="ef-field"><span>分类</span><input v-model="work.category" class="ef-input" type="text" /></label>
        </div>
        <div class="ef-field">
          <span>封面图（上传或粘贴 URL，无封面时取图集第一张）</span>
          <OssUploader v-model="work.cover" type="image" />
        </div>
        <div class="ef-field">
          <span>图集（可上传多张，详情弹窗中滑动浏览）</span>
          <div class="ef-gallery">
            <div v-for="(img, j) in work.images || []" :key="img + j" class="ef-gallery-item">
              <img :src="img" alt="图集图片" />
              <button class="ef-gallery-remove" type="button" title="删除" @click="removeImage(i, j)">✕</button>
            </div>
            <button
              class="ef-gallery-add"
              type="button"
              :disabled="uploading && galleryWorkIndex === i"
              @click="pickImages(i)"
            >
              {{ uploading && galleryWorkIndex === i ? `上传中 ${progress}%` : '+ 添加图片' }}
            </button>
          </div>
          <p v-if="error && galleryWorkIndex === i" class="ef-gallery-error">{{ error }}</p>
        </div>
        <div class="ef-field">
          <span>作品视频（可选，单个，详情弹窗优先播放视频）</span>
          <OssUploader v-model="work.videoUrl" type="video" />
        </div>
        <label class="ef-field"><span>描述</span><textarea v-model="work.description" class="ef-textarea"></textarea></label>
        <div class="ef-row-two">
          <label class="ef-field"><span>年份</span><input v-model="work.year" class="ef-input" type="text" /></label>
          <label class="ef-field"><span>外链（可选）</span><input v-model="work.link" class="ef-input" type="text" /></label>
        </div>
        <label class="ef-field"><span>标签（用、或 , 分隔）</span><input :value="tagsText(i)" class="ef-input" type="text" @input="setTags(i, ($event.target as HTMLInputElement).value)" /></label>
        <label class="ef-checkbox"><input v-model="work.highlight" type="checkbox" /> 标记为高亮（占双列宽）</label>
        <div class="ef-item-actions">
          <button class="ef-move-btn" type="button" @click="move(i, -1)">上移</button>
          <button class="ef-move-btn" type="button" @click="move(i, 1)">下移</button>
          <button class="ef-remove-btn" type="button" @click="removeWork(i)">删除</button>
        </div>
      </div>
    </details>

    <p v-if="!store.works.length" class="ef-empty">暂无作品，点击右上角添加</p>

    <input ref="galleryInput" type="file" hidden multiple accept="image/*" @change="onGalleryChange" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useOssUpload } from '@/composables/useOssUpload'

/**
 * 通用 OSS 上传组件(图片/视频)
 * <p>
 * v-model 绑定 URL 字符串:上传成功后回填 OSS 公开地址;
 * 也可手动输入/粘贴 URL(与上传并存)。上传中显示进度条,
 * 预览区按类型渲染 img/video。校验与上传逻辑见 useOssUpload。
 */
const props = withDefaults(
  defineProps<{
    /** 当前 URL(双向绑定,上传成功后回填) */
    modelValue: string
    /** 媒体类型:image-图片 video-视频 */
    type: 'image' | 'video'
    /** 是否显示 URL 手填输入框(默认显示;头像等场景可关闭) */
    showUrlInput?: boolean
    /** 上传按钮文案(默认"上传图片/上传视频") */
    buttonText?: string
  }>(),
  {
    modelValue: '',
    showUrlInput: true,
    buttonText: '',
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const { uploading, progress, error, upload, label } = useOssUpload(props.type)
const fileInput = ref<HTMLInputElement | null>(null)

const buttonTextResolved = props.buttonText || (props.type === 'image' ? '上传图片' : '上传视频')

const pick = () => {
  error.value = ''
  fileInput.value?.click()
}

const onFileChange = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  // 允许连续选择同一文件重新上传
  input.value = ''
  if (!file) return
  const url = await upload(file)
  if (url) {
    emit('update:modelValue', url)
  }
}

const onManualInput = (e: Event) => {
  emit('update:modelValue', (e.target as HTMLInputElement).value)
}

const clear = () => {
  error.value = ''
  emit('update:modelValue', '')
}
</script>

<template>
  <div class="oss-uploader">
    <div class="oss-row">
      <input
        v-if="showUrlInput"
        :value="modelValue"
        class="oss-input"
        type="text"
        :placeholder="`粘贴 URL 或点击${buttonTextResolved}`"
        @input="onManualInput"
      />
      <button class="oss-btn" type="button" :disabled="uploading" @click="pick">
        {{ uploading ? `上传中 ${progress}%` : buttonTextResolved }}
      </button>
      <button v-if="modelValue" class="oss-clear" type="button" title="清除" @click="clear">✕</button>
    </div>

    <div v-if="uploading" class="oss-progress">
      <div class="oss-progress-bar" :style="{ width: `${progress}%` }"></div>
    </div>
    <p v-if="error" class="oss-error">{{ error }}</p>
    <p class="oss-hint">支持 {{ label }}</p>

    <div v-if="modelValue" class="oss-preview">
      <img v-if="type === 'image'" :src="modelValue" alt="预览" />
      <video v-else :src="modelValue" controls preload="metadata"></video>
    </div>

    <input
      ref="fileInput"
      type="file"
      hidden
      :accept="type === 'image' ? 'image/*' : 'video/*'"
      @change="onFileChange"
    />
  </div>
</template>

<style scoped>
.oss-row {
  display: flex;
  gap: 6px;
  align-items: center;
}
.oss-input {
  flex: 1;
  min-width: 0;
  padding: 7px 10px;
  font-size: 13px;
  color: #f5f1e8;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(245, 241, 232, 0.15);
  border-radius: 6px;
  outline: none;
}
.oss-input:focus {
  border-color: #d4a574;
}
.oss-btn {
  padding: 7px 12px;
  font-size: 13px;
  color: #d4a574;
  white-space: nowrap;
  background: rgba(212, 165, 116, 0.1);
  border: 1px solid rgba(212, 165, 116, 0.3);
  border-radius: 6px;
  cursor: pointer;
}
.oss-btn:hover:not(:disabled) {
  background: rgba(212, 165, 116, 0.2);
}
.oss-btn:disabled {
  opacity: 0.6;
  cursor: default;
}
.oss-clear {
  padding: 7px 9px;
  font-size: 13px;
  color: rgba(245, 241, 232, 0.6);
  background: none;
  border: 1px solid rgba(245, 241, 232, 0.15);
  border-radius: 6px;
  cursor: pointer;
}
.oss-clear:hover {
  color: #c8553d;
  border-color: rgba(200, 85, 61, 0.5);
}
.oss-progress {
  height: 4px;
  margin-top: 8px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 2px;
}
.oss-progress-bar {
  height: 100%;
  background: #d4a574;
  transition: width 0.2s;
}
.oss-error {
  margin: 6px 0 0;
  font-size: 12px;
  color: #c8553d;
}
.oss-hint {
  margin: 4px 0 0;
  font-size: 12px;
  color: rgba(245, 241, 232, 0.4);
}
.oss-preview {
  margin-top: 8px;
}
.oss-preview img,
.oss-preview video {
  width: 100%;
  max-height: 180px;
  object-fit: contain;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 6px;
}
</style>

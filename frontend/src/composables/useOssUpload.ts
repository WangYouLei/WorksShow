import { ref } from 'vue'
import { uploadToOss, mediaConstraint, type OssMediaType } from '@/api/oss'

/**
 * OSS 上传状态管理
 * <p>
 * 封装"选文件 → 校验 → 申请凭证 → 直传 → 返回 URL"全流程,
 * 暴露 uploading/progress/error 响应式状态供 UI 渲染进度条与提示。
 * 每个上传位(封面/视频/头像)独立创建一份实例,互不干扰。
 *
 * @param type 媒体类型:image-图片 video-视频
 */
export function useOssUpload(type: OssMediaType) {
  /** 是否上传中 */
  const uploading = ref(false)
  /** 上传进度(0-100) */
  const progress = ref(0)
  /** 最近一次错误信息(空串表示无错误) */
  const error = ref('')

  /**
   * 上传文件到 OSS
   * @return 成功返回公开访问 URL,失败返回 null(错误写入 error)
   */
  async function upload(file: File): Promise<string | null> {
    if (uploading.value) return null
    error.value = ''
    uploading.value = true
    progress.value = 0
    try {
      const url = await uploadToOss(file, type, (pct) => {
        progress.value = pct
      })
      return url
    } catch (e) {
      error.value = (e as Error).message || '上传失败,请重试'
      return null
    } finally {
      uploading.value = false
    }
  }

  return { uploading, progress, error, upload, label: mediaConstraint(type).label }
}

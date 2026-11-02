import request from './request'

/**
 * 阿里云 OSS 直传(后端签名 + 前端直传)
 * <p>
 * 流程:申请预签名 URL(JWT 接口)→ XHR PUT 直传 OSS(带进度)→
 * 拿 publicUrl 作为普通字段走各业务接口保存。
 * 上传走裸 XHR 而非 axios 实例:大视频耗时远超 axios 全局 15s 超时。
 */

/** 上传凭证(后端 POST /oss/upload-token 返回) */
export interface OssUploadToken {
  /** 预签名上传 URL(仅对该 objectKey 有效,限 PUT,按时效过期) */
  uploadUrl: string
  /** 对象存储 key */
  objectKey: string
  /** 上传成功后的公开访问 URL */
  publicUrl: string
  /** 签名时纳入的 Content-Type(PUT 必须携带完全一致的值,否则 403) */
  contentType: string
  /** 凭证有效期(秒) */
  expireSeconds: number
}

/** 媒体类型:image-图片 video-视频 */
export type OssMediaType = 'image' | 'video'

/** 图片大小上限(10MB) */
export const IMAGE_MAX_SIZE = 10 * 1024 * 1024

/** 视频大小上限(200MB) */
export const VIDEO_MAX_SIZE = 200 * 1024 * 1024

/** 图片扩展名白名单(与后端 OssServiceImpl 保持一致) */
export const IMAGE_EXTS = ['jpg', 'jpeg', 'png', 'webp', 'gif']

/** 视频扩展名白名单(与后端 OssServiceImpl 保持一致) */
export const VIDEO_EXTS = ['mp4', 'webm']

/** 按媒体类型取大小上限与扩展名白名单 */
export function mediaConstraint(type: OssMediaType): {
  maxSize: number
  exts: string[]
  label: string
} {
  if (type === 'image') {
    return { maxSize: IMAGE_MAX_SIZE, exts: IMAGE_EXTS, label: '图片(jpg/png/webp/gif,≤10MB)' }
  }
  return { maxSize: VIDEO_MAX_SIZE, exts: VIDEO_EXTS, label: '视频(mp4/webm,≤200MB)' }
}

/** 提取小写扩展名(不含点号),无扩展名返回空串 */
export function fileExt(fileName: string): string {
  const idx = fileName.lastIndexOf('.')
  return idx < 0 ? '' : fileName.slice(idx + 1).toLowerCase()
}

/** 申请上传凭证 */
export function getUploadToken(type: OssMediaType, fileName: string) {
  return request.post<OssUploadToken>('/oss/upload-token', { type, fileName }) as unknown as Promise<OssUploadToken>
}

/**
 * XHR PUT 直传文件到 OSS(带上传进度)。
 * <p>
 * 不走 axios 实例:大文件上传远超全局超时,且 OSS 直传无需携带 JWT。
 * Content-Type 必须传后端签名时返回的值(而非 file.type):
 * OSS 按请求实发头重算签名,两端不一致会返回 403 SignatureDoesNotMatch。
 */
export function putToOss(
  uploadUrl: string,
  file: File,
  contentType: string,
  onProgress?: (percent: number) => void,
): Promise<void> {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    xhr.open('PUT', uploadUrl)
    xhr.setRequestHeader('Content-Type', contentType)
    xhr.upload.onprogress = (e) => {
      if (e.lengthComputable && onProgress) {
        onProgress(Math.round((e.loaded / e.total) * 100))
      }
    }
    xhr.onload = () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        resolve()
      } else {
        reject(new Error(`上传失败(HTTP ${xhr.status}),请重试`))
      }
    }
    xhr.onerror = () => reject(new Error('网络错误,上传中断,请重试'))
    xhr.send(file)
  })
}

/**
 * 一站式上传:校验类型/大小 → 申请凭证 → 直传 OSS。
 * @return 上传成功后的公开访问 URL
 * @throws 校验或上传失败时抛出带可读信息的 Error
 */
export async function uploadToOss(
  file: File,
  type: OssMediaType,
  onProgress?: (percent: number) => void,
): Promise<string> {
  // 1. 本地校验(与后端白名单一致,提前拦截)
  const { maxSize, exts, label } = mediaConstraint(type)
  const ext = fileExt(file.name)
  if (!exts.includes(ext)) {
    throw new Error(`不支持的文件格式,仅支持 ${label}`)
  }
  if (file.size > maxSize) {
    throw new Error(`文件超过大小限制,仅支持 ${label}`)
  }
  // 2. 申请凭证 + 直传(Content-Type 用签名时的值,保证签名校验一致)
  const token = await getUploadToken(type, file.name)
  await putToOss(token.uploadUrl, file, token.contentType, onProgress)
  return token.publicUrl
}

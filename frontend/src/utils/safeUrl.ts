/**
 * 用户输入 URL 的协议白名单校验。
 * <p>
 * 作品外链/社交链接为自由文本,若原样渲染为 href,
 * javascript:/data:/vbscript: 等危险协议可实现存储型 XSS
 * (部署页与主应用同源时,点击即可窃取访问者 localStorage 中的 token)。
 * 仅放行 http(s)/mailto/tel 与站内相对路径;
 * 不合法时返回 undefined(Vue 对 null/undefined 不渲染 href,链接退化为不可跳转)。
 */

/** 允许的绝对 URL 协议 */
const SAFE_SCHEME = /^(https?|mailto|tel):/i

/**
 * 校验并返回安全的 URL。
 * @param url 用户输入的原始 URL
 * @return 安全的 URL;为空或不合法时返回 undefined
 */
export function safeUrl(url: string | null | undefined): string | undefined {
  if (!url) return undefined
  const trimmed = url.trim()
  if (!trimmed) return undefined
  // 含协议前缀的绝对 URL:仅允许白名单协议
  if (/^[a-zA-Z][a-zA-Z0-9+.-]*:/.test(trimmed)) {
    return SAFE_SCHEME.test(trimmed) ? trimmed : undefined
  }
  // 协议相对 URL(//evil.com)拒绝;站内相对路径(/path、#anchor)放行
  if (trimmed.startsWith('//')) return undefined
  return trimmed
}

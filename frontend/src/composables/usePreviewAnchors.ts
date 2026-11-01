import { onMounted, onUnmounted, type Ref } from 'vue'

/**
 * 应用内预览锚点拦截
 * 模板预览是内嵌在应用页面里的组件，而应用使用 hash 路由（#/editor/xxx）。
 * 若放任 <a href="#works"> 默认行为，hash 会被 Vue Router 解析成 /works 路由导致页面跳走。
 * 这里拦截容器内所有 # 锚点点击，改为容器内平滑滚动，不改变应用 URL。
 * 导出的独立 HTML 不走此逻辑，保留浏览器标准锚点行为。
 */
export function usePreviewAnchors(root?: Ref<HTMLElement | null>) {
  let scopeEl: HTMLElement | Document = document

  const onClick = (e: MouseEvent) => {
    // 只处理左键无修饰键的点击
    if (e.defaultPrevented || e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return
    const anchor = (e.target as HTMLElement | null)?.closest?.('a')
    if (!anchor) return
    const href = anchor.getAttribute('href') || ''
    if (!href.startsWith('#') || href === '#') return
    const target = document.getElementById(decodeURIComponent(href.slice(1)))
    if (!target || !scopeEl.contains(target) || !scopeEl.contains(anchor)) return
    e.preventDefault()
    target.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  onMounted(() => {
    scopeEl = root?.value ?? document
    // capture 阶段监听，抢在浏览器默认锚点导航之前
    scopeEl.addEventListener('click', onClick, true)
  })

  onUnmounted(() => {
    scopeEl.removeEventListener('click', onClick, true)
  })
}

import { onMounted, onUnmounted, type Ref } from 'vue'

/**
 * 应用内预览作品弹窗交互（对齐导出 runtime 的行为）。
 * 除墨韵外的模板预览组件把作品弹窗渲染为隐藏静态 DOM（display:none），
 * 开合逻辑只存在于导出页 runtime 中。这里在预览容器上注入等价逻辑：
 * 点击 [data-work-id] 卡片 → 显示对应 [data-modal-id] 弹窗（fixed 容器，
 * 经 PreviewFrame 外壳 transform 包含块锚定在可视预览区），
 * 点击关闭按钮/遮罩或按 Esc → 隐藏。
 * 墨韵预览使用自己的 Vue WorkModal（无 data-modal-id DOM），此处自动跳过不冲突。
 */
export function usePreviewModals(root?: Ref<HTMLElement | null>) {
  let scopeEl: HTMLElement | Document = document
  let openModal: HTMLElement | null = null

  const show = (m: HTMLElement) => {
    openModal = m
    m.style.display = 'flex'
  }
  const hide = (m: HTMLElement) => {
    m.style.display = 'none'
    if (openModal === m) openModal = null
  }

  const onClick = (e: MouseEvent) => {
    const target = e.target as HTMLElement
    // 关闭：弹窗内关闭按钮或遮罩
    if (openModal && openModal.contains(target)) {
      if (target.closest('.modal-close') || target.classList.contains('modal-overlay')) {
        hide(openModal)
        return
      }
    }
    // 打开：作品卡片
    const card = target.closest('[data-work-id]') as HTMLElement | null
    if (card) {
      const id = card.getAttribute('data-work-id')
      const modal = id ? scopeEl.querySelector(`[data-modal-id="${id}"]`) : null
      if (modal instanceof HTMLElement) {
        e.preventDefault()
        show(modal)
      }
    }
  }
  const onKeydown = (e: KeyboardEvent) => {
    if (e.key === 'Escape' && openModal) hide(openModal)
  }

  onMounted(() => {
    scopeEl = root?.value ?? document
    scopeEl.addEventListener('click', onClick)
    window.addEventListener('keydown', onKeydown)
  })
  onUnmounted(() => {
    scopeEl.removeEventListener('click', onClick)
    window.removeEventListener('keydown', onKeydown)
  })
}

import { onMounted, onUnmounted, type Ref } from 'vue'

/**
 * 滚动渐入组合式函数
 * 监听带有 .reveal 类的元素，进入视口时添加 .is-visible
 */
export function useScrollReveal(root?: Ref<HTMLElement | null>) {
  let observer: IntersectionObserver | null = null

  const observe = () => {
    const scope = root?.value ?? document
    const targets = scope.querySelectorAll<HTMLElement>('.reveal')

    if (!('IntersectionObserver' in window)) {
      targets.forEach((el) => el.classList.add('is-visible'))
      return
    }

    observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible')
            observer?.unobserve(entry.target)
          }
        })
      },
      { threshold: 0.12, rootMargin: '0px 0px -8% 0px' },
    )

    targets.forEach((el) => observer?.observe(el))
  }

  onMounted(() => {
    // 等待 DOM 完整渲染
    requestAnimationFrame(observe)
  })

  onUnmounted(() => {
    observer?.disconnect()
  })
}

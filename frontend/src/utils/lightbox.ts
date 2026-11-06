/**
 * 图片放大查看(灯箱)。
 * <p>
 * 单例 DOM 覆盖层:点击遮罩/关闭按钮/Esc 关闭,Esc 在捕获阶段拦截,
 * 避免同时关掉底下的作品弹窗。
 * 两个使用方共享同一份实现:
 * - 编辑预览(Vue):WorkMedia/WorkModal 直接 import 调用;
 * - 导出静态页:mediaCarousel.ts 通过 toString() 把本函数注入 runtime,
 *   由 LIGHTBOX_WIRING_JS 委托捕获 .wm-zoom 点击。
 * 注意:函数必须保持纯 DOM 实现(不引用模块作用域),否则注入导出页后失效。
 */
export function openLightbox(src: string): void {
  let box = document.getElementById('ws-lightbox')
  if (!box) {
    box = document.createElement('div')
    box.id = 'ws-lightbox'
    const el = box
    el.style.cssText =
      'position:fixed;inset:0;z-index:300;display:none;align-items:center;justify-content:center;' +
      'background:rgba(5,5,8,0.92);cursor:zoom-out;'
    const img = document.createElement('img')
    img.alt = ''
    img.style.cssText =
      'max-width:92vw;max-height:92vh;object-fit:contain;border-radius:8px;' +
      'box-shadow:0 40px 80px -20px rgba(0,0,0,0.6);'
    const close = document.createElement('button')
    close.type = 'button'
    close.setAttribute('aria-label', '关闭')
    close.textContent = '✕'
    close.style.cssText =
      'position:absolute;top:16px;right:16px;width:40px;height:40px;font-size:18px;' +
      'color:#f5f1e8;background:rgba(10,10,15,0.6);border:1px solid rgba(245,241,232,0.2);' +
      'border-radius:50%;cursor:pointer;'
    const hide = () => {
      el.style.display = 'none'
    }
    el.addEventListener('click', (e) => {
      if (e.target === el) hide()
    })
    close.addEventListener('click', hide)
    document.addEventListener(
      'keydown',
      (e) => {
        if (e.key === 'Escape' && el.style.display !== 'none') {
          e.stopPropagation()
          hide()
        }
      },
      true,
    )
    el.appendChild(img)
    el.appendChild(close)
    document.body.appendChild(el)
  }
  const img = box.querySelector('img')
  if (img) img.src = src
  box.style.display = 'flex'
}

/**
 * 导出页灯箱接线脚本(随 MEDIA_CAROUSEL_JS 注入各模板 runtime)。
 * <p>
 * 捕获阶段委托 .wm-zoom 点击:先于卡片/弹窗的冒泡处理器接管事件;
 * 当前图片来源按优先级:轮播当前可见帧 → 弹窗图集主图 → 按钮 data-src。
 */
export const LIGHTBOX_WIRING_JS = `
  var openLightbox = (${openLightbox.toString()});
  document.addEventListener('click', function (e) {
    var btn = e.target && e.target.closest ? e.target.closest('.wm-zoom') : null;
    if (!btn) return;
    var src = null;
    var media = btn.closest('[data-media]');
    if (media) {
      var slides = media.querySelectorAll('.wm-item');
      for (var i = 0; i < slides.length; i++) {
        var s = slides[i];
        if (s.style.display !== 'none' && s.tagName === 'IMG') { src = s.getAttribute('src'); break; }
      }
    }
    if (!src) {
      var gal = btn.closest('[data-gallery]');
      var main = gal ? gal.querySelector('.gallery-main') : null;
      if (main) src = main.getAttribute('src');
    }
    if (!src) src = btn.getAttribute('data-src');
    if (src) { e.stopPropagation(); e.preventDefault(); openLightbox(src); }
  }, true);
`

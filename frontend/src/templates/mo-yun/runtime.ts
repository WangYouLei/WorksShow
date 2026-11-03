import { MEDIA_CAROUSEL_JS } from '../mediaCarousel'

/** 导出页面运行时脚本（原生 JS，还原交互：导航栏滚动、渐入、作品弹窗、媒体轮播、回到顶部） */
export const RUNTIME_JS = `
(function(){
  // 导航栏滚动样式
  var navbar = document.querySelector('.navbar');
  if (navbar) {
    var onScroll = function(){ navbar.classList.toggle('is-scrolled', window.scrollY > 50); };
    window.addEventListener('scroll', onScroll, {passive:true});
    onScroll();
  }

  // 滚动渐入
  var reveals = document.querySelectorAll('.reveal');
  if ('IntersectionObserver' in window && reveals.length) {
    var io = new IntersectionObserver(function(entries){
      entries.forEach(function(e){
        if (e.isIntersecting) { e.target.classList.add('is-visible'); io.unobserve(e.target); }
      });
    }, {threshold:0.12, rootMargin:'0px 0px -8% 0px'});
    reveals.forEach(function(el){ io.observe(el); });
  } else {
    reveals.forEach(function(el){ el.classList.add('is-visible'); });
  }

  // 作品卡点击 -> 显示对应模态框
  document.querySelectorAll('[data-work-id]').forEach(function(card){
    card.addEventListener('click', function(){
      var id = card.getAttribute('data-work-id');
      var modal = document.querySelector('[data-modal-id="' + id + '"]');
      if (modal) {
        modal.style.display = '';
        document.body.style.overflow = 'hidden';
      }
    });
  });

  // 模态框关闭
  function closeModal(m){ m.style.display = 'none'; document.body.style.overflow = ''; }
  document.querySelectorAll('.export-modal').forEach(function(m){
    var close = m.querySelector('.modal-close');
    var overlay = m.querySelector('.modal-overlay');
    if (close) close.addEventListener('click', function(){ closeModal(m); });
    if (overlay) overlay.addEventListener('click', function(e){ if (e.target === overlay) closeModal(m); });
  });
  document.addEventListener('keydown', function(e){
    if (e.key === 'Escape') {
      document.querySelectorAll('.export-modal').forEach(function(m){
        if (m.style.display !== 'none') closeModal(m);
      });
    }
  });

  // 作品图集浏览(主图切换 + 缩略图条点击 + 左右箭头 + 触屏滑动)
  document.querySelectorAll('[data-gallery]').forEach(function(g){
    var stageImg = g.querySelector('.gallery-main');
    var counter = g.querySelector('.gallery-counter');
    var thumbs = Array.prototype.slice.call(g.querySelectorAll('.gallery-thumb'));
    if (!stageImg || !thumbs.length) return;
    var urls = thumbs.map(function(t){
      var im = t.querySelector('img');
      return im ? im.getAttribute('src') : '';
    });
    var idx = 0;
    function render(){
      stageImg.setAttribute('src', urls[idx]);
      thumbs.forEach(function(t, i){ t.classList.toggle('is-active', i === idx); });
      if (counter) counter.textContent = (idx + 1) + ' / ' + urls.length;
      if (thumbs[idx] && thumbs[idx].scrollIntoView) {
        thumbs[idx].scrollIntoView({ block: 'nearest', inline: 'nearest' });
      }
    }
    thumbs.forEach(function(t){
      t.addEventListener('click', function(){
        idx = parseInt(t.getAttribute('data-index'), 10) || 0;
        render();
      });
    });
    var prev = g.querySelector('.gallery-prev');
    var next = g.querySelector('.gallery-next');
    if (prev) prev.addEventListener('click', function(){ idx = (idx - 1 + urls.length) % urls.length; render(); });
    if (next) next.addEventListener('click', function(){ idx = (idx + 1) % urls.length; render(); });
    var stage = g.querySelector('.gallery-stage');
    if (stage) {
      var sx = null;
      stage.addEventListener('touchstart', function(e){ sx = e.touches[0].clientX; }, { passive: true });
      stage.addEventListener('touchend', function(e){
        if (sx === null) return;
        var dx = e.changedTouches[0].clientX - sx;
        if (Math.abs(dx) > 40) { idx = (idx + (dx < 0 ? 1 : -1) + urls.length) % urls.length; render(); }
        sx = null;
      }, { passive: true });
    }
  });

  // 作品卡片媒体轮播(视频首帧 + 图集,箭头切换 + 图片帧自动轮播)
  ${MEDIA_CAROUSEL_JS}

  // 回到顶部
  var backTop = document.querySelector('.back-top');
  if (backTop) backTop.addEventListener('click', function(){ window.scrollTo({top:0, behavior:'smooth'}); });
})();
`

/** 导出页面运行时脚本（原生 JS，还原交互：导航栏滚动、渐入、作品弹窗、回到顶部） */
export const RUNTIME_JS = `
(function(){
  // 导航栏滚动样式
  var navbar = document.querySelector('.d-navbar');
  if (navbar) {
    var onScroll = function(){ navbar.classList.toggle('is-scrolled', window.scrollY > 40); };
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
        modal.classList.add('is-open');
        document.body.style.overflow = 'hidden';
      }
    });
  });

  // 模态框关闭
  function closeModal(m){ m.classList.remove('is-open'); document.body.style.overflow = ''; }
  document.querySelectorAll('.d-modal').forEach(function(m){
    var close = m.querySelector('.modal-close');
    var overlay = m.querySelector('.modal-overlay');
    if (close) close.addEventListener('click', function(){ closeModal(m); });
    if (overlay) overlay.addEventListener('click', function(e){ if (e.target === overlay) closeModal(m); });
  });
  document.addEventListener('keydown', function(e){
    if (e.key === 'Escape') {
      document.querySelectorAll('.d-modal.is-open').forEach(function(m){ closeModal(m); });
    }
  });

  // 回到顶部
  var backTop = document.querySelector('.back-top');
  if (backTop) backTop.addEventListener('click', function(){ window.scrollTo({top:0, behavior:'smooth'}); });
})();
`

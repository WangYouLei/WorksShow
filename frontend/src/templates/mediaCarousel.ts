/**
 * 导出页作品媒体轮播共享脚本(各模板 runtime 注入)。
 * 与编辑预览内 WorkMedia.vue 行为保持一致:
 * - 视频作为第一帧,图集图片随后(全部帧都在静态 DOM 中,靠 display 切换)
 * - 箭头切换(阻止冒泡,避免误触卡片打开弹窗),计数联动
 * - 图片帧自动轮播,视频帧与悬停时暂停
 */
export const MEDIA_CAROUSEL_JS = `
  document.querySelectorAll('[data-media]').forEach(function(wm){
    var slides = Array.prototype.slice.call(wm.querySelectorAll('.wm-item'));
    if (slides.length < 2) return;
    var counter = wm.querySelector('.wm-counter');
    var prev = wm.querySelector('.wm-prev');
    var next = wm.querySelector('.wm-next');
    var idx = 0;
    var hovering = false;
    function render(){
      slides.forEach(function(s, i){ s.style.display = (i === idx) ? '' : 'none'; });
      if (counter) counter.textContent = (idx + 1) + ' / ' + slides.length;
    }
    function step(dir){ idx = (idx + dir + slides.length) % slides.length; render(); }
    if (prev) prev.addEventListener('click', function(e){ e.stopPropagation(); step(-1); });
    if (next) next.addEventListener('click', function(e){ e.stopPropagation(); step(1); });
    var video = wm.querySelector('video');
    if (video) video.addEventListener('click', function(e){ e.stopPropagation(); });
    wm.addEventListener('mouseenter', function(){ hovering = true; });
    wm.addEventListener('mouseleave', function(){ hovering = false; });
    window.setInterval(function(){
      if (!hovering && slides[idx] && slides[idx].tagName !== 'VIDEO') step(1);
    }, 3500);
  });
`

/** 导出页面运行时脚本（原生 JS，还原交互：导航栏滚动、渐入、Hero 3D 视差、卡片 3D 倾斜、作品弹窗、回到顶部） */
export const RUNTIME_JS = `
(function(){
  var reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  var fine = window.matchMedia('(pointer: fine)').matches;

  // 导航栏滚动样式
  var nav = document.querySelector('.g3-nav');
  if (nav) {
    var onScroll = function(){ nav.classList.toggle('is-scrolled', window.scrollY > 30); };
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

  // Hero 3D 场景鼠标视差（rAF + lerp 平滑）
  if (fine && !reduce) {
    var stage = document.querySelector('.g3-stage');
    var scene = document.querySelector('.g3-scene');
    if (stage && scene) {
      var raf = null, tx = 0, ty = 0, cx = 0, cy = 0;
      function tick(){
        cx += (tx - cx) * 0.08;
        cy += (ty - cy) * 0.08;
        scene.style.transform = 'rotateX(' + (10 - cy * 8).toFixed(2) + 'deg) rotateY(' + (-16 + cx * 12).toFixed(2) + 'deg)';
        if (Math.abs(tx - cx) > 0.002 || Math.abs(ty - cy) > 0.002) raf = requestAnimationFrame(tick);
        else raf = null;
      }
      function wake(){ if (raf === null) raf = requestAnimationFrame(tick); }
      stage.addEventListener('mousemove', function(e){
        var r = stage.getBoundingClientRect();
        tx = ((e.clientX - r.left) / Math.max(r.width, 1) - 0.5) * 2;
        ty = ((e.clientY - r.top) / Math.max(r.height, 1) - 0.5) * 2;
        wake();
      });
      stage.addEventListener('mouseleave', function(){ tx = 0; ty = 0; wake(); });
    }
  }

  // 卡片 3D 倾斜跟随 + 高光位置（事件委托 + rAF 时间归一化插值：悬停快速跟随、离开缓慢回弹）
  if (fine && !reduce) {
    var states = [];
    var tiltRaf = null, tiltLast = 0;
    function findState(card){
      for (var k = 0; k < states.length; k++) if (states[k].el === card) return states[k];
      return null;
    }
    function tiltFrame(now){
      var dt = Math.min((now - tiltLast) / 1000, 0.05);
      tiltLast = now;
      var follow = 1 - Math.exp(-dt * 16);
      var back = 1 - Math.exp(-dt * 10);
      var glare = 1 - Math.exp(-dt * 18);
      for (var k = states.length - 1; k >= 0; k--) {
        var s = states[k];
        // 元素已从 DOM 移除：直接丢弃，避免僵尸状态空转
        if (!s.el.isConnected) { states.splice(k, 1); continue; }
        var eg = s.hover ? follow : back;
        s.rx += (s.tRx - s.rx) * eg;
        s.ry += (s.tRy - s.ry) * eg;
        s.lift += (s.tLift - s.lift) * eg;
        s.gx += (s.tGx - s.gx) * glare;
        s.gy += (s.tGy - s.gy) * glare;
        var settled = !s.hover
          && Math.abs(s.tRx - s.rx) < 0.05
          && Math.abs(s.tRy - s.ry) < 0.05
          && Math.abs(s.tLift - s.lift) < 0.1
          && Math.abs(s.tGx - s.gx) < 0.5;
        if (settled) {
          s.el.style.transform = '';
          s.el.style.removeProperty('--gx');
          s.el.style.removeProperty('--gy');
          states.splice(k, 1);
        } else {
          s.el.style.transform = 'perspective(900px) rotateX(' + s.rx.toFixed(2) + 'deg) rotateY(' + s.ry.toFixed(2) + 'deg) translateY(' + s.lift.toFixed(2) + 'px)';
          s.el.style.setProperty('--gx', s.gx.toFixed(1) + '%');
          s.el.style.setProperty('--gy', s.gy.toFixed(1) + '%');
        }
      }
      tiltRaf = states.length ? requestAnimationFrame(tiltFrame) : null;
    }
    function tiltWake(){
      if (tiltRaf === null) { tiltLast = performance.now(); tiltRaf = requestAnimationFrame(tiltFrame); }
    }
    document.addEventListener('mousemove', function(e){
      var t = e.target;
      var card = t && t.closest ? t.closest('[data-tilt]') : null;
      // 兜底：指针已不在卡片上时让所有活跃卡片进入回弹（不依赖 mouseout，事件丢失也能归位）
      for (var k = 0; k < states.length; k++) {
        var st = states[k];
        if (st.el !== card && !(t && st.el.contains(t))) {
          st.hover = false;
          st.tRx = 0; st.tRy = 0; st.tLift = 0; st.tGx = 50; st.tGy = 50;
        }
      }
      if (!card) { if (states.length) tiltWake(); return; }
      var r = card.getBoundingClientRect();
      var px = (e.clientX - r.left) / Math.max(r.width, 1);
      var py = (e.clientY - r.top) / Math.max(r.height, 1);
      var s = findState(card);
      if (!s) {
        s = { el: card, rx: 0, ry: 0, lift: 0, gx: 50, gy: 50, tRx: 0, tRy: 0, tLift: 0, tGx: 50, tGy: 50, hover: true };
        states.push(s);
      }
      s.hover = true;
      s.tRx = (0.5 - py) * 9;
      s.tRy = (px - 0.5) * 11;
      s.tLift = -6;
      s.tGx = px * 100;
      s.tGy = py * 100;
      tiltWake();
    });
    document.addEventListener('mouseout', function(e){
      var t = e.target;
      var card = t && t.closest ? t.closest('[data-tilt]') : null;
      if (!card || card.contains(e.relatedTarget)) return;
      var s = findState(card);
      if (!s) return;
      s.hover = false;
      s.tRx = 0; s.tRy = 0; s.tLift = 0; s.tGx = 50; s.tGy = 50;
      tiltWake();
    });
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
  document.querySelectorAll('.g3-modal').forEach(function(m){
    var close = m.querySelector('.modal-close');
    var overlay = m.querySelector('.modal-overlay');
    if (close) close.addEventListener('click', function(){ closeModal(m); });
    if (overlay) overlay.addEventListener('click', function(e){ if (e.target === overlay) closeModal(m); });
  });
  document.addEventListener('keydown', function(e){
    if (e.key === 'Escape') {
      document.querySelectorAll('.g3-modal.is-open').forEach(function(m){ closeModal(m); });
    }
  });

  // 回到顶部
  var backTop = document.querySelector('.g3-backtop');
  if (backTop) backTop.addEventListener('click', function(){ window.scrollTo({top:0, behavior:'smooth'}); });
})();
`

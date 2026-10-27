/**
 * 立方档案共享交互引擎（原生 JS 字符串）。
 * Vue 预览组件与导出 HTML 的 runtime 共用同一份代码，保证两端行为完全一致。
 *
 * 约定的 DOM 结构（root 内）：
 * - .cd-stage    3D 舞台（拖拽/点击区域）
 * - .cd-cube     立方体（引擎逐帧写 rotateX/rotateY）
 * - [data-face]  六个面（点击展开对应面板）
 * - [data-nav]   底部导航（点击展开对应面板）
 * - [data-panel] 面板视图（与 face 同名，切换 is-active）
 * - .cd-close    面板关闭按钮
 * - [data-work-id] / [data-modal-id] / .cd-modal  作品弹窗
 * root 自身在展开时获得 .is-open
 */
export const CUBE_ENGINE = `(function (root) {
  var stage = root.querySelector('.cd-stage');
  var cube = root.querySelector('.cd-cube');
  if (!stage || !cube) return function () {};

  var reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  var RX_LIMIT = 90;
  var rx = -16, ry = 34;            // 当前姿态
  var vx = 0, vy = 0;               // 惯性角速度
  var dragging = false, moved = 0, px = 0, py = 0, downT = 0;
  var openFace = null;              // 当前展开的面
  var anim = null;                  // 转正动画目标 {rx, ry}
  var autoSpeed = reduce ? 0 : 0.1; // 空闲自转速度
  var raf = requestAnimationFrame(tick);

  var FACE_ROT = {
    front: [0, 0], right: [0, -90], back: [0, 180],
    left: [0, 90], top: [-90, 0], bottom: [90, 0]
  };

  function clamp(v, a, b) { return v < a ? a : v > b ? b : v; }

  // 折算到与当前角最短的等价路径，避免转正时绕远路
  function shortest(target, current) {
    var t = ((target % 360) + 360) % 360;
    var c = ((current % 360) + 360) % 360;
    var d = t - c;
    if (d > 180) d -= 360;
    if (d < -180) d += 360;
    return current + d;
  }

  function tick() {
    if (anim) {
      var e = reduce ? 0.4 : 0.12;
      rx += (anim.rx - rx) * e;
      ry += (anim.ry - ry) * e;
      if (Math.abs(anim.rx - rx) < 0.25 && Math.abs(anim.ry - ry) < 0.25) {
        rx = anim.rx; ry = anim.ry; anim = null;
      }
    } else if (!dragging) {
      ry += vx;
      rx = clamp(rx + vy, -RX_LIMIT, RX_LIMIT);
      vx *= 0.94; vy *= 0.94;
      if (Math.abs(vx) < 0.02) vx = 0;
      if (Math.abs(vy) < 0.02) vy = 0;
      if (!openFace && !vx && !vy && autoSpeed) ry += autoSpeed;
    }
    cube.style.transform = 'rotateX(' + rx.toFixed(2) + 'deg) rotateY(' + ry.toFixed(2) + 'deg)';
    raf = requestAnimationFrame(tick);
  }

  function setNav(face) {
    var navs = root.querySelectorAll('[data-nav]');
    for (var i = 0; i < navs.length; i++) {
      navs[i].classList.toggle('is-active', navs[i].getAttribute('data-nav') === face);
    }
  }

  function openPanel(face) {
    if (!FACE_ROT[face]) return;
    openFace = face;
    vx = vy = 0;
    anim = { rx: FACE_ROT[face][0], ry: shortest(FACE_ROT[face][1], ry) };
    if (face === 'top' || face === 'bottom') anim.ry = shortest(0, ry);
    root.classList.add('is-open');
    var views = root.querySelectorAll('[data-panel]');
    for (var i = 0; i < views.length; i++) {
      views[i].classList.toggle('is-active', views[i].getAttribute('data-panel') === face);
    }
    setNav(face);
    var closeBtn = root.querySelector('.cd-close');
    if (closeBtn) closeBtn.focus({ preventScroll: true });
  }

  function closePanel() {
    openFace = null;
    anim = null;
    root.classList.remove('is-open');
    setNav(null);
  }

  function onDown(e) {
    if (openFace) { closePanel(); return; }
    if (e.button !== undefined && e.button !== 0) return;
    dragging = true;
    moved = 0;
    px = e.clientX; py = e.clientY;
    vx = vy = 0;
    anim = null;
    downT = Date.now();
    if (stage.setPointerCapture) {
      try { stage.setPointerCapture(e.pointerId); } catch (err) {}
    }
  }

  function onMove(e) {
    if (!dragging) return;
    var dx = e.clientX - px, dy = e.clientY - py;
    moved += Math.abs(dx) + Math.abs(dy);
    px = e.clientX; py = e.clientY;
    ry += dx * 0.32;
    rx = clamp(rx - dy * 0.32, -RX_LIMIT, RX_LIMIT);
    vx = dx * 0.32;
    vy = -dy * 0.32;
  }

  function onUp(e) {
    if (!dragging) return;
    dragging = false;
    // 位移小 + 时间短 => 视为点击：展开所点的面
    if (moved < 8 && Date.now() - downT < 600) {
      var el = e.target && e.target.closest ? e.target.closest('[data-face]') : null;
      vx = vy = 0;
      if (el) openPanel(el.getAttribute('data-face'));
    }
  }

  function closeModal(m) { m.classList.remove('is-open'); document.body.style.overflow = ''; }

  stage.addEventListener('pointerdown', onDown);
  stage.addEventListener('pointermove', onMove);
  stage.addEventListener('pointerup', onUp);
  stage.addEventListener('pointercancel', onUp);

  // ---- 事件委托:统一处理 root 上的 nav 点击 / 卡片点击 / close 点击 / modal 点击 ----
  // 原因:[data-work-id]/[data-modal-id]/[data-nav] 等元素会被 Vue 重渲染替换,
  // 逐个绑 addEventListener 在 onMounted 时一次性执行,之后新 DOM 节点没有监听器。
  // 事件委托绑定在 root 模板静态元素上,重渲染后自动生效。
  function onRootClick(ev) {
    var target = ev.target;
    if (!target) return;
    // 1. 作品卡片 → 打开对应 modal
    var card = target.closest('[data-work-id]');
    if (card) {
      ev.stopPropagation();
      var id = card.getAttribute('data-work-id');
      var modal = root.querySelector('[data-modal-id="' + id + '"]');
      if (modal) {
        modal.classList.add('is-open');
        document.body.style.overflow = 'hidden';
      }
      return;
    }
    // 2. modal 的关闭按钮 / overlay
    var modal = target.closest('.cd-modal');
    if (modal) {
      var close = target.closest('.modal-close');
      var overlay = target.classList && target.classList.contains('modal-overlay') && target === modal.querySelector('.modal-overlay');
      if (close || overlay) {
        closeModal(modal);
      }
      return;
    }
    // 3. nav 按钮 → openPanel
    var navBtn = target.closest('[data-nav]');
    if (navBtn) {
      openPanel(navBtn.getAttribute('data-nav'));
      return;
    }
    // 4. close 按钮 → closePanel
    if (target.closest('.cd-close')) {
      closePanel();
    }
  }
  root.addEventListener('click', onRootClick);

  function onKey(e) {
    if (e.key !== 'Escape') return;
    var opened = root.querySelectorAll('.cd-modal.is-open');
    if (opened.length) {
      for (var i = 0; i < opened.length; i++) closeModal(opened[i]);
    } else if (openFace) {
      closePanel();
    }
  }
  document.addEventListener('keydown', onKey);

  return function dispose() {
    cancelAnimationFrame(raf);
    stage.removeEventListener('pointerdown', onDown);
    stage.removeEventListener('pointermove', onMove);
    stage.removeEventListener('pointerup', onUp);
    stage.removeEventListener('pointercancel', onUp);
    root.removeEventListener('click', onRootClick);
    document.removeEventListener('keydown', onKey);
  };
})`

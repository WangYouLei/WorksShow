/**
 * 粒子星球共享交互引擎（原生 JS 字符串）。
 * Vue 预览组件与导出 HTML 的 runtime 共用同一份代码，保证两端行为完全一致。
 *
 * 约定的 DOM 结构（root 内）：
 * - .pp-canvas   粒子画布（拖拽旋转 / 短按点击切换板块）
 * - [data-panel] 板块视图（home/about/stats/works/skills/contact，切换 is-active）
 * - [data-nav]   底部导航（切换板块）
 * - [data-work-id] / [data-modal-id] / .pp-modal  作品弹窗
 * root 需要 data-name 属性（用于粒子重组为姓名文字）
 */
export const PP_ENGINE = `(function (root) {
  var canvas = root.querySelector('.pp-canvas');
  if (!canvas || !canvas.getContext) return function () {};
  var ctx = canvas.getContext('2d');
  if (!ctx) return function () {};

  var reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  var MOBILE = window.innerWidth < 640;
  var N = MOBILE ? 1500 : 2400;
  var FOV = 640;
  var W = 0, H = 0, DPR = 1;

  var px = new Float32Array(N), py = new Float32Array(N), pz = new Float32Array(N);
  var tx = new Float32Array(N), ty = new Float32Array(N), tz = new Float32Array(N);
  var delay = new Int16Array(N);
  var spr = new Uint8Array(N);
  var szb = new Float32Array(N);

  var i;
  for (i = 0; i < N; i++) {
    spr[i] = i % 3;
    szb[i] = 2.6 + ((i * 7919) % 1000) / 1000 * 2.6;
  }

  function makeSprite(rgb) {
    var s = document.createElement('canvas');
    s.width = 32; s.height = 32;
    var c = s.getContext('2d');
    var g = c.createRadialGradient(16, 16, 0, 16, 16, 16);
    g.addColorStop(0, 'rgba(255,255,255,0.95)');
    g.addColorStop(0.25, 'rgba(' + rgb + ',0.85)');
    g.addColorStop(1, 'rgba(' + rgb + ',0)');
    c.fillStyle = g;
    c.fillRect(0, 0, 32, 32);
    return s;
  }
  var SPRITES = [makeSprite('56,189,248'), makeSprite('139,124,246'), makeSprite('236,95,168')];

  function jit(s) { return ((i * 2654435761) % 1000) / 1000 * s - s / 2; }

  // —— 六种目标形态：星球 / 姓名 / 数据环 / 作品星系 / 技能螺旋 / 联系漩涡 ——
  function fSphere(i2) {
    if (i2 % 13 === 0) { // 少量粒子组成倾斜星环
      var a = i2 * 0.5236;
      var y0 = 8, x0 = Math.cos(a) * 262, z0 = Math.sin(a) * 262;
      var ca = Math.cos(0.42), sa = Math.sin(0.42);
      return [x0, y0 * ca - z0 * sa, y0 * sa + z0 * ca];
    }
    var y = 1 - 2 * (i2 + 0.5) / N;
    var r = Math.sqrt(Math.max(0, 1 - y * y));
    var th = i2 * 2.39996323;
    return [Math.cos(th) * r * 185, y * 185, Math.sin(th) * r * 185];
  }

  var textPts = null;
  function buildText(str) {
    var c = document.createElement('canvas');
    c.width = 1000; c.height = 320;
    var x = c.getContext('2d');
    if (!x) return [];
    var font = '900 170px "Noto Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif';
    x.font = font;
    var w = Math.max(x.measureText(str).width, 1);
    var k = Math.min(0.55, 470 / w);
    x.textAlign = 'center'; x.textBaseline = 'middle'; x.fillStyle = '#fff';
    x.fillText(str, 500, 160);
    var d = x.getImageData(0, 0, 1000, 320).data;
    var pts = [];
    for (var yy = 0; yy < 320; yy += 3) {
      for (var xx = 0; xx < 1000; xx += 3) {
        if (d[(yy * 1000 + xx) * 4 + 3] > 110) pts.push([(xx - 500) * k, (yy - 160) * k]);
      }
    }
    return pts;
  }
  function fText(i2) {
    if (!textPts || !textPts.length) return fSphere(i2);
    var p = textPts[i2 % textPts.length];
    return [p[0] + jit(5), p[1] + jit(5), jit(30)];
  }

  function fRings(i2) {
    var ring = i2 % 3;
    var R = [200, 154, 110][ring];
    var a = i2 * 0.61 + ring * 2.094;
    var x0 = Math.cos(a) * R, z0 = Math.sin(a) * R, y0 = jit(8);
    var ax = 0.5 + ring * 0.4, az = ring * 0.9;
    var y1 = y0 * Math.cos(ax) - z0 * Math.sin(ax);
    var z1 = y0 * Math.sin(ax) + z0 * Math.cos(ax);
    return [x0 * Math.cos(az) - y1 * Math.sin(az), x0 * Math.sin(az) + y1 * Math.cos(az), z1];
  }

  function fGalaxy(i2) {
    var t = i2 / N;
    var r = 34 + t * 215;
    var a = t * Math.PI * 10;
    return [Math.cos(a) * r, jit(26), Math.sin(a) * r];
  }

  function fHelix(i2) {
    var t = i2 / N;
    var ang = t * Math.PI * 7;
    if (i2 % 14 < 2) { // 横档粒子：从轴心向外伸展
      var rr = ((i2 % 14) / 14 + 0.15) * 95;
      return [Math.cos(ang) * rr, (t - 0.5) * 380, Math.sin(ang) * rr];
    }
    var strand = i2 % 2;
    var a2 = ang + strand * Math.PI;
    return [Math.cos(a2) * 95, (t - 0.5) * 380, Math.sin(a2) * 95];
  }

  function fVortex(i2) {
    var t = i2 / N;
    var a = t * Math.PI * 16;
    var r = 232 * (1 - t) + 26;
    return [Math.cos(a) * r, (t - 0.5) * 300, Math.sin(a) * r];
  }

  var FORMATIONS = { home: fSphere, about: fText, stats: fRings, works: fGalaxy, skills: fHelix, contact: fVortex };

  // —— 板块切换 ——
  var sections = [];
  var panels = root.querySelectorAll('[data-panel]');
  for (i = 0; i < panels.length; i++) sections.push(panels[i].getAttribute('data-panel'));
  var cur = sections.indexOf('home') >= 0 ? sections.indexOf('home') : 0;
  var raf = null;
  var running = true;

  function setSection(idx, rebuildText) {
    idx = ((idx % sections.length) + sections.length) % sections.length;
    cur = idx;
    var name = sections[idx];
    if (name === 'about' || rebuildText) textPts = buildText(root.getAttribute('data-name') || 'HELLO');
    var fn = FORMATIONS[name] || fSphere;
    for (var j = 0; j < N; j++) {
      i = j;
      var p = fn(j);
      tx[j] = p[0]; ty[j] = p[1]; tz[j] = p[2];
      delay[j] = reduce ? 0 : (j % 24);
    }
    // 姓名形态需要正对读者：把 ry 归一到最接近 0 的等价角并暂停自转
    if (name === 'about') {
      ry = ((ry % (Math.PI * 2)) + Math.PI * 3) % (Math.PI * 2) - Math.PI;
    }
    for (var k = 0; k < panels.length; k++) {
      panels[k].classList.toggle('is-active', k === idx);
    }
    var navs = root.querySelectorAll('[data-nav]');
    for (var m = 0; m < navs.length; m++) {
      navs[m].classList.toggle('is-active', m === idx);
    }
    wake();
  }

  function next(dir) { setSection(cur + (dir === undefined ? 1 : dir)); }

  // —— 旋转（自转 + 拖拽 + 惯性）——
  var rx = -0.14, ry = 0.6;
  var vrx = 0, vry = 0;
  var dragging = false, moved = 0, lpx = 0, lpy = 0, downT = 0;
  var AUTO = reduce ? 0 : 0.0016;

  // —— 渲染循环 ——
  var t0 = 0;
  function frame(now) {
    if (!running) return;
    var dt = now - t0; t0 = now;
    ctx.globalCompositeOperation = 'source-over';
    ctx.fillStyle = 'rgba(6, 8, 18, 0.38)';
    ctx.fillRect(0, 0, W, H);
    ctx.globalCompositeOperation = 'lighter';

    if (!dragging) {
      ry += vry; rx += vrx;
      vry *= 0.95; vrx *= 0.95;
      if (Math.abs(vry) < 1e-5) vry = 0;
      if (Math.abs(vrx) < 1e-5) vrx = 0;
      if (!vry && !vrx && sections[cur] !== 'about' && AUTO) ry += AUTO * Math.min(dt, 40) / 16.7;
      // 姓名形态：静止后缓慢回正，保证文字可读
      if (!vry && !vrx && sections[cur] === 'about' && !reduce) {
        var ryT = Math.round(ry / (Math.PI * 2)) * Math.PI * 2;
        ry += (ryT - ry) * 0.02;
        rx += (-0.14 - rx) * 0.02;
      }
    }
    if (rx > 1.15) rx = 1.15;
    if (rx < -1.15) rx = -1.15;

    var zoom = Math.min(W, H) / 640;
    var cx = W / 2, cy = H / 2;
    var cy0 = Math.cos(ry), sy0 = Math.sin(ry), cx0 = Math.cos(rx), sx0 = Math.sin(rx);
    var ease = reduce ? 1 : 0.075;
    var breathe = reduce ? 0 : Math.sin(now * 0.0012);

    for (var j = 0; j < N; j++) {
      if (delay[j] > 0) { delay[j]--; }
      else {
        px[j] += (tx[j] - px[j]) * ease;
        py[j] += (ty[j] - py[j]) * ease;
        pz[j] += (tz[j] - pz[j]) * ease;
      }
      var ox = px[j] + breathe * (szb[j] - 3.9) * 0.8;
      var oy = py[j] + Math.cos(now * 0.001 + j) * (reduce ? 0 : 0.8);
      var X = ox * cy0 + pz[j] * sy0;
      var Z = -ox * sy0 + pz[j] * cy0;
      var Y = oy * cx0 - Z * sx0;
      Z = oy * sx0 + Z * cx0;
      var f = FOV / (FOV + Z);
      if (f < 0.1 || f > 4) continue;
      var sx = cx + X * f * zoom;
      if (sx < -20 || sx > W + 20) continue;
      var syp = cy + Y * f * zoom;
      if (syp < -20 || syp > H + 20) continue;
      var s = szb[j] * f * zoom * DPR * 2;
      var a = f - 0.72;
      ctx.globalAlpha = a < 0 ? 0.2 : (a > 0.6 ? 1 : a * 1.4 + 0.2);
      ctx.drawImage(SPRITES[spr[j]], sx - s / 2, syp - s / 2, s, s);
    }
    ctx.globalAlpha = 1;
    raf = requestAnimationFrame(frame);
  }
  function wake() {
    if (!raf && running) { t0 = performance.now(); raf = requestAnimationFrame(frame); }
  }

  // —— 尺寸 ——
  function resize() {
    DPR = Math.min(window.devicePixelRatio || 1, 2);
    var r = root.getBoundingClientRect();
    W = Math.max(1, Math.round(r.width * DPR));
    H = Math.max(1, Math.round(r.height * DPR));
    canvas.width = W;
    canvas.height = H;
    canvas.style.width = r.width + 'px';
    canvas.style.height = r.height + 'px';
    wake();
  }

  // —— 交互 ——
  function onDown(e) {
    if (e.button !== undefined && e.button !== 0) return;
    dragging = true;
    moved = 0;
    lpx = e.clientX; lpy = e.clientY;
    vrx = vry = 0;
    downT = Date.now();
    if (canvas.setPointerCapture) {
      try { canvas.setPointerCapture(e.pointerId); } catch (err) {}
    }
  }
  function onMove(e) {
    if (!dragging) return;
    var dx = e.clientX - lpx, dy = e.clientY - lpy;
    moved += Math.abs(dx) + Math.abs(dy);
    lpx = e.clientX; lpy = e.clientY;
    ry += dx * 0.005;
    rx += dy * 0.005;
    vry = dx * 0.005;
    vrx = dy * 0.005;
  }
  function onUp() {
    if (!dragging) return;
    dragging = false;
    if (moved < 8 && Date.now() - downT < 600) next(1);
  }
  canvas.addEventListener('pointerdown', onDown);
  canvas.addEventListener('pointermove', onMove);
  canvas.addEventListener('pointerup', onUp);
  canvas.addEventListener('pointercancel', onUp);

  var navs = root.querySelectorAll('[data-nav]');
  for (i = 0; i < navs.length; i++) {
    (function (btn, idx) {
      btn.addEventListener('click', function () { setSection(idx); });
    })(navs[i], i);
  }

  function closeModal(m) { m.classList.remove('is-open'); document.body.style.overflow = ''; }
  var cards = root.querySelectorAll('[data-work-id]');
  for (i = 0; i < cards.length; i++) {
    (function (card) {
      card.addEventListener('click', function (ev) {
        ev.stopPropagation();
        var id = card.getAttribute('data-work-id');
        var modal = root.querySelector('[data-modal-id="' + id + '"]');
        if (modal) {
          modal.classList.add('is-open');
          document.body.style.overflow = 'hidden';
        }
      });
    })(cards[i]);
  }
  var modals = root.querySelectorAll('.pp-modal');
  for (i = 0; i < modals.length; i++) {
    (function (modal) {
      var close = modal.querySelector('.modal-close');
      var overlay = modal.querySelector('.modal-overlay');
      if (close) close.addEventListener('click', function () { closeModal(modal); });
      if (overlay) overlay.addEventListener('click', function (ev) { if (ev.target === overlay) closeModal(modal); });
    })(modals[i]);
  }

  function onKey(e) {
    if (e.key === 'ArrowRight') next(1);
    else if (e.key === 'ArrowLeft') next(-1);
    else if (e.key === 'Escape') {
      var opened = root.querySelectorAll('.pp-modal.is-open');
      for (var q = 0; q < opened.length; q++) closeModal(opened[q]);
    }
  }
  document.addEventListener('keydown', onKey);

  function onVis() {
    running = !document.hidden;
    if (running) wake();
    else if (raf) { cancelAnimationFrame(raf); raf = null; }
  }
  document.addEventListener('visibilitychange', onVis);
  window.addEventListener('resize', resize);

  // 字体就绪后重绘姓名形态（避免首绘用了回退字体）
  if (document.fonts && document.fonts.ready) {
    document.fonts.ready.then(function () {
      if (sections[cur] === 'about') setSection(cur, true);
    });
  }

  resize();
  setSection(cur);
  wake();

  return function dispose() {
    running = false;
    if (raf) cancelAnimationFrame(raf);
    canvas.removeEventListener('pointerdown', onDown);
    canvas.removeEventListener('pointermove', onMove);
    canvas.removeEventListener('pointerup', onUp);
    canvas.removeEventListener('pointercancel', onUp);
    document.removeEventListener('keydown', onKey);
    document.removeEventListener('visibilitychange', onVis);
    window.removeEventListener('resize', resize);
  };
})`

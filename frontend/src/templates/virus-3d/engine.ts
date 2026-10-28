/**
 * 病毒侵染 VIRION · 共享交互引擎（原生 JS 字符串）。
 * Vue 预览组件与导出 HTML 的 runtime 共用同一份代码，保证两端行为完全一致。
 *
 * 创意核心：整份个人档案按**病毒感染生命周期**六个阶段展开——
 *   0 觉醒 Awakening（首页）/ 1 附着 Attachment（关于）/ 2 注入 Penetration（数据）
 *   3 复制 Replication（作品）/ 4 装配 Assembly（技能）/ 5 释放 Release（联系）
 *
 * 视觉：电影级冠状病毒主体——
 * - 致密的蛋白质衣壳：数千壳颗粒深度排序遮挡，形成可转动观察的实体球
 * - 26 根 S 刺突：品红锥杆 + 三瓣球头（真实电镜 morphology）
 * - 内部琥珀 RNA 螺旋，透过衣壳隐约可见；附着阶段前壳淡出、RNA 增亮
 * - 背景游离病毒（虚化光球+短刺）与孢子漂浮，暗场显微镜氛围
 * - 阶段效果：注入=数据粒子螺旋涌入；复制=主体分裂出子病毒（可点击）；
 *   装配=琥珀扫描带游走刺突间；释放=衣壳爆发四散再回聚
 * - 拖拽旋转/惯性/自转，非拖拽时主体轻微"注视"鼠标（视差）
 *
 * DOM 约定（root 内）：
 * - .vi-stage / .vi-canvas           画布
 * - [data-panel="0..5"]              六段档案内容
 * - [data-nav]                       底部生命周期进度节点
 * - [data-wv="i"]                    子病毒热点（复制阶段，引擎写入坐标）
 * - [data-work-id] / [data-modal-id] / .vi-modal   作品弹窗
 */
export const VIRUS_ENGINE = `(function (root) {
  var stage = root.querySelector('.vi-stage') || root;
  var canvas = root.querySelector('.vi-canvas');
  if (!canvas || !canvas.getContext) return function () {};
  var ctx = canvas.getContext('2d');
  if (!ctx) return function () {};

  var reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  var MOBILE = window.innerWidth < 640;
  var W = 0, H = 0, cw = 0, ch = 0, DPR = 1, cx = 0, cy = 0, zoom = 1;
  var FOV = 950, R = 140;

  // —— 预渲染贴图：壳颗粒三档亮度 + 琥珀(装配扫描) + 品红辉光(刺突头/子病毒) + 青辉光 ——
  function disc(col0, col1) {
    var s = document.createElement('canvas');
    s.width = 48; s.height = 48;
    var c = s.getContext('2d');
    var g = c.createRadialGradient(24, 24, 0, 24, 24, 24);
    g.addColorStop(0, col0);
    g.addColorStop(0.55, col1);
    g.addColorStop(1, 'rgba(0,0,0,0)');
    c.fillStyle = g;
    c.fillRect(0, 0, 48, 48);
    return s;
  }
  var TEX_SHELL = [
    disc('rgba(38,110,106,0.95)', 'rgba(22,66,64,0.55)'),
    disc('rgba(84,190,172,0.95)', 'rgba(36,110,102,0.55)'),
    disc('rgba(168,244,222,0.95)', 'rgba(84,190,172,0.55)')
  ];
  var TEX_AMBER = disc('rgba(255,220,150,0.95)', 'rgba(255,170,70,0.5)');
  var TEX_MAG = disc('rgba(255,170,180,0.95)', 'rgba(255,55,105,0.45)');
  var TEX_CY = disc('rgba(180,250,235,0.9)', 'rgba(60,200,180,0.4)');

  // —— 衣壳颗粒：斐波那契球面 + 每颗粒爆发系数 ——
  var N = MOBILE ? 1500 : 2300;
  var gx = new Float32Array(N), gy = new Float32Array(N), gz = new Float32Array(N);
  var gb = new Float32Array(N), glon = new Float32Array(N);
  var i0;
  for (i0 = 0; i0 < N; i0++) {
    var yy = 1 - 2 * (i0 + 0.5) / N;
    var rr = Math.sqrt(Math.max(0, 1 - yy * yy));
    var aa = i0 * 2.39996323;
    var fv = Math.sin(i0 * 12.9898) * 43758.5453;
    fv -= Math.floor(fv);
    var rv = R * (0.988 + 0.024 * fv);
    gx[i0] = Math.cos(aa) * rr * rv;
    gy[i0] = yy * rv;
    gz[i0] = Math.sin(aa) * rr * rv;
    gb[i0] = fv;
    glon[i0] = aa;
  }

  // —— S 刺突方向 + 三瓣相位 ——
  var SPN = 26, dirs = [], dph = [];
  for (var d0 = 0; d0 < SPN; d0++) {
    var sy = 1 - 2 * (d0 + 0.5) / SPN;
    var sr = Math.sqrt(Math.max(0, 1 - sy * sy));
    var sa = d0 * 2.39996323;
    dirs.push([Math.cos(sa) * sr, sy, Math.sin(sa) * sr]);
    dph.push(d0 * 1.9);
  }

  // —— 档案阶段 ——
  var sections = [], panels = root.querySelectorAll('[data-panel]');
  for (var p0 = 0; p0 < panels.length; p0++) sections.push(panels[p0].getAttribute('data-panel'));
  var hots = root.querySelectorAll('[data-wv]');
  var cur = 0;
  var mode = '0';

  // 阶段参数：scl 主体缩放 / ox 主体横移 / see 前壳透明度 / rna RNA 增亮
  //          inv 注入粒子 / rep 子病毒 / scn 装配扫描 / brs 爆发
  var TGT = {
    '0': { scl: 1.00, ox: 0.00, see: 0.00, rna: 0.35, inv: 0, rep: 0, scn: 0, brs: 0 },
    '1': { scl: 1.22, ox: 0.55, see: 0.80, rna: 1.00, inv: 0, rep: 0, scn: 0, brs: 0 },
    '2': { scl: 0.96, ox: 0.00, see: 0.00, rna: 0.45, inv: 1, rep: 0, scn: 0, brs: 0 },
    '3': { scl: 0.60, ox: 0.00, see: 0.00, rna: 0.35, inv: 0, rep: 1, scn: 0, brs: 0 },
    '4': { scl: 1.00, ox: 0.00, see: 0.00, rna: 0.35, inv: 0, rep: 0, scn: 1, brs: 0 },
    '5': { scl: 1.05, ox: 0.00, see: 0.10, rna: 0.60, inv: 0, rep: 0, scn: 0, brs: 1 }
  };
  var st = { scl: 1, ox: 0, see: 0, rna: 0.35, inv: 0, rep: 0, scn: 0, brs: 0 };

  // 旋转：拖拽 + 惯性 + 自转 + 鼠标视差（主体"注视"）
  var rx = -0.18, ry = 0.5, vrx = 0, vry = 0;
  var dragging = false, moved = 0, lpx = 0, lpy = 0, downT = 0;
  var mx = 0.5, my = 0.5;
  var AUTO = reduce ? 0 : 0.0028;

  var RY_C = 1, RY_S = 0, RX_C = 1, RX_S = 0;
  var PA = { x: 0, y: 0, z: 0, f: 1 }, PB = { x: 0, y: 0, z: 0, f: 1 };
  var baseX = 0, baseY = 0;
  function projTo(x, y, z, o) {
    var X = x * RY_C + z * RY_S;
    var Z = -x * RY_S + z * RY_C;
    var Y = y * RX_C - Z * RX_S;
    Z = y * RX_S + Z * RX_C;
    var f = FOV / (FOV + Z);
    o.x = baseX + X * f * zoom;
    o.y = baseY + Y * f * zoom;
    o.z = Z;
    o.f = f;
  }
  function clamp01(v) { return v < 0 ? 0 : (v > 1 ? 1 : v); }
  function depthOf(f) { return clamp01((f - 0.80) / 0.42); }

  // —— 注入粒子（数据）：从屏幕外螺旋收拢到三条轨道 ——
  var INV_N = MOBILE ? 110 : 200;
  var invA = new Float32Array(INV_N), invR = new Float32Array(INV_N);
  var invT = new Float32Array(INV_N), invS = new Float32Array(INV_N);
  for (var v0 = 0; v0 < INV_N; v0++) {
    invA[v0] = v0 * 2.39996;
    invT[v0] = v0 % 3;
    invR[v0] = 205 + (v0 % 3) * 34 + (v0 % 7) * 4;
    invS[v0] = 0.004 + (v0 % 5) * 0.0012;
  }

  // —— 子病毒（作品）：轨道参数 ——
  var wv = [];
  for (var w0 = 0; w0 < hots.length; w0++) {
    wv.push({
      a: w0 * 1.35, sp: 0.0035 + (w0 % 4) * 0.0009,
      rad: 198 + (w0 % 3) * 26 + (w0 % 5) * 6,
      tilt: 0.35 + (w0 % 5) * 0.22, tilt2: w0 * 0.9,
      x: 0, y: 0, z: 0, ok: false
    });
  }

  // —— 背景游离病毒 + 孢子 ——
  var drifters = [];
  for (var f0 = 0; f0 < 7; f0++) {
    drifters.push({
      a: f0 * 0.9, rad: 330 + (f0 % 3) * 70, va: 0.0004 + (f0 % 3) * 0.0002,
      sz: 16 + (f0 % 4) * 7, ph: f0 * 1.3
    });
  }
  var motes = [];
  for (var m0 = 0; m0 < 90; m0++) {
    motes.push({ a: m0 * 0.7, rad: 60 + (m0 * 37 % 340), va: 0.0008 + (m0 % 5) * 0.0004, sz: 1.2 + (m0 % 4) * 0.8 });
  }

  var raf = null, running = true, t0 = 0;
  var draws = [];

  function flush() {
    draws.sort(function (m, n) { return n.z - m.z; });
    for (var k = 0; k < draws.length; k++) {
      var o = draws[k];
      if (o.k === 0) {
        // 壳颗粒贴图
        ctx.globalAlpha = o.a;
        ctx.drawImage(o.tex, o.x - o.r, o.y - o.r, o.r * 2, o.r * 2);
      } else if (o.k === 1) {
        // 刺突杆
        ctx.globalAlpha = o.a;
        ctx.strokeStyle = o.col;
        ctx.lineWidth = o.w * DPR;
        ctx.lineCap = 'round';
        ctx.beginPath();
        ctx.moveTo(o.x, o.y);
        ctx.lineTo(o.x2, o.y2);
        ctx.stroke();
      } else if (o.k === 2) {
        // 刺突头：辉光贴图 + 三瓣实心圆
        ctx.globalAlpha = o.a;
        ctx.drawImage(o.glow, o.x - o.r * 2.4, o.y - o.r * 2.4, o.r * 4.8, o.r * 4.8);
        ctx.fillStyle = o.col;
        for (var b = 0; b < 3; b++) {
          var ba = o.ph + b * 2.094;
          ctx.beginPath();
          ctx.arc(o.x + Math.cos(ba) * o.r * 0.55, o.y + Math.sin(ba) * o.r * 0.55, o.r * 0.72, 0, 6.2832);
          ctx.fill();
        }
      } else if (o.k === 3) {
        // RNA 段
        ctx.globalAlpha = o.a;
        ctx.strokeStyle = o.col;
        ctx.lineWidth = o.w * DPR;
        ctx.lineCap = 'round';
        ctx.beginPath();
        ctx.moveTo(o.x, o.y);
        ctx.lineTo(o.x2, o.y2);
        ctx.stroke();
      } else if (o.k === 4) {
        // 游离病毒 / 孢子：辉光贴图
        ctx.globalAlpha = o.a;
        ctx.drawImage(o.tex, o.x - o.r, o.y - o.r, o.r * 2, o.r * 2);
      } else if (o.k === 5) {
        // 子病毒：青/品红辉光球 + 六根短刺
        ctx.globalAlpha = o.a * 0.5;
        ctx.drawImage(o.glow, o.x - o.r * 2.6, o.y - o.r * 2.6, o.r * 5.2, o.r * 5.2);
        ctx.globalAlpha = o.a;
        ctx.strokeStyle = 'rgba(255,150,170,0.9)';
        ctx.lineWidth = 1 * DPR;
        for (var s2 = 0; s2 < 6; s2++) {
          var sa = o.ph + s2 * 1.047;
          ctx.beginPath();
          ctx.moveTo(o.x + Math.cos(sa) * o.r, o.y + Math.sin(sa) * o.r);
          ctx.lineTo(o.x + Math.cos(sa) * o.r * 1.85, o.y + Math.sin(sa) * o.r * 1.85);
          ctx.stroke();
        }
      } else if (o.k === 6) {
        // 注入粒子：小亮贴图
        ctx.globalAlpha = o.a;
        ctx.drawImage(TEX_CY, o.x - o.r, o.y - o.r, o.r * 2, o.r * 2);
      }
    }
    ctx.globalAlpha = 1;
    draws.length = 0;
  }

  function frame(now) {
    if (!running) return;
    var dt = Math.min(now - t0, 48); t0 = now;

    var tgt = TGT[mode] || TGT['0'];
    var ease = reduce ? 1 : 0.07;
    st.scl += (tgt.scl - st.scl) * ease;
    st.ox += (tgt.ox - st.ox) * ease;
    st.see += (tgt.see - st.see) * ease;
    st.rna += (tgt.rna - st.rna) * ease;
    st.inv += (tgt.inv - st.inv) * ease;
    st.rep += (tgt.rep - st.rep) * ease;
    st.scn += (tgt.scn - st.scn) * ease;
    st.brs += (tgt.brs - st.brs) * (reduce ? 1 : 0.045);

    // 释放阶段爆发呼吸
    var brsAmp = 0;
    if (mode === '5' && !reduce) brsAmp = st.brs * (1 + Math.sin(now * 0.0016) * 0.35);

    if (!dragging) {
      ry += vry; rx += vrx;
      vry *= 0.95; vrx *= 0.95;
      if (Math.abs(vry) < 1e-5) vry = 0;
      if (Math.abs(vrx) < 1e-5) vrx = 0;
      if (!vry && !vrx && AUTO) ry += AUTO * dt / 16.7;
    }
    if (rx > 1.15) rx = 1.15;
    if (rx < -1.15) rx = -1.15;
    RY_C = Math.cos(ry); RY_S = Math.sin(ry);
    RX_C = Math.cos(rx); RX_S = Math.sin(rx);

    // 主体中心：排版基准 + 阶段横移 + 鼠标视差（"注视"）
    var px = reduce ? 0.5 : mx, py = reduce ? 0.5 : my;
    var ox2 = (mode === '1') ? st.ox * cw * 0.16 : 0;
    baseX = cx + ox2 + (px - 0.5) * 26 * st.scl;
    baseY = cy + (py - 0.5) * 18 * st.scl;

    ctx.globalCompositeOperation = 'source-over';
    ctx.clearRect(0, 0, W, H);

    var scanA = reduce ? 1.1 : now * 0.0006;
    var scl = st.scl;
    var burstR = 1 + brsAmp * 1.7;

    // —— 背景孢子 ——
    for (var m1 = 0; m1 < motes.length; m1++) {
      var mo = motes[m1];
      var ma = mo.a + (reduce ? 0 : now * mo.va);
      var mxp = Math.cos(ma) * mo.rad, myp = Math.sin(ma * 1.3) * mo.rad * 0.5, mzp = Math.sin(ma) * mo.rad;
      PA.x = baseX * 0.3 + cw * 0.5 + mxp * zoom;
      PA.y = ch * 0.5 + myp * zoom;
      if (PA.x < -10 || PA.x > cw + 10 || PA.y < -10 || PA.y > ch + 10) continue;
      var mdep = clamp01(0.5 + mzp / 400);
      draws.push({ k: 4, z: 800 + mzp, x: PA.x, y: PA.y, r: mo.sz * (0.7 + mdep) * zoom * DPR, a: 0.05 + 0.16 * mdep, tex: mdep > 0.55 ? TEX_CY : TEX_SHELL[1] });
    }

    // —— 游离病毒 ——
    for (var f1 = 0; f1 < drifters.length; f1++) {
      var dv = drifters[f1];
      var da = dv.a + (reduce ? 0 : now * dv.va);
      var dxp = Math.cos(da) * dv.rad, dyp = Math.sin(da * 0.8 + dv.ph) * 120, dzp = Math.sin(da) * dv.rad;
      // 游离病毒使用独立投影中心（不随主体视差，制造层次）
      var df = FOV / (FOV + dzp + 260);
      var dxx = cw * 0.5 + dxp * df * zoom;
      var dyy = ch * 0.5 + dyp * df * zoom;
      if (dxx < -60 || dxx > cw + 60 || dyy < -60 || dyy > ch + 60) continue;
      var ddep = depthOf(df);
      draws.push({ k: 4, z: dzp + 260, x: dxx, y: dyy, r: dv.sz * df * zoom * DPR * 2, a: 0.10 + 0.22 * ddep, tex: TEX_SHELL[2] });
      // 短刺
      ctx.globalAlpha = (0.10 + 0.2 * ddep) * 0.9;
      ctx.strokeStyle = 'rgba(120,200,185,0.8)';
      ctx.lineWidth = 1 * DPR;
      for (var ds = 0; ds < 6; ds++) {
        var dsa = dv.ph + ds * 1.047 + (reduce ? 0 : now * 0.0004);
        ctx.beginPath();
        ctx.moveTo(dxx + Math.cos(dsa) * dv.sz * df * zoom, dyy + Math.sin(dsa) * dv.sz * df * zoom);
        ctx.lineTo(dxx + Math.cos(dsa) * dv.sz * df * zoom * 1.9, dyy + Math.sin(dsa) * dv.sz * df * zoom * 1.9);
        ctx.stroke();
      }
      ctx.globalAlpha = 1;
    }

    // —— 衣壳颗粒 ——
    for (var g1 = 0; g1 < N; g1++) {
      var gs0 = gx[g1] * scl * burstR, gs1 = gy[g1] * scl * burstR, gs2 = gz[g1] * scl * burstR;
      projTo(gs0, gs1, gs2, PA);
      if (PA.x < -12 || PA.x > cw + 12 || PA.y < -12 || PA.y > ch + 12) continue;
      var dep = depthOf(PA.f);
      var al = 0.50 + 0.50 * dep;
      if (PA.z < 0) al *= 1 - st.see * 0.78; // 附着：前壳淡出看内部
      if (st.brs > 0.05) al *= 1 - st.brs * 0.45 * (0.4 + gb[g1] * 0.6); // 释放：爆发稀疏
      var tex = TEX_SHELL[dep < 0.34 ? 0 : (dep < 0.7 ? 1 : 2)];
      // 装配扫描：亮带处换琥珀
      if (st.scn > 0.04) {
        var dlon = ((glon[g1] + ry - scanA) % 6.2832 + 9.4248) % 6.2832 - 3.1416;
        var band = clamp01(1 - Math.abs(dlon) / 0.55) * st.scn;
        if (band > 0.06) { tex = TEX_AMBER; al = Math.min(1, al + band * 0.5); }
      }
      var psz = (2.9 + 2.6 * dep) * zoom * PA.f * DPR * 2.2 * (0.9 + gb[g1] * 0.25);
      draws.push({ k: 0, z: PA.z, x: PA.x, y: PA.y, r: psz, a: al, tex: tex });
    }

    // —— S 刺突 ——
    var spikeLen = 46 + (mode === '5' ? 16 * st.brs : 0);
    for (var s1 = 0; s1 < SPN; s1++) {
      var dr = dirs[s1];
      var bR = R * scl * (0.995 + 0.02 * (s1 % 3)) * burstR;
      var tR = bR + (spikeLen + (s1 % 5) * 5) * (mode === '5' ? 1 + 0.3 * st.brs : 1) * scl;
      projTo(dr[0] * bR, dr[1] * bR, dr[2] * bR, PA);
      projTo(dr[0] * tR, dr[1] * tR, dr[2] * tR, PB);
      if (PA.x < -70 || PA.x > cw + 70 || PA.y < -70 || PA.y > ch + 70) continue;
      var sdep = depthOf((PA.f + PB.f) / 2);
      var zmid = (PA.z + PB.z) / 2;
      var sfade = zmid < 0 ? 1 - st.see * 0.5 : 1;
      draws.push({
        k: 1, z: zmid, a: (0.35 + 0.6 * sdep) * sfade,
        x: PA.x, y: PA.y, x2: PB.x, y2: PB.y,
        w: 1.6 + 2.8 * sdep,
        col: sdep > 0.55 ? 'rgba(255,110,140,0.95)' : 'rgba(150,45,75,0.85)'
      });
      // 头：装配阶段扫到琥珀，否则品红
      var hlon = ((Math.atan2(dr[2], dr[0]) + ry - scanA) % 6.2832 + 9.4248) % 6.2832 - 3.1416;
      var hband = st.scn > 0.04 ? clamp01(1 - Math.abs(hlon) / 0.6) * st.scn : 0;
      var beat = 1 + (reduce ? 0 : Math.sin(now * 0.0022 + s1 * 1.7) * 0.10);
      draws.push({
        k: 2, z: PB.z, a: (0.5 + 0.5 * sdep) * sfade,
        x: PB.x, y: PB.y,
        r: Math.max(1.4, 4.6 * PB.f * zoom * beat * DPR * 1.25),
        ph: dph[s1],
        col: hband > 0.25 ? 'rgba(255,196,110,0.95)' : 'rgba(255,86,124,0.95)',
        glow: hband > 0.25 ? TEX_AMBER : TEX_MAG
      });
    }

    // —— 内部 RNA 螺旋 ——
    var rnaA = (0.30 + st.rna * 0.65) * (1 - st.brs * 0.6);
    var spin = reduce ? 0 : now * 0.00038;
    var hasLast = false;
    for (var r1 = 0; r1 <= 60; r1++) {
      var tt = r1 / 60;
      var ra = tt * 6.2 * Math.PI + spin;
      projTo(Math.cos(ra) * 58 * scl, (tt - 0.5) * 156 * scl, Math.sin(ra) * 58 * scl, PA);
      if (hasLast) {
        var rdep = depthOf((PA.f + PB.f) / 2);
        var z3 = (PA.z + PB.z) / 2;
        draws.push({
          k: 3, z: z3, a: rnaA * (0.45 + 0.55 * rdep),
          x: PB.x, y: PB.y, x2: PA.x, y2: PA.y,
          w: 1.3 + 1.5 * rdep,
          col: 'rgba(255,182,80,1)'
        });
      }
      PB.x = PA.x; PB.y = PA.y; PB.z = PA.z; PB.f = PA.f;
      hasLast = true;
    }

    // —— 注入粒子（数据）：从外圈螺旋收拢 ——
    if (st.inv > 0.02) {
      for (var v1 = 0; v1 < INV_N; v1++) {
        invA[v1] += invS[v1] * dt * (0.4 + st.inv);
        var pull = st.inv;
        var rr2 = 620 + (invR[v1] - 620) * pull;
        var va2 = invA[v1];
        var vx2 = Math.cos(va2) * rr2, vz2 = Math.sin(va2) * rr2;
        var vy2 = Math.sin(va2 * 2 + invT[v1] * 2.1) * (46 * pull);
        var ct = Math.cos(invT[v1] * 0.8), stn = Math.sin(invT[v1] * 0.8);
        var vy3 = vy2 * ct - vz2 * stn;
        var vz3 = vy2 * stn + vz2 * ct;
        projTo(vx2, vy3, vz3, PA);
        if (PA.x < -14 || PA.x > cw + 14 || PA.y < -14 || PA.y > ch + 14) continue;
        var vdep = depthOf(PA.f);
        draws.push({
          k: 6, z: PA.z, x: PA.x, y: PA.y,
          r: Math.max(1, (2.2 + 1.6 * vdep) * zoom * PA.f * DPR * 1.6),
          a: (0.18 + 0.62 * vdep) * Math.min(1, pull * 1.6)
        });
      }
    }

    // —— 子病毒（作品）——
    for (var w1 = 0; w1 < wv.length; w1++) {
      var wv1 = wv[w1];
      wv1.ok = false;
      if (st.rep < 0.03) continue;
      wv1.a += wv1.sp * dt * (0.5 + st.rep);
      var wx2 = Math.cos(wv1.a) * wv1.rad, wz2 = Math.sin(wv1.a) * wv1.rad;
      var wy2 = Math.sin(wv1.a * 1.4) * 44;
      var ct2 = Math.cos(wv1.tilt), st2 = Math.sin(wv1.tilt);
      var wy3 = wy2 * ct2 - wz2 * st2;
      var wz3 = wy2 * st2 + wz2 * ct2;
      var ct3 = Math.cos(wv1.tilt2), st3 = Math.sin(wv1.tilt2);
      var wx3 = wx2 * ct3 - wy3 * st3;
      var wy4 = wx2 * st3 + wy3 * ct3;
      projTo(wx3, wy4, wz3, PA);
      wv1.x = PA.x; wv1.y = PA.y; wv1.z = PA.z;
      wv1.ok = PA.x > -40 && PA.x < cw + 40 && PA.y > -40 && PA.y < ch + 40;
      if (!wv1.ok) continue;
      var wdep = depthOf(PA.f);
      var wr = Math.max(2, (10 + (w1 % 3) * 2.4) * PA.f * zoom * DPR * 1.5 * st.rep);
      draws.push({
        k: 5, z: PA.z, a: (0.35 + 0.65 * wdep) * st.rep,
        x: PA.x, y: PA.y, r: wr, ph: wv1.a * 2,
        glow: w1 % 2 ? TEX_MAG : TEX_CY
      });
    }

    flush();

    // —— 子病毒热点跟随 ——
    for (var h1 = 0; h1 < hots.length; h1++) {
      var hb = hots[h1], wv2 = wv[h1];
      if (!wv2 || !wv2.ok || st.rep < 0.4) { hb.style.opacity = '0'; hb.style.pointerEvents = 'none'; continue; }
      hb.style.transform = 'translate(' + Math.round(wv2.x) + 'px,' + Math.round(wv2.y) + 'px)';
      hb.style.opacity = wv2.z < 0 ? '1' : '0.25';
      hb.style.pointerEvents = wv2.z < 0 ? 'auto' : 'none';
    }

    raf = requestAnimationFrame(frame);
  }

  function wake() {
    if (!raf && running) { t0 = performance.now(); raf = requestAnimationFrame(frame); }
  }

  function resize() {
    DPR = Math.min(window.devicePixelRatio || 1, 2);
    var r = stage.getBoundingClientRect();
    cw = Math.max(1, Math.round(r.width));
    ch = Math.max(1, Math.round(r.height));
    W = Math.round(cw * DPR);
    H = Math.round(ch * DPR);
    canvas.width = W;
    canvas.height = H;
    canvas.style.width = cw + 'px';
    canvas.style.height = ch + 'px';
    cx = cw * 0.5;
    cy = ch * (cw < 860 ? 0.42 : 0.5);
    zoom = Math.min(cw, ch) / (cw < 860 ? 560 : 640);
    wake();
  }

  // —— 阶段切换 ——
  function setSection(idx) {
    if (!sections.length) return;
    idx = ((idx % sections.length) + sections.length) % sections.length;
    cur = idx;
    mode = sections[idx];
    for (var k1 = 0; k1 < panels.length; k1++) panels[k1].classList.toggle('is-active', k1 === idx);
    var navs = root.querySelectorAll('[data-nav]');
    for (var m2 = 0; m2 < navs.length; m2++) navs[m2].classList.toggle('is-active', m2 === idx);
    var hds = root.querySelectorAll('[data-hd]');
    for (var h2 = 0; h2 < hds.length; h2++) hds[h2].classList.toggle('is-active', h2 === idx);
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
    var r = stage.getBoundingClientRect();
    mx = clamp01((e.clientX - r.left) / Math.max(r.width, 1));
    my = clamp01((e.clientY - r.top) / Math.max(r.height, 1));
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
    dragging = false;
  }
  function onClick(e) {
    if (moved >= 8 || Date.now() - downT > 600) return;
    // 点击主体：脉动反馈 + 前进一阶段
    var r = stage.getBoundingClientRect();
    var mxp2 = e.clientX - r.left, myp2 = e.clientY - r.top;
    var ddx = mxp2 - baseX, ddy = myp2 - baseY;
    if (ddx * ddx + ddy * ddy < (R * zoom * st.scl * 1.3) * (R * zoom * st.scl * 1.3)) {
      st.rna = Math.min(1, st.rna + 0.3);
      setSection(cur + 1);
    }
  }
  canvas.addEventListener('pointerdown', onDown);
  canvas.addEventListener('pointermove', onMove);
  canvas.addEventListener('pointerup', onUp);
  canvas.addEventListener('pointercancel', onUp);
  canvas.addEventListener('click', onClick);

  function closeModal(m) {
    m.classList.remove('is-open');
    document.body.style.overflow = '';
  }
  function onRootClick(ev) {
    var target = ev.target;
    if (!target) return;
    // 子病毒热点 / 作品卡片 → 弹窗
    var hit = target.closest('[data-wv],[data-work-id]');
    if (hit) {
      var wid = hit.getAttribute('data-work-id') || (hit.getAttribute('data-wv') !== null ? hots[+hit.getAttribute('data-wv')] && hots[+hit.getAttribute('data-wv')].getAttribute('data-work-id') : null);
      if (wid) {
        ev.stopPropagation();
        var modal = root.querySelector('[data-modal-id="' + wid + '"]');
        if (modal) {
          modal.classList.add('is-open');
          document.body.style.overflow = 'hidden';
        }
        return;
      }
    }
    var modalEl = target.closest('.vi-modal');
    if (modalEl) {
      if (target.closest('.modal-close') || target.classList.contains('modal-overlay')) closeModal(modalEl);
      return;
    }
    var navBtn = target.closest('[data-nav]');
    if (navBtn) {
      var all = root.querySelectorAll('[data-nav]');
      for (var n1 = 0; n1 < all.length; n1++) {
        if (all[n1] === navBtn) { setSection(n1); break; }
      }
    }
  }
  root.addEventListener('click', onRootClick);

  function onKey(e) {
    if (e.key === 'ArrowRight') setSection(cur + 1);
    else if (e.key === 'ArrowLeft') setSection(cur - 1);
    else if (e.key === 'Escape') {
      var opened = root.querySelectorAll('.vi-modal.is-open');
      for (var q1 = 0; q1 < opened.length; q1++) closeModal(opened[q1]);
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

  resize();
  setSection(0);
  wake();

  return function dispose() {
    running = false;
    if (raf) cancelAnimationFrame(raf);
    canvas.removeEventListener('pointerdown', onDown);
    canvas.removeEventListener('pointermove', onMove);
    canvas.removeEventListener('pointerup', onUp);
    canvas.removeEventListener('pointercancel', onUp);
    canvas.removeEventListener('click', onClick);
    root.removeEventListener('click', onRootClick);
    document.removeEventListener('keydown', onKey);
    document.removeEventListener('visibilitychange', onVis);
    window.removeEventListener('resize', resize);
  };
})`

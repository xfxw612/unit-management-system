/* 公共脚本：请求封装、侧栏、提示、图表、下载 */

const $ = (sel, root = document) => root.querySelector(sel);
const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));

function esc(value) {
  if (value === null || value === undefined) return '';
  return String(value).replace(/[&<>"']/g, (c) => (
    { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]
  ));
}

function fmtScore(v) {
  if (v === null || v === undefined || v === '') return '—';
  return Number(v).toFixed(1);
}

let toastTimer = null;
function toast(msg, isErr = false) {
  let box = $('.toast');
  if (!box) {
    box = document.createElement('div');
    box.className = 'toast';
    document.body.appendChild(box);
  }
  box.textContent = msg;
  box.classList.toggle('err', !!isErr);
  box.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => box.classList.remove('show'), 2200);
}

/** 统一请求：401 跳登录，其它错误抛 Error(message)。 */
async function api(path, opts = {}) {
  const init = { credentials: 'same-origin', ...opts };
  if (init.body && typeof init.body !== 'string') {
    init.body = JSON.stringify(init.body);
    init.headers = { 'Content-Type': 'application/json', ...(init.headers || {}) };
  }
  const res = await fetch(path, init);
  if (res.status === 401) {
    location.href = '/login.html';
    throw new Error('未登录');
  }
  const text = await res.text();
  let body = null;
  try { body = text ? JSON.parse(text) : null; } catch (e) { /* 非 JSON */ }
  if (!res.ok) {
    throw new Error((body && body.message) || ('请求失败 HTTP ' + res.status));
  }
  if (body && body.code !== undefined && body.code !== 0) {
    throw new Error(body.message || '请求失败');
  }
  return body ? body.data : null;
}

/** 触发文件下载（后端已带 Content-Disposition）。 */
async function download(url, fallbackName) {
  try {
    const res = await fetch(url, { credentials: 'same-origin' });
    if (res.status === 401) { location.href = '/login.html'; return; }
    if (!res.ok) {
      const text = await res.text();
      let msg = '导出失败 HTTP ' + res.status;
      try { msg = JSON.parse(text).message || msg; } catch (e) { /* ignore */ }
      throw new Error(msg);
    }
    const disp = res.headers.get('Content-Disposition') || '';
    const m = /filename\*=UTF-8''([^;]+)/i.exec(disp) || /filename="?([^";]+)"?/i.exec(disp);
    const name = m ? decodeURIComponent(m[1]) : fallbackName;
    const blob = await res.blob();
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = name;
    document.body.appendChild(a);
    a.click();
    setTimeout(() => { URL.revokeObjectURL(a.href); a.remove(); }, 1000);
    toast('已导出 ' + name);
  } catch (e) {
    toast(e.message, true);
  }
}

/* ---------- 布局 ---------- */

const NAV = [
  { href: '/index.html', text: '人员管理', perm: 'user:read' },
  { href: '/dept.html', text: '部门管理', perm: 'dept:read' },
  { href: '/roles.html', text: '角色权限', perm: 'role:read' },
  { href: '/stats.html', text: '统计与导出', perm: 'stats:read' }
];

function renderSidebar(me) {
  const side = $('.sidebar');
  if (!side) return;
  const here = location.pathname.replace(/\/$/, '') || '/index.html';
  const links = NAV.filter((n) => me.perms.includes(n.perm))
    .map((n) => `<a href="${n.href}" class="${here === n.href ? 'active' : ''}">${n.text}</a>`)
    .join('');
  side.innerHTML = `
    <div class="brand">单位管理系统<small>人员 · 部门 · 权限</small></div>
    ${links}
    <div class="who">
      <b>${esc(me.realName)}</b>
      ${esc(me.deptName)} · ${esc(me.roleName)}
      <a href="#" id="logout" style="color:#93c5fd;display:inline-block;margin-top:6px">退出登录</a>
    </div>`;
  $('#logout').addEventListener('click', async (e) => {
    e.preventDefault();
    try { await api('/api/auth/logout', { method: 'POST' }); } catch (err) { /* ignore */ }
    location.href = '/login.html';
  });
}

/** 每个页面开头调用：拉当前用户、画侧栏、无权限则跳转到它能看的第一个页面。 */
async function boot(requiredPerm) {
  const me = await api('/api/auth/me');
  renderSidebar(me);
  $$('[data-perm]').forEach((el) => {
    if (!me.perms.includes(el.dataset.perm)) el.style.display = 'none';
  });
  if (requiredPerm && !me.perms.includes(requiredPerm)) {
    const first = NAV.find((n) => me.perms.includes(n.perm));
    location.href = first ? first.href : '/login.html';
    throw new Error('无权限');
  }
  return me;
}

/* ---------- 极简 SVG 图表 ---------- */

function svgEl(tag, attrs = {}) {
  const el = document.createElementNS('http://www.w3.org/2000/svg', tag);
  Object.entries(attrs).forEach(([k, v]) => el.setAttribute(k, v));
  return el;
}

/** 把最大值凑成 1/2/5 的整数刻度，坐标轴才不会出现 14/9.3/4.7 这种怪数字 */
function niceCeil(v) {
  if (!isFinite(v) || v <= 0) return 1;
  const mag = Math.pow(10, Math.floor(Math.log10(v / 3)));
  const step = [1, 2, 5, 10].map((m) => m * mag).find((s) => s * 3 >= v) || 10 * mag;
  return Math.ceil(v / step) * step;
}

/** 竖状柱图：items = [{label, value}] */
function chartBar(host, items, opts = {}) {
  const suffix = opts.suffix || '';
  host.innerHTML = '';
  const W = Math.max(host.clientWidth || 320, 280), H = 210;
  const padL = 34, padR = 12, padT = 18, padB = 34;
  const svg = svgEl('svg', { class: 'chart', viewBox: `0 0 ${W} ${H}` });
  const data = items.length ? items : [{ label: '暂无数据', value: 0 }];
  const max = niceCeil(Math.max(1, ...data.map((d) => Number(d.value) || 0)));
  const iw = W - padL - padR, ih = H - padT - padB;
  const step = iw / data.length;
  const bw = Math.min(46, step * 0.55);

  for (let i = 0; i <= 3; i++) {
    const y = padT + (ih * i) / 3;
    svg.appendChild(svgEl('line', { class: 'axis', x1: padL, y1: y, x2: W - padR, y2: y }));
    const t = svgEl('text', { x: padL - 8, y: y + 4, 'text-anchor': 'end' });
    const raw = max * (3 - i) / 3;
    t.textContent = Number.isInteger(raw) ? raw : raw.toFixed(1);
    svg.appendChild(t);
  }

  data.forEach((d, i) => {
    const v = Number(d.value) || 0;
    const h = (v / max) * ih;
    const x = padL + step * i + (step - bw) / 2;
    const y = padT + ih - h;
    svg.appendChild(svgEl('rect', {
      class: 'bar' + (i % 2 ? ' alt' : ''), x, y, width: bw, height: Math.max(h, 0), rx: 4
    }));
    const val = svgEl('text', { x: x + bw / 2, y: y - 5, 'text-anchor': 'middle' });
    val.textContent = v + suffix;
    svg.appendChild(val);
    const lab = svgEl('text', { x: x + bw / 2, y: H - padB + 16, 'text-anchor': 'middle' });
    const text = String(d.label);
    lab.textContent = text.length > 6 ? text.slice(0, 6) : text;
    svg.appendChild(lab);
  });
  host.appendChild(svg);
}

/** 环形图 + 右侧图例：items = [{label, value}] */
function chartDonut(host, items) {
  host.innerHTML = '';
  const W = Math.max(host.clientWidth || 320, 280), H = 210;
  const svg = svgEl('svg', { class: 'chart', viewBox: `0 0 ${W} ${H}` });
  const data = (items || []).filter((d) => Number(d.value) > 0);
  const total = data.reduce((s, d) => s + Number(d.value), 0) || 1;
  const colors = ['#2563eb', '#60a5fa', '#34d399', '#f59e0b', '#a78bfa', '#ef4444'];
  const cx = 96, cy = 105, r = 62;
  let acc = 0;
  data.forEach((d, i) => {
    const frac = Number(d.value) / total;
    const len = 2 * Math.PI * r;
    svg.appendChild(svgEl('circle', {
      cx, cy, r, fill: 'none', stroke: colors[i % colors.length], 'stroke-width': 20,
      'stroke-dasharray': `${len * frac - 2} ${len}`,
      'stroke-dashoffset': -acc,
      transform: `rotate(-90 ${cx} ${cy})`
    }));
    acc += len * frac;

    const ly = 46 + i * 26;
    svg.appendChild(svgEl('rect', { x: 186, y: ly - 8, width: 10, height: 10, rx: 2, fill: colors[i % colors.length] }));
    const t = svgEl('text', { x: 202, y: ly });
    const pct = (frac * 100).toFixed(0);
    t.textContent = `${d.label}  ${d.value}（${pct}%）`;
    svg.appendChild(t);
  });
  const center = svgEl('text', { x: cx, y: cy + 6, 'text-anchor': 'middle', style: 'font-size:18px;fill:#1f2937;font-weight:700' });
  center.textContent = total;
  svg.appendChild(center);
  host.appendChild(svg);
}

/** 折线图：items = [{label, value}]，纵轴按数据范围自适应（不从 0 起，否则趋势看着是平的） */
function chartLine(host, items) {
  host.innerHTML = '';
  const W = Math.max(host.clientWidth || 320, 280), H = 210;
  const padL = 34, padR = 14, padT = 18, padB = 34;
  const svg = svgEl('svg', { class: 'chart', viewBox: `0 0 ${W} ${H}` });
  const data = items || [];
  const vals = data.map((d) => Number(d.value) || 0);
  const vmax = Math.max(1, ...vals), vmin = Math.min(...(vals.length ? vals : [0]));
  const span = Math.max(vmax - vmin, 0.1);
  const hi = vmax + Math.max(0.5, span * 0.3);
  const lo = Math.max(0, vmin - Math.max(0.5, span * 0.9));
  const iw = W - padL - padR, ih = H - padT - padB;
  for (let i = 0; i <= 3; i++) {
    const y = padT + (ih * i) / 3;
    svg.appendChild(svgEl('line', { class: 'axis', x1: padL, y1: y, x2: W - padR, y2: y }));
    const t = svgEl('text', { x: padL - 8, y: y + 4, 'text-anchor': 'end' });
    t.textContent = (lo + (hi - lo) * (3 - i) / 3).toFixed(1);
    svg.appendChild(t);
  }
  const stepX = data.length > 1 ? iw / (data.length - 1) : 0;
  const pts = data.map((d, i) => {
    const x = padL + stepX * i;
    const y = padT + ih - ((Number(d.value) || 0) - lo) / (hi - lo) * ih;
    return { x, y, d };
  });
  if (pts.length > 1) {
    svg.appendChild(svgEl('polyline', {
      points: pts.map((p) => `${p.x},${p.y}`).join(' '),
      fill: 'none', stroke: '#2563eb', 'stroke-width': 2.5, 'stroke-linejoin': 'round'
    }));
  }
  pts.forEach((p, i) => {
    svg.appendChild(svgEl('circle', { cx: p.x, cy: p.y, r: 4, fill: '#fff', stroke: '#2563eb', 'stroke-width': 2 }));
    const val = svgEl('text', { x: p.x, y: p.y - 9, 'text-anchor': 'middle' });
    val.textContent = p.d.value;
    svg.appendChild(val);
    const lab = svgEl('text', { x: p.x, y: H - padB + 16, 'text-anchor': 'middle' });
    lab.textContent = String(p.d.label).slice(5);
    svg.appendChild(lab);
    if (i === 0 && data.length === 1) { /* 单点时也能看清 */ }
  });
  host.appendChild(svg);
}

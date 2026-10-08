// ที่เก็บชั่วคราวในเบราว์เซอร์ (localStorage) — เปลี่ยนเป็นเรียก API/DB จริงภายหลัง
const S = { get: (k, d) => { try { return JSON.parse(localStorage.getItem('sm_' + k)) ?? d } catch { return d } }, set: (k, v) => localStorage.setItem('sm_' + k, JSON.stringify(v)) }
const $ = (s, r = document) => r.querySelector(s), $$ = (s, r = document) => [...r.querySelectorAll(s)]
const baht = n => '฿' + Number(n || 0).toLocaleString('th-TH', { maximumFractionDigits: 2 })
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]))
const ST = { pending: 'รอจ่าย', paid: 'จ่ายครบแล้ว', overdue: 'เกินกำหนด' }
const fmtDate = d => d ? new Date(d).toLocaleDateString('th-TH', { day: 'numeric', month: 'short', year: 'numeric' }) : '-'
const getProfile = () => S.get('profile', { name: 'ผู้ใช้ใหม่', email: '', phone: '', bio: '' })
const empty = (ic, t, h, a = '') => `<div class="state">${ic}<b style="color:var(--ink)">${t}</b>${h}${a}</div>`
const qid = () => +new URLSearchParams(location.search).get('id')
const newId = list => list.reduce((m, x) => Math.max(m, x.id), 0) + 1
// เติมชื่อ/อีเมล/ตัวอักษรย่อของผู้ใช้ใน sidebar และ topbar
const _me = getProfile()
$$('[data-pname]').forEach(e => e.textContent = _me.name)
$$('[data-pshort]').forEach(e => e.textContent = _me.name.split(' ')[0])
$$('[data-pemail]').forEach(e => e.textContent = _me.email || 'ยังไม่ระบุอีเมล')
$$('[data-pinit]').forEach(e => e.textContent = (_me.name[0] || 'P').toUpperCase())

// ระบบแจ้งเตือน (กระดิ่ง) — แสดงเป็น popup กลางจอ
const notify = t => { if (!S.get('notify', true)) return; const l = S.get('notifs', []); l.unshift({ t, at: Date.now(), read: false }); S.set('notifs', l.slice(0, 30)) }
const bell = $('#bell')
if (bell) {
  const d = document.createElement('dialog'); document.body.appendChild(d)
  const paint = () => bell.classList.toggle('has', S.get('notifs', []).some(n => !n.read))
  paint()
  bell.onclick = () => {
    const l = S.get('notifs', [])
    d.innerHTML = '<div class="row sp" style="margin-bottom:12px"><h2>การแจ้งเตือน</h2><button class="btn ghost sm" id="nx" aria-label="ปิด">✕</button></div>' + (l.length
      ? '<div class="list">' + l.map(n => `<div><div class="ico" style="background:var(--g-l)">🔔</div><div style="flex:1"><b style="font-weight:500">${esc(n.t)}</b><div class="muted">${new Date(n.at).toLocaleString('th-TH', { dateStyle: 'medium', timeStyle: 'short' })}</div></div></div>`).join('') + '</div><button class="btn soft block" id="nc" style="margin-top:12px">ล้างการแจ้งเตือน</button>'
      : empty('🔔', 'ยังไม่มีการแจ้งเตือน', 'เมื่อมีบิลหรือกลุ่มใหม่ จะแจ้งให้ทราบที่นี่'))
    d.showModal(); S.set('notifs', l.map(n => ({ ...n, read: true }))); paint()
    $('#nx', d).onclick = () => d.close(); const c = $('#nc', d); if (c) c.onclick = () => { S.set('notifs', []); d.close() }
  }
  d.addEventListener('click', e => { if (e.target === d) d.close() })
}

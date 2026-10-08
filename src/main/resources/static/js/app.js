const $ = (s, r = document) => r.querySelector(s)
const $$ = (s, r = document) => [...r.querySelectorAll(s)]
const baht = n => '฿' + Number(n || 0).toLocaleString('th-TH', { maximumFractionDigits: 2 })
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]))
const fmtDate = d => d ? new Date(d + (String(d).length === 10 ? 'T00:00:00' : '')).toLocaleDateString('th-TH', { day: 'numeric', month: 'short', year: 'numeric' }) : '-'
const qid = () => Number(new URLSearchParams(location.search).get('id'))
const empty = (ic, t, h, a = '') => `<div class="state">${ic}<b style="color:var(--ink)">${t}</b>${h}${a}</div>`
const newId = list => list.reduce((m, x) => Math.max(m, Number(x.id || 0)), 0) + 1

const S = {
  get: (k, d) => { try { return JSON.parse(localStorage.getItem('sm_' + k)) ?? d } catch { return d } },
  set: (k, v) => localStorage.setItem('sm_' + k, JSON.stringify(v)),
  remove: k => localStorage.removeItem('sm_' + k)
}

const ST = { DRAFT: 'ฉบับร่าง', PENDING: 'รอจ่าย', PAID: 'จ่ายแล้ว', OVERDUE: 'เกินกำหนด', CANCELLED: 'ยกเลิก' }
const statusKey = status => String(status || 'PENDING').toUpperCase() === 'PAID' ? 'paid' : String(status || '').toUpperCase() === 'OVERDUE' ? 'overdue' : String(status || '').toUpperCase() === 'CANCELLED' ? 'cancelled' : String(status || '').toUpperCase() === 'DRAFT' ? 'pending' : 'pending'
const api = {
  request: async (url, options = {}) => {
    const res = await fetch(url, { credentials: 'same-origin', headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }, ...options })
    if (!res.ok) {
      let message = `HTTP ${res.status}`
      try { const body = await res.json(); message = body.message || body.error || message } catch {}
      const err = new Error(message); err.status = res.status; throw err
    }
    if (res.status === 204) return null
    const type = res.headers.get('content-type') || ''
    return type.includes('application/json') ? res.json() : res.text()
  },
  get: url => api.request(url),
  post: (url, body) => api.request(url, { method: 'POST', body: JSON.stringify(body) }),
  put: (url, body) => api.request(url, { method: 'PUT', body: JSON.stringify(body) }),
  patch: (url, body) => api.request(url, { method: 'PATCH', body: JSON.stringify(body) }),
  delete: url => api.request(url, { method: 'DELETE' })
}

const isAuthPage = !!document.body?.classList.contains('auth')
const SM = (() => {
  const ready = isAuthPage ? Promise.resolve(null) : api.get('/api/v1/session/me').then(user => {
    S.set('profile', { name: user.username, email: user.email, phone: user.phone || '', bio: user.bio || '' })
    sessionStorage.removeItem('sm_auth_redirected')
    return { user }
  }).catch(err => {
    // 404 means the API route/resource is missing, not that the user is logged out.
    // Redirect only once for a real unauthenticated session (401).
    if (err.status === 401 && location.pathname !== '/login' && !sessionStorage.getItem('sm_auth_redirected')) {
      sessionStorage.setItem('sm_auth_redirected', '1')
      location.replace('/login')
    }
    throw err
  })
  return {
    ready,
    api,
    getProfile: () => {
      const cached = S.get('profile', { name: 'ผู้ใช้ใหม่', email: '', phone: '', bio: '' })
      return cached
    },
    loadBills: async () => {
      const page = await api.get('/api/v1/bills?size=100&sort=createdAt,desc')
      return page.content || []
    },
    loadGroups: async () => {
      const page = await api.get('/api/v1/groups?size=100&sort=createdAt,desc')
      return page.content || []
    }
  }
})()

const getProfile = SM.getProfile
const notify = t => {
  if (!S.get('notify', true)) return
  const list = S.get('notifs', [])
  list.unshift({ t, at: Date.now(), read: false })
  S.set('notifs', list.slice(0, 30))
}

const updateShellUser = user => {
  if (!user) return
  const name = user.username || 'ผู้ใช้'
  $$('[data-pname]').forEach(e => e.textContent = name)
  $$('[data-pshort]').forEach(e => e.textContent = name.split(' ')[0])
  $$('[data-pemail]').forEach(e => e.textContent = user.email || 'ยังไม่ระบุอีเมล')
  $$('[data-pinit]').forEach(e => e.textContent = (name[0] || 'P').toUpperCase())
}

if (!isAuthPage) {
  SM.ready.then(({ user }) => updateShellUser(user)).catch(() => {})
}

const bell = $('#bell')
if (bell) {
  const d = document.createElement('dialog'); document.body.appendChild(d)
  const paint = () => bell.classList.toggle('has', S.get('notifs', []).some(n => !n.read))
  paint()
  bell.onclick = () => {
    const list = S.get('notifs', [])
    d.innerHTML = '<div class="row sp" style="margin-bottom:12px"><h2>การแจ้งเตือน</h2><button class="btn ghost sm" id="nx" aria-label="ปิด">✕</button></div>' + (list.length
      ? '<div class="list">' + list.map(n => `<div><div class="ico" style="background:var(--g-l)">🔔</div><div style="flex:1"><b style="font-weight:500">${esc(n.t)}</b><div class="muted">${new Date(n.at).toLocaleString('th-TH', { dateStyle: 'medium', timeStyle: 'short' })}</div></div></div>`).join('') + '</div><button class="btn soft block" id="nc" style="margin-top:12px">ล้างการแจ้งเตือน</button>'
      : empty('🔔', 'ยังไม่มีการแจ้งเตือน', 'เมื่อมีบิลหรือกลุ่มใหม่ จะแจ้งให้ทราบที่นี่'))
    d.showModal(); S.set('notifs', list.map(n => ({ ...n, read: true }))); paint()
    $('#nx', d).onclick = () => d.close()
    const c = $('#nc', d); if (c) c.onclick = () => { S.set('notifs', []); d.close(); paint() }
  }
  d.addEventListener('click', e => { if (e.target === d) d.close() })
}

// ฟังก์ชันกลางที่ทุกหน้าใช้: เรียก REST API, ข้อมูลผู้ใช้ที่ล็อกอิน, กระดิ่งแจ้งเตือน และการเข้าร่วมบิลด้วยรหัส
// ข้อมูลบิล/โปรไฟล์เก็บในฐานข้อมูลผ่าน /api/v1/** — localStorage ใช้แค่ cache ชื่อผู้ใช้และประวัติแจ้งเตือนในเครื่อง

const S = {
  get: (k, d) => {
    try {
      return JSON.parse(localStorage.getItem('sm_' + k)) ?? d
    } catch {
      return d
    }
  },
  set: (k, v) => {
    try { localStorage.setItem('sm_' + k, JSON.stringify(v)) } catch {}
  },
  remove: k => {
    try { localStorage.removeItem('sm_' + k) } catch {}
  }
}

const $ = (s, r = document) => r.querySelector(s)
const $$ = (s, r = document) => [...r.querySelectorAll(s)]

const baht = n =>
  '฿' + Number(n || 0).toLocaleString('th-TH', {
    maximumFractionDigits: 2
  })

const esc = s =>
  String(s ?? '').replace(/[&<>"']/g, c => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  }[c]))

// สถานะบิลจาก server (ตัวพิมพ์ใหญ่) -> ข้อความภาษาไทย
const ST = {
  DRAFT: 'ฉบับร่าง',
  PENDING: 'รอจ่าย',
  PAID: 'จ่ายแล้ว',
  OVERDUE: 'เกินกำหนด',
  CANCELLED: 'ยกเลิก'
}

// สถานะ -> class ของ pill และตัวกรอง (paid / pending / overdue / cancelled)
const statusKey = status => {
  const s = String(status || 'PENDING').toUpperCase()
  if (s === 'PAID') return 'paid'
  if (s === 'OVERDUE') return 'overdue'
  if (s === 'CANCELLED') return 'cancelled'
  return 'pending'
}

const statusLabel = status => ST[String(status || 'PENDING').toUpperCase()] || 'รอจ่าย'

// วันที่จาก server มาเป็น 'YYYY-MM-DD' — แปลงเป็นเวลาท้องถิ่นเพื่อไม่ให้วันเลื่อน
const fmtDate = d =>
  d
    ? new Date(String(d).length === 10 ? d + 'T00:00:00' : d).toLocaleDateString('th-TH', {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
      })
    : '-'

const empty = (ic, t, h, a = '') =>
  `<div class="state">${ic}<b style="color:var(--ink)">${t}</b>${h}${a}</div>`

const qid = () => Number(new URLSearchParams(location.search).get('id'))

// ---------- REST API ----------
const api = {
  request: async (url, options = {}) => {
    const isForm = options.body instanceof FormData
    const res = await fetch(url, {
      credentials: 'same-origin',
      ...options,
      headers: isForm ? (options.headers || {}) : { 'Content-Type': 'application/json', ...(options.headers || {}) }
    })

    if (!res.ok) {
      let message = `HTTP ${res.status}`
      try {
        const body = await res.json()
        message = body.message || body.error || message
      } catch {}
      if (res.status === 401 && !isAuthPage) location.replace('/login')
      const err = new Error(message)
      err.status = res.status
      throw err
    }

    if (res.status === 204) return null
    const type = res.headers.get('content-type') || ''
    return type.includes('application/json') ? res.json() : res.text()
  },
  get: url => api.request(url),
  post: (url, body) => api.request(url, { method: 'POST', body: JSON.stringify(body) }),
  put: (url, body) => api.request(url, { method: 'PUT', body: body === undefined ? undefined : JSON.stringify(body) }),
  patch: (url, body) => api.request(url, { method: 'PATCH', body: JSON.stringify(body) }),
  delete: url => api.request(url, { method: 'DELETE' }),
  upload: (url, file) => {
    const fd = new FormData()
    fd.append('file', file)
    return api.request(url, { method: 'POST', body: fd })
  }
}

// ---------- ผู้ใช้ที่ล็อกอิน ----------
const isAuthPage = /^\/(login|register)(;|$)/.test(location.pathname)

const getProfile = () =>
  S.get('profile', {
    name: '',
    email: '',
    phone: '',
    bio: ''
  })

// เติมชื่อ/อีเมล/ตัวอักษรย่อของผู้ใช้ใน sidebar และ topbar
const paintMe = p => {
  const name = p.name || ''
  $$('[data-pname]').forEach(e => e.textContent = name)
  $$('[data-pshort]').forEach(e => e.textContent = name.split(' ')[0])
  $$('[data-pemail]').forEach(e => {
    e.textContent = p.email || ''
  })
  $$('[data-pinit]').forEach(e => {
    e.textContent = (name[0] || 'P').toUpperCase()
  })
}

const SM = (() => {
  if (isAuthPage) {
    // อยู่หน้า login/สมัคร = ยังไม่มีใครล็อกอิน ล้างข้อมูลผู้ใช้เก่าที่ค้างในเบราว์เซอร์
    S.remove('profile')
    return { ready: new Promise(() => {}), loadBills: async () => [] }
  }

  // cache ของผู้ใช้คนเดิม แสดงไปก่อนระหว่างรอ server
  paintMe(getProfile())

  const ready = api.get('/api/v1/users/me').then(user => {
    S.set('profile', { id: user.id, name: user.username, email: user.email, phone: user.phone || '', bio: user.bio || '' })
    S.set('notify', user.notificationEnabled !== false)
    paintMe(getProfile())
    return { user }
  })

  return {
    ready,
    // บิลที่ผู้ใช้สร้าง + บิลที่เข้าร่วม (ใหม่ไปเก่า)
    loadBills: async () => {
      const page = await api.get('/api/v1/bills?size=100&sort=createdAt,desc')
      return page.content || []
    }
  }
})()

// ---------- แจ้งเตือนในเครื่อง ----------
const notify = t => {
  if (!S.get('notify', true)) return
  const l = S.get('notifs', [])
  l.unshift({ t, at: Date.now(), read: false })
  S.set('notifs', l.slice(0, 30))
}

// กระดิ่ง: คำเชิญเข้าบิลจาก server + ประวัติแจ้งเตือนในเครื่อง
const bell = $('#bell')

if (bell && !isAuthPage) {
  const d = document.createElement('dialog')
  document.body.appendChild(d)
  d.addEventListener('click', e => {
    if (e.target === d) d.close()
  })

  let invites = []

  const paint = () =>
    bell.classList.toggle('has', invites.length > 0 || S.get('notifs', []).some(n => !n.read))

  const loadInvites = async () => {
    try {
      invites = await api.get('/api/v1/invitations')
    } catch {
      invites = []
    }
    paint()
  }

  paint()
  SM.ready.then(loadInvites).catch(() => {})

  const respond = async (btn, action) => {
    btn.disabled = true
    try {
      await api.put('/api/v1/invitations/' + btn.dataset.id + '/' + action)
      const inv = invites.find(v => String(v.id) === btn.dataset.id)
      if (action === 'accept') {
        notify('เข้าร่วมบิล "' + (inv?.billName || '') + '" เรียบร้อยแล้ว')
        location = '/bills/detail?id=' + inv.billId
        return
      }
      notify('ปฏิเสธคำเชิญแล้ว')
      invites = invites.filter(v => v !== inv)
      d.close()
      paint()
    } catch (err) {
      alert(err.message)
      btn.disabled = false
    }
  }

  bell.onclick = async () => {
    await loadInvites()
    const l = S.get('notifs', [])

    const invHtml = invites.length
      ? '<h2 style="margin:12px 0 6px">คำเชิญเข้าบิล</h2><div class="list">' +
        invites.map(v => `
          <div>
            <div class="ico" style="background:#fff1e0">🍽️</div>
            <div style="flex:1">
              <b style="font-weight:500">${esc(v.inviterName)} เชิญคุณเข้าร่วมบิล</b>
              <div class="muted">${esc(v.billName)}</div>
              <div class="row" style="margin-top:6px;gap:8px">
                <button class="btn soft sm" data-inv="accept" data-id="${v.id}">ยอมรับ</button>
                <button class="btn ghost sm" data-inv="reject" data-id="${v.id}">ปฏิเสธ</button>
              </div>
            </div>
          </div>
        `).join('') + '</div>'
      : ''

    const histHtml = l.length
      ? '<h2 style="margin:12px 0 6px">ประวัติการแจ้งเตือน</h2><div class="list">' +
        l.map(n => `
          <div>
            <div class="ico" style="background:var(--g-l)">🔔</div>
            <div style="flex:1">
              <b style="font-weight:500">${esc(n.t)}</b>
              <div class="muted">
                ${new Date(n.at).toLocaleString('th-TH', { dateStyle: 'medium', timeStyle: 'short' })}
              </div>
            </div>
          </div>
        `).join('') + '</div>' +
        '<button class="btn soft block" id="nc" style="margin-top:12px">ล้างการแจ้งเตือน</button>'
      : (invites.length ? '' : empty('🔔', 'ยังไม่มีการแจ้งเตือน', 'เมื่อมีคำเชิญหรือบิลใหม่ จะแจ้งให้ทราบที่นี่'))

    d.innerHTML =
      '<div class="row sp" style="margin-bottom:12px">' +
      '<h2>การแจ้งเตือน</h2>' +
      '<button class="btn ghost sm" id="nx" aria-label="ปิด">✕</button>' +
      '</div>' + invHtml + histHtml

    d.showModal()
    S.set('notifs', l.map(n => ({ ...n, read: true })))
    paint()

    $('#nx', d).onclick = () => d.close()
    const c = $('#nc', d)
    if (c) {
      c.onclick = () => {
        S.set('notifs', [])
        d.close()
        paint()
      }
    }
    $$('[data-inv]', d).forEach(btn => {
      btn.onclick = () => respond(btn, btn.dataset.inv)
    })
  }
}

// ---------- เข้าร่วมบิลด้วยรหัส ----------
const openJoin = (prefill = '') => {
  let d = $('#joinDlg')
  if (!d) {
    d = document.createElement('dialog')
    d.id = 'joinDlg'
    document.body.appendChild(d)
    d.addEventListener('click', e => {
      if (e.target === d) d.close()
    })
  }

  d.innerHTML = `
    <form id="joinF">
      <h2 style="margin-bottom:6px">เข้าร่วมบิลของเพื่อน</h2>
      <p class="muted" style="margin-bottom:14px">กรอกรหัสบิลที่เพื่อนส่งมาให้ เช่น SM-AB12CD</p>
      <div class="field">
        <label for="joinCode">รหัสบิล</label>
        <input class="in" id="joinCode" name="code" required maxlength="12" autocomplete="off"
          placeholder="SM-XXXXXX" style="text-transform:uppercase" value="${esc(prefill)}">
      </div>
      <div class="err" id="joinErr"></div>
      <div class="row" style="justify-content:flex-end">
        <button type="button" class="btn ghost" id="joinX">ยกเลิก</button>
        <button class="btn">เข้าร่วมบิล</button>
      </div>
    </form>`

  $('#joinX', d).onclick = () => d.close()
  $('#joinF', d).onsubmit = async e => {
    e.preventDefault()
    const btn = e.submitter
    if (btn) btn.disabled = true
    try {
      const bill = await api.post('/api/v1/bills/join', { code: e.target.code.value })
      notify('เข้าร่วมบิล "' + bill.restaurantName + '" เรียบร้อยแล้ว')
      location = '/bills/detail?id=' + bill.id
    } catch (err) {
      $('#joinErr', d).textContent = err.status === 404 ? 'ไม่พบบิลที่ใช้รหัสนี้' : err.message
      if (btn) btn.disabled = false
    }
  }
  d.showModal()
}

document.addEventListener('click', e => {
  if (e.target.closest('[data-join]')) openJoin()
})

// เปิดจากลิงก์เชิญ /join?code=... (server พามาที่ /bills?join=...)
if (!isAuthPage) {
  const c = new URLSearchParams(location.search).get('join')
  if (c) {
    history.replaceState(null, '', location.pathname)
    SM.ready.then(() => openJoin(c)).catch(() => {})
  }
}

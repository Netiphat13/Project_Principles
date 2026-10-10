// โฟลว์สร้างบิล 5 ขั้น — คำนวณยอดใน browser แบบเรียลไทม์ แล้วบันทึกลงฐานข้อมูลผ่าน /api/v1/bills
(async () => {
  const { user } = await SM.ready
  const me = user.username
  const COL = ['#bfeadd', '#ffd9a8', '#d9d0ff', '#c5dcff', '#ffc9c0', '#fff0a8']
  const ICONS = ['🍽️', '🍕', '🍗', '🥤', '🍟', '🍜', '🍣', '🥩', '🍰']
  const STEPS = ['ข้อมูลบิล', 'สมาชิก', 'รายการอาหาร', 'วิธีหาร', 'สรุป']

  const TITLES = [
    ['สร้างบิลใหม่', 'กรอกข้อมูลบิลเบื้องต้น'],
    ['เพิ่มสมาชิก', 'เลือกเพื่อนที่ร่วมมื้ออาหาร'],
    ['เพิ่มรายการอาหาร', 'ระบุว่าใครกินอะไร เพื่อคำนวณอย่างแฟร์'],
    ['เลือกวิธีหาร', 'เลือกวิธีที่เหมาะกับมื้อนี้ที่สุด'],
    ['สรุปและชำระ', 'ตรวจสอบยอดก่อนบันทึกบิล']
  ]

  const METHODS = [
    ['equal', '⚖️', 'หารเท่ากัน', 'Equal Split'],
    ['item', '🍕', 'ตามรายการที่กิน', 'Item Based'],
    ['percentage', '％', 'ตามสัดส่วน', 'Percentage'],
    ['treat', '🎁', 'เลี้ยงเพื่อน', 'Treat Someone'],
    ['custom', '✏️', 'กำหนดเอง', 'Custom Split']
  ]

  // ชื่อเพื่อนที่ไม่มีบัญชี (เพิ่มเอง) เก็บไว้ในเครื่องเพื่อใช้ซ้ำ
  let friends = S.get('friends', [])

  // เพื่อนที่มีบัญชี: ค้นด้วยอีเมลแล้วส่งคำเชิญตอนบันทึกบิล
  const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/
  let found = null
  let lookupMsg = ''
  let lookupTimer
  let lookupSeq = 0

  // รหัสเข้าร่วมบิลจาก server (ถ้าเรียกไม่ได้ server จะสุ่มให้ตอนบันทึก)
  let serverCode = ''
  try {
    serverCode = (await api.get('/api/v1/bills/join-code/next')).code
  } catch {}

  const st = {
    step: 0,
    info: {
      shop: '',
      // วันที่ตามเวลาในเครื่อง (toISOString เป็น UTC ทำให้ช่วงเช้าวันเลื่อนไปเมื่อวาน)
      date: new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 10),
      time: '',
      note: ''
    },
    receipt: null,
    sel: [me],
    items: [],
    disc: 0,
    svc: 10,
    vat: 7,
    method: 'equal',
    pct: {},
    treat: me,
    custom: {},
    paid: { [me]: true },
    code: serverCode,
    invited: [],
    q: '',
    saving: false,
    msg: ''
  }

  const all = () => [...new Set([me, ...friends, ...st.invited.map(u => u.username)])]
  const joinLink = () => location.origin + '/join?code=' + encodeURIComponent(st.code)

  const resultsHtml = () => found
    ? `<div class="kv">
        <span class="row">${av(found.username, 28)}<span><b>${esc(found.username)}</b><span class="muted" style="display:block">${esc(found.email)}</span></span></span>
        <button type="button" class="btn soft sm" data-act="inv">＋ เพิ่ม</button>
      </div>`
    : (lookupMsg ? `<p class="muted">${esc(lookupMsg)}</p>` : '')

  const lookup = email => {
    clearTimeout(lookupTimer)
    found = null
    lookupMsg = ''
    const v = email.trim()
    const seq = ++lookupSeq

    if (!EMAIL_RE.test(v)) {
      $('#invr').innerHTML = ''
      return
    }

    lookupTimer = setTimeout(async () => {
      try {
        const u = await api.get('/api/v1/users/lookup?email=' + encodeURIComponent(v))
        if (seq !== lookupSeq) return
        if (u.id === user.id) lookupMsg = 'นี่คืออีเมลของคุณเอง'
        else if (st.invited.some(x => x.id === u.id)) lookupMsg = 'เพิ่มคนนี้แล้ว'
        else if (all().includes(u.username)) lookupMsg = 'มีชื่อ "' + u.username + '" ในบิลแล้ว'
        else found = u
      } catch (err) {
        if (seq !== lookupSeq) return
        lookupMsg = err.status === 404 ? 'ไม่พบผู้ใช้ที่ใช้อีเมลนี้' : err.message
      }
      $('#invr').innerHTML = resultsHtml()
    }, 250)
  }
  const color = n => COL[all().indexOf(n) % COL.length]

  const av = (n, s = 40) =>
    `<div class="av" style="background:${color(n)};width:${s}px;height:${s}px">${esc((n[0] || '?').toUpperCase())}</div>`

  const money = n =>
    '฿' + (+n).toLocaleString('th-TH', { maximumFractionDigits: 2 })

  const nid = () => Math.max(0, ...st.items.map(i => i.id)) + 1

  function readReceiptFile(file) {
    if (!file) return

    const allowed = ['image/jpeg', 'image/png', 'image/webp', 'image/gif', 'application/pdf']

    if (!allowed.includes(file.type)) {
      st.msg = 'รองรับเฉพาะไฟล์ JPG, PNG, WEBP, GIF หรือ PDF'
      render()
      return
    }

    if (file.size > 8 * 1024 * 1024) {
      st.msg = 'ไฟล์ใหญ่เกินไป กรุณาเลือกไฟล์ไม่เกิน 8 MB'
      render()
      return
    }

    st.msg = ''

    // ย่อรูปไว้แสดงตัวอย่าง ส่วนไฟล์จริงจะอัปโหลดขึ้น server ตอนบันทึกบิล
    if (file.type.startsWith('image/')) {
      const fr = new FileReader()

      fr.onload = () => {
        const img = new Image()

        img.onload = () => {
          const max = 1400
          const scale = Math.min(
            1,
            max / Math.max(img.width, img.height)
          )
          const canvas = document.createElement('canvas')

          canvas.width = Math.max(1, Math.round(img.width * scale))
          canvas.height = Math.max(1, Math.round(img.height * scale))

          canvas.getContext('2d').drawImage(
            img, 0, 0, canvas.width, canvas.height
          )

          st.receipt = {
            name: file.name,
            type: file.type,
            file,
            dataUrl: canvas.toDataURL('image/jpeg', .82)
          }

          render()
        }

        img.src = fr.result
      }

      fr.readAsDataURL(file)
    } else {
      st.receipt = {
        name: file.name,
        type: file.type,
        file,
        dataUrl: ''
      }

      render()
    }
  }

  function calc() {
    const m = st.sel
    const n = m.length
    const zero = () => Object.fromEntries(m.map(x => [x, 0]))

    const sub = st.items.reduce((a, i) => a + i.price * i.qty, 0)
    const disc = Math.min(+st.disc || 0, sub)
    const svc = sub * st.svc / 100
    const vat = sub * st.vat / 100
    const net = sub - disc + svc + vat

    const byItem = () => {
      const f = zero()

      st.items.forEach(i => {
        const e = i.eaters.filter(x => m.includes(x))
        const t = e.length ? e : m

        t.forEach(x => {
          f[x] += i.price * i.qty / t.length
        })
      })

      return f
    }

    let f = null

    if (st.method === 'item') {
      f = byItem()
    } else if (st.method === 'percentage') {
      f = Object.fromEntries(
        m.map(x => [x, sub * (st.pct[x] ?? 100 / n) / 100])
      )
    } else if (st.method === 'treat') {
      f = Object.fromEntries(
        m.map(x => [x, x === st.treat ? sub : 0])
      )
    } else if (st.method === 'equal') {
      f = Object.fromEntries(m.map(x => [x, sub / n]))
    }

    const share = Object.fromEntries(
      m.map(x => [
        x,
        f ? (sub ? f[x] / sub * net : 0) : (+st.custom[x] || 0)
      ])
    )

    const ib = byItem()
    const dev = net
      ? m.reduce(
          (a, x) => a + Math.abs(
            share[x] - (sub ? ib[x] / sub * net : 0)
          ),
          0
        ) / (2 * net)
      : 0

    return {
      sub,
      disc,
      svc,
      vat,
      net,
      share,
      fair: Math.max(0, Math.round(100 - dev * 100)),
      sum: Object.values(share).reduce((a, b) => a + b, 0)
    }
  }

  const pctSum = () =>
    st.sel.reduce(
      (a, x) => a + (st.pct[x] ?? 100 / st.sel.length),
      0
    )

  function check() {
    if (st.step === 0 && !st.info.shop.trim()) {
      return 'กรุณากรอกชื่อร้านอาหาร'
    }

    if (st.step === 2 && !st.items.length) {
      return 'เพิ่มรายการอาหารอย่างน้อย 1 รายการ'
    }

    if (st.step === 3) {
      if (
        st.method === 'percentage' &&
        Math.abs(pctSum() - 100) > .01
      ) {
        return 'สัดส่วนรวมต้องเท่ากับ 100%'
      }

      if (
        st.method === 'custom' &&
        Math.abs(calc().sum - calc().net) > .5
      ) {
        return 'ยอดที่กำหนดต้องรวมเท่ากับยอดสุทธิ'
      }
    }

    return ''
  }

  const body = [
    // ขั้นที่ 1: ข้อมูลบิลและใบเสร็จ
    () => `
      <div class="grid g2e">
        <div class="card">
          <h2 style="margin-bottom:14px">ข้อมูลบิล</h2>

          <div class="field">
            <label>ชื่อร้านอาหาร</label>
            <input
              class="in"
              data-in="shop"
              maxlength="60"
              placeholder="เช่น MK Restaurant"
              value="${esc(st.info.shop)}"
            >
          </div>

          <div class="grid g2e">
            <div class="field">
              <label>วันที่</label>
              <input
                type="date"
                class="in"
                data-in="date"
                value="${st.info.date}"
              >
            </div>

            <div class="field">
              <label>เวลา</label>
              <input
                type="time"
                class="in"
                data-in="time"
                value="${st.info.time}"
              >
            </div>
          </div>

          <div class="field">
            <label>โน้ต (ไม่บังคับ)</label>
            <textarea
              class="in"
              data-in="note"
              rows="3"
              placeholder="เช่น มื้อฉลองสอบเสร็จ..."
            >${esc(st.info.note)}</textarea>
          </div>
        </div>

        <div class="card">
          <div class="row sp">
            <h2>แนบสลิป / ใบเสร็จ</h2>
            <span class="tag">ไม่บังคับ</span>
          </div>

          <div class="drop receipt-drop" style="margin-top:14px">
            <div class="ico" style="background:#fff">🧾</div>

            <b>
              ${st.receipt
                ? 'แนบใบเสร็จเรียบร้อยแล้ว'
                : 'ลากใบเสร็จมาวางที่นี่'}
            </b>

            <span class="muted">รองรับ JPG, PNG, WEBP, GIF, PDF · ไม่เกิน 8 MB</span>

            <input
              id="receipt-file"
              type="file"
              accept="image/jpeg,image/png,image/webp,image/gif,application/pdf"
              hidden
            >

            <button
              type="button"
              class="btn ghost"
              data-act="rf"
            >📎 ${st.receipt ? 'เปลี่ยนไฟล์' : 'เลือกไฟล์'}</button>

            ${st.receipt ? `
              <div class="receipt-file">
                <span>📄 ${esc(st.receipt.name)}</span>
                <button
                  type="button"
                  class="btn danger sm"
                  data-act="cr"
                >ลบไฟล์</button>
              </div>
              ${st.receipt.dataUrl ? `
                <img
                  class="receipt-preview"
                  src="${st.receipt.dataUrl}"
                  alt="ตัวอย่างใบเสร็จ"
                >
              ` : ''}
            ` : ''}
          </div>
        </div>
      </div>
    `,

    // ขั้นที่ 2: สมาชิกและรหัสบิล
    () => `
      <div class="grid g2e">
        <div class="card">
          <div class="row sp">
            <h2>ใครอยู่ในบิลนี้?</h2>
            <span class="tag">${st.sel.length} คน</span>
          </div>

          <p class="muted">เลือกเพื่อนที่ร่วมมื้ออาหาร</p>

          <div class="mgrid">
            ${all().map(n => `
              <button
                class="mc${st.sel.includes(n) ? ' on' : ''}"
                data-act="tm"
                data-n="${esc(n)}"
              >
                ${av(n, 44)}
                <b>${esc(n)}</b>
                <span class="muted">${n === me ? 'คุณ' : st.invited.some(u => u.username === n) ? 'มีบัญชี · จะได้รับคำเชิญ' : 'เพื่อน'}</span>
              </button>
            `).join('')}

            <button class="mc" data-act="af">
              <div
                class="ico"
                style="background:var(--g-l);color:var(--g-d);font-size:22px"
              >＋</div>
              <b>เพิ่มเพื่อน</b>
              <span class="muted">เพิ่มชื่อใหม่</span>
            </button>
          </div>
        </div>

        <div
          class="card"
          style="align-content:start;display:grid;gap:10px"
        >
          <span class="tag" style="width:fit-content">INVITE FRIENDS</span>
          <h2>ชวนเพื่อนเข้าร่วมบิล</h2>
          <p class="muted">
            หลังบันทึกบิล ให้เพื่อนกรอกรหัสนี้ที่หน้า "บิลของฉัน" → "เข้าร่วมบิล" หรือส่งลิงก์ให้
          </p>
          ${st.code
            ? `<span class="code" style="width:fit-content">${esc(st.code)}</span>
               <div class="row">
                 <button type="button" class="btn ghost sm" data-act="cc">คัดลอกรหัส</button>
                 <button type="button" class="btn ghost sm" data-act="cl">คัดลอกลิงก์</button>
               </div>`
            : '<p class="muted">ระบบจะสร้างรหัสให้เมื่อบันทึกบิล</p>'}

          <div class="field" style="margin-top:6px">
            <label for="invq">หรือเชิญเพื่อนที่มีบัญชีด้วยอีเมล</label>
            <input
              class="in"
              id="invq"
              type="email"
              placeholder="อีเมลเพื่อน เช่น name@email.com"
              autocomplete="off"
              value="${esc(st.q)}"
            >
          </div>
          <div id="invr">${resultsHtml()}</div>
          ${st.invited.length
            ? `<div class="chips">${st.invited.map(u => `
                <span class="chip on">${esc(u.username)}
                  <button type="button" data-act="uninv" data-uid="${u.id}" aria-label="ยกเลิกคำเชิญ"
                    style="border:0;background:none;cursor:pointer">✕</button>
                </span>`).join('')}</div>
               <p class="muted">คำเชิญจะถูกส่งเมื่อบันทึกบิล</p>`
            : ''}
        </div>
      </div>
    `,

    // ขั้นที่ 3: รายการอาหาร
    () => {
      const c = calc()

      return `
        <div class="head" style="margin-bottom:12px">
          <div>
            <h2>รายการอาหาร</h2>
            <p class="muted">
              ระบุว่าใครกินอะไร เพื่อคำนวณอย่างแฟร์
            </p>
          </div>
          <button class="btn" data-act="ai">＋ เพิ่มรายการอาหาร</button>
        </div>

        <div class="grid split">
          <div class="grid" style="align-content:start">
            ${st.items.length
              ? st.items.map(i => `
                <div class="card">
                  <div class="row sp">
                    <div class="row">
                      <div class="ico" style="background:#fff1e0">
                        ${i.icon}
                      </div>
                      <div>
                        <b>${esc(i.name)}</b>
                        <div class="muted">
                          จำนวน ${i.qty} · ${money(i.price)}/ชิ้น
                        </div>
                      </div>
                    </div>

                    <div class="row">
                      <b>${money(i.price * i.qty)}</b>
                      <button
                        class="btn ghost sm"
                        data-act="ei"
                        data-id="${i.id}"
                      >แก้ไข</button>
                      <button
                        class="btn danger sm"
                        data-act="di"
                        data-id="${i.id}"
                      >ลบ</button>
                    </div>
                  </div>

                  <div
                    class="chips"
                    style="margin-top:12px;align-items:center"
                  >
                    <span class="muted">กินโดย</span>
                    ${st.sel.map(n => `
                      <button
                        class="chip${i.eaters.includes(n) ? ' on' : ''}"
                        data-act="te"
                        data-id="${i.id}"
                        data-n="${esc(n)}"
                      >
                        ${esc(n)}${i.eaters.includes(n) ? ' ✓' : ''}
                      </button>
                    `).join('')}
                  </div>
                </div>
              `).join('')
              : `
                <div class="card">
                  ${empty(
                    '🍽️',
                    'ยังไม่มีรายการอาหาร',
                    'กด "เพิ่มรายการอาหาร" เพื่อเริ่มใส่เมนู'
                  )}
                </div>
              `}
          </div>

          <div class="card sum" style="align-self:start">
            <h2 style="margin-bottom:8px">สรุปยอด</h2>

            <div class="kv">
              <span class="muted">ยอดอาหาร</span>
              <b>${money(c.sub)}</b>
            </div>

            <div class="kv">
              <span class="muted">ส่วนลด (บาท)</span>
              <input
                class="in num"
                type="number"
                min="0"
                data-bind="disc"
                value="${st.disc}"
              >
            </div>

            <div class="kv">
              <span class="muted">Service charge (%)</span>
              <input
                class="in num"
                type="number"
                min="0"
                max="100"
                data-bind="svc"
                value="${st.svc}"
              >
            </div>

            <div class="kv">
              <span class="muted">VAT (%)</span>
              <input
                class="in num"
                type="number"
                min="0"
                max="100"
                data-bind="vat"
                value="${st.vat}"
              >
            </div>

            <div class="kv" style="font-size:12px">
              <span class="muted">Service / VAT</span>
              <span>${money(c.svc)} / ${money(c.vat)}</span>
            </div>

            <div class="kv">
              <b>ยอดรวมสุทธิ</b>
              <b style="color:var(--g-d)">${money(c.net)}</b>
            </div>
          </div>
        </div>
      `
    },

    // ขั้นที่ 4: วิธีหาร
    () => {
      const c = calc()
      const rec = st.items.some(
        i => i.eaters.length && i.eaters.length < st.sel.length
      ) ? 'item' : 'equal'

      const panel = {
        equal: `
          <p class="muted">
            ทุกคนจ่ายเท่ากัน คนละ ${money(c.net / st.sel.length)}
          </p>
        `,

        item: `
          <div class="eat-list">
            ${st.items.map(i => `
              <div class="eat-row">
                <div class="eat-info">
                  <span class="eat-ico">${i.icon}</span>
                  <div>
                    <b>${esc(i.name)}</b>
                    <div class="muted">
                      ${money(i.price * i.qty)}
                      ${i.eaters.length
                        ? ' · คนละ ' + money(i.price * i.qty / i.eaters.length)
                        : ''}
                    </div>
                  </div>
                </div>

                <div class="eat-av">
                  ${st.sel.map(n => {
                    const on = i.eaters.includes(n)

                    return `
                      <button
                        type="button"
                        class="eat-btn${on ? ' on' : ''}"
                        data-act="te"
                        data-id="${i.id}"
                        data-n="${esc(n)}"
                        title="${esc(n)}"
                      >
                        ${av(n, 34)}
                        ${on ? '<span class="eat-ck">✓</span>' : ''}
                        <small>${esc(n)}</small>
                      </button>
                    `
                  }).join('')}
                </div>

                ${i.eaters.length
                  ? ''
                  : '<div class="err eat-warn">ยังไม่ได้เลือกใคร (จะหารทุกคนเท่ากัน)</div>'}
              </div>
            `).join('')}
          </div>

          <p class="muted">
            กดที่อวาตาร์เพื่อเลือก/ยกเลิกว่าใครกินรายการนั้น
          </p>
        `,

        percentage: `
          ${st.sel.map(n => `
            <div class="kv">
              <span class="row">
                ${av(n, 30)}
                ${esc(n)}
              </span>
              <span>
                <input
                  class="in num"
                  type="number"
                  min="0"
                  max="100"
                  data-bind="pct"
                  data-n="${esc(n)}"
                  value="${+(st.pct[n] ?? 100 / st.sel.length).toFixed(2)}"
                > %
              </span>
            </div>
          `).join('')}

          <p class="${Math.abs(pctSum() - 100) > .01 ? 'err' : 'muted'}">
            รวม ${+pctSum().toFixed(2)}% (ต้องเท่ากับ 100%)
          </p>
        `,

        treat: `
          <div class="field">
            <label>ใครเป็นคนเลี้ยงทั้งโต๊ะ?</label>
            <select class="in" data-bind="treat">
              ${st.sel.map(n => `
                <option${st.treat === n ? ' selected' : ''}>
                  ${esc(n)}
                </option>
              `).join('')}
            </select>
          </div>
        `,

        custom: `
          ${st.sel.map(n => `
            <div class="kv">
              <span class="row">
                ${av(n, 30)}
                ${esc(n)}
              </span>
              <span>
                <input
                  class="in num"
                  type="number"
                  min="0"
                  data-bind="custom"
                  data-n="${esc(n)}"
                  value="${st.custom[n] ?? ''}"
                > ฿
              </span>
            </div>
          `).join('')}

          <p class="${Math.abs(c.sum - c.net) > .5 ? 'err' : 'muted'}">
            รวม ${money(c.sum)} จากยอดสุทธิ ${money(c.net)}
          </p>
        `
      }[st.method]

      return `
        <div class="mm">
          ${METHODS.map(([k, ic, t, e]) => `
            <button
              class="mth${st.method === k ? ' on' : ''}"
              data-act="sm"
              data-k="${k}"
            >
              ${k === rec ? '<span class="badge">แนะนำ</span>' : ''}
              <div class="ico" style="background:var(--bg)">${ic}</div>
              <b>${t}</b>
              <small>${e}</small>
            </button>
          `).join('')}
        </div>

        <div class="grid g2">
          <div class="card">
            <h2 style="margin-bottom:10px">
              ${st.method === 'item'
                ? 'ตรวจสอบการแบ่งรายการ'
                : 'ตรวจสอบการแบ่ง'}
            </h2>
            ${panel}
          </div>

          <div class="card">
            <h2>ยอดแบบเรียลไทม์</h2>
            <div class="list">
              ${st.sel.map(n => `
                <div>
                  ${av(n, 34)}
                  <b style="flex:1">${esc(n)}</b>
                  <b>${money(c.share[n])}</b>
                </div>
              `).join('')}
            </div>
          </div>
        </div>
      `
    },

    // ขั้นที่ 5: สรุปบิล
    () => {
      const c = calc()
      const mx = Math.max(...Object.values(c.share), 1)
      const lab = c.fair >= 85
        ? 'แฟร์มาก'
        : c.fair >= 70
          ? 'ค่อนข้างแฟร์'
          : 'ควรทบทวนวิธีหาร'

      const done = st.sel.filter(n => st.paid[n]).length

      return `
        <div
          class="grid split"
          style="grid-template-columns:1fr 300px"
        >
          <div class="card" style="padding:0">
            <div
              class="row sp"
              style="padding:20px;border-bottom:1px solid var(--line)"
            >
              <div class="row">
                <div class="ico" style="background:#fff1e0">🍽️</div>
                <div>
                  <h2>${esc(st.info.shop)}</h2>
                  <span class="muted">
                    ${fmtDate(st.info.date)} · ${st.sel.length} คน
                  </span>
                </div>
              </div>

              <div style="text-align:right">
                <span class="muted">ยอดรวมสุทธิ</span>
                <div
                  style="font-size:28px;font-weight:700;color:var(--g-d)"
                >${money(c.net)}</div>
              </div>
            </div>

            <div style="padding:8px 20px">
              ${st.sel.map(n => `
                <div style="padding:12px 0">
                  <div class="row sp">
                    <span class="row">
                      ${av(n, 32)}
                      <b>${esc(n)}</b>
                      ${n === me ? '<span class="pill paid">คุณ</span>' : ''}
                    </span>
                    <b>${money(c.share[n])}</b>
                  </div>

                  <div class="bar">
                    <i style="width:${c.share[n] / mx * 100}%"></i>
                  </div>
                </div>
              `).join('')}
            </div>

            <div
              class="grid g4"
              style="gap:0;border-top:1px solid var(--line);text-align:center"
            >
              ${[
                ['ยอดอาหาร', c.sub],
                ['ส่วนลด', -c.disc],
                ['Service ' + st.svc + '%', c.svc],
                ['VAT ' + st.vat + '%', c.vat]
              ].map(([l, v]) => `
                <div style="padding:12px">
                  <span class="muted">${l}</span>
                  <br>
                  <b>${v < 0 ? '-' : ''}${money(Math.abs(v))}</b>
                </div>
              `).join('')}
            </div>
          </div>

          <div class="grid" style="align-content:start">
            <div class="card">
              <span class="tag">FAIRNESS CHECK</span>

              <div class="row" style="margin-top:12px">
                <div class="ring" style="--p:${c.fair}">
                  <span>${c.fair}</span>
                </div>
                <div>
                  <b>${lab}</b>
                  <div class="muted">เทียบกับการแบ่งตามที่กินจริง</div>
                </div>
              </div>
            </div>

            <div class="card">
              <div class="row sp">
                <h2>สถานะการชำระ</h2>
                <span class="pill pending">รอ ${st.sel.length - done} คน</span>
              </div>

              <div class="bar">
                <i style="width:${done / st.sel.length * 100}%"></i>
              </div>

              ${st.sel.map(n => `
                <label class="row" style="margin-top:10px">
                  <input
                    type="checkbox"
                    data-act="tp"
                    data-n="${esc(n)}"
                    ${st.paid[n] ? ' checked' : ''}
                  >
                  ${esc(n)}
                  <span class="muted" style="margin-left:auto">
                    ${money(c.share[n])}
                  </span>
                </label>
              `).join('')}
            </div>
          </div>
        </div>
      `
    }
  ]

  function render() {
    const [t, d] = TITLES[st.step]

    $('#app').innerHTML = `
      <div class="head">
        <div>
          <span class="tag">SMART BILL CREATOR</span>
          <h1>${t}</h1>
          <p class="muted">${d}</p>
        </div>
      </div>

      <div class="card steps">
        ${STEPS.map((s, i) => `
          <div class="${i <= st.step ? 'on' : ''}">
            <i>${i < st.step ? '✓' : i + 1}</i>
            ${s}
          </div>
        `).join('')}
      </div>

      ${body[st.step]()}

      <div class="err" style="margin-top:12px;text-align:right">
        ${esc(st.msg)}
      </div>

      <div class="row sp" style="margin-top:6px">
        <button
          class="btn ghost"
          data-act="bk"
          ${st.step ? '' : ' disabled'}
        >← ย้อนกลับ</button>

        <button class="btn" data-act="nx">
          ${st.step === 4 ? (st.saving ? 'กำลังบันทึก...' : '✓ บันทึกบิล') : 'ถัดไป →'}
        </button>
      </div>
    `
  }

  function openItem(id) {
    const it = st.items.find(x => x.id === id) || {
      name: '',
      price: '',
      qty: 1,
      icon: ICONS[0],
      eaters: [...st.sel]
    }

    $('#fI').dataset.id = id || ''

    $('#fI').innerHTML = `
      <h2 style="margin-bottom:14px">
        ${id ? 'แก้ไขรายการ' : 'เพิ่มรายการอาหาร'}
      </h2>

      <div class="field">
        <label>ชื่อเมนู</label>
        <input
          class="in"
          name="name"
          required
          maxlength="40"
          value="${esc(it.name)}"
        >
      </div>

      <div class="grid g2e">
        <div class="field">
          <label>ราคาต่อชิ้น (บาท)</label>
          <input
            class="in"
            type="number"
            name="price"
            min="0.01"
            step="0.01"
            required
            value="${it.price}"
          >
        </div>

        <div class="field">
          <label>จำนวน</label>
          <input
            class="in"
            type="number"
            name="qty"
            min="1"
            step="1"
            required
            value="${it.qty}"
          >
        </div>
      </div>

      <div class="field">
        <label>ไอคอน</label>
        <div class="emoji-pick">
          ${ICONS.map(e => `
            <label>
              <input
                type="radio"
                name="ic"
                value="${e}"
                ${e === it.icon ? ' checked' : ''}
              >
              <span>${e}</span>
            </label>
          `).join('')}
        </div>
      </div>

      <div class="field">
        <label>ใครกินบ้าง</label>
        <div class="chips">
          ${st.sel.map(n => `
            <label class="chip">
              <input
                type="checkbox"
                name="e"
                value="${esc(n)}"
                ${it.eaters.includes(n) ? ' checked' : ''}
              >
              ${esc(n)}
            </label>
          `).join('')}
        </div>
      </div>

      <div class="row" style="justify-content:flex-end">
        <button
          type="button"
          class="btn ghost"
          data-act="cx"
        >ยกเลิก</button>

        <button class="btn">
          ${id ? 'บันทึก' : 'เพิ่มรายการ'}
        </button>
      </div>
    `

    $('#dI').showModal()
  }

  async function save() {
    if (st.saving) return
    st.saving = true
    render()

    const c = calc()
    const i = st.info
    const round2 = n => Math.round(n * 100) / 100

    // รายละเอียดการแบ่งที่หน้า "รายละเอียดบิล" ใช้แสดงผล
    const config = {
      version: 1,
      members: st.sel,
      shares: c.share,
      paid: st.paid,
      items: st.items,
      fairness: c.fair,
      servicePercent: st.svc,
      vatPercent: st.vat,
      pct: st.pct,
      treat: st.treat,
      custom: st.custom
    }

    try {
      const created = await api.post('/api/v1/bills', {
        restaurantName: i.shop.trim(),
        billDate: i.date || null,
        billTime: i.time || null,
        note: i.note.trim() || null,
        discount: round2(c.disc),
        serviceCharge: round2(c.svc),
        vat: round2(c.vat),
        splitMethod: st.method,
        splitConfigData: JSON.stringify(config),
        items: st.items.map(x => ({ name: x.name, quantity: x.qty, unitPrice: round2(x.price) })),
        joinCode: st.code || null,
        // ยอดที่แต่ละคนต้องจ่าย -> สร้างสถานะการจ่าย/สลิปรายคนในฐานข้อมูล
        payments: st.sel.map(n => ({
          name: n,
          amount: round2(Number(c.share[n]) || 0),
          paid: !!st.paid[n]
        }))
      })

      // ส่งคำเชิญให้เพื่อนที่มีบัญชี
      const sent = await Promise.allSettled(
        st.invited
          .filter(u => st.sel.includes(u.username))
          .map(u => api.post('/api/v1/bills/' + created.id + '/invitations', { inviteeId: u.id }))
      )
      if (sent.some(r => r.status === 'rejected')) {
        notify('บันทึกบิลแล้ว แต่ส่งคำเชิญบางคนไม่สำเร็จ เชิญใหม่ได้ที่หน้ารายละเอียดบิล')
      }

      // อัปโหลดสลิป/ใบเสร็จ (ถ้ามี)
      if (st.receipt?.file) {
        try {
          await api.upload('/api/v1/bills/' + created.id + '/slip', st.receipt.file)
        } catch {
          notify('บันทึกบิลแล้ว แต่อัปโหลดสลิปไม่สำเร็จ แนบใหม่ได้ที่หน้ารายละเอียดบิล')
        }
      }

      notify('สร้างบิล "' + i.shop.trim() + '" ยอด ' + money(c.net) + ' เรียบร้อยแล้ว')
      location = '/bills/detail?id=' + created.id
    } catch (err) {
      st.saving = false
      st.msg = 'บันทึกไม่สำเร็จ: ' + err.message
      render()
    }
  }

  document.addEventListener('click', async e => {
    const b = e.target.closest('[data-act]')
    if (!b) return

    const a = b.dataset.act
    const n = b.dataset.n
    const id = +b.dataset.id

    if (a === 'tp') {
      st.paid[n] = b.checked
      return render()
    }

    if (a === 'rf') {
      $('#receipt-file')?.click()
      return
    }

    if (a === 'cr') {
      st.receipt = null
      st.msg = ''
      return render()
    }

    if (a === 'tm') {
      if (n !== me) {
        st.sel = st.sel.includes(n)
          ? st.sel.filter(x => x !== n)
          : [...st.sel, n]

        if (st.sel.includes(n)) {
          st.items.forEach(i => i.eaters.push(n))
        }
      }
    } else if (a === 'af') {
      $('#dF').showModal()
      return
    } else if (a === 'cc' || a === 'cl') {
      navigator.clipboard?.writeText(a === 'cc' ? st.code : joinLink())
      b.textContent = 'คัดลอกแล้ว ✓'
      return
    } else if (a === 'inv') {
      const u = found
      if (u && !st.invited.some(x => x.id === u.id)) {
        st.invited.push(u)
        if (!st.sel.includes(u.username)) st.sel.push(u.username)
        st.items.forEach(i => {
          if (!i.eaters.includes(u.username)) i.eaters.push(u.username)
        })
        st.q = ''
        found = null
        lookupMsg = ''
      }
    } else if (a === 'uninv') {
      const u = st.invited.find(x => x.id === +b.dataset.uid)
      if (u) {
        st.invited = st.invited.filter(x => x !== u)
        st.sel = st.sel.filter(x => x !== u.username)
        st.items.forEach(i => {
          i.eaters = i.eaters.filter(x => x !== u.username)
        })
      }
    } else if (a === 'ai') {
      return openItem()
    } else if (a === 'ei') {
      return openItem(id)
    } else if (a === 'cx') {
      $('#dI').close()
      return
    } else if (a === 'di') {
      st.items = st.items.filter(i => i.id !== id)
    } else if (a === 'te') {
      const it = st.items.find(i => i.id === id)

      it.eaters = it.eaters.includes(n)
        ? it.eaters.filter(x => x !== n)
        : [...it.eaters, n]
    } else if (a === 'sm') {
      st.method = b.dataset.k
    } else if (a === 'bk') {
      st.step--
      st.msg = ''
    } else if (a === 'nx') {
      st.msg = check()

      if (!st.msg) {
        if (st.step === 4) return await save()
        st.step++
      }
    }

    render()
  })

  document.addEventListener('input', e => {
    if (e.target.id === 'invq') {
      st.q = e.target.value
      lookup(st.q)
      return
    }

    const k = e.target.dataset.in
    if (k) st.info[k] = e.target.value
  })

  document.addEventListener('change', e => {
    const t = e.target

    if (t.id === 'receipt-file') {
      readReceiptFile(t.files?.[0])
      t.value = ''
      return
    }

    const k = t.dataset.bind
    if (!k) return

    if (k === 'treat') {
      st.treat = t.value
    } else if (k === 'pct' || k === 'custom') {
      st[k][t.dataset.n] = +t.value
    } else {
      st[k] = Math.max(0, +t.value || 0)
    }

    render()
  })

  $('#fI').onsubmit = e => {
    e.preventDefault()

    const f = e.target
    const id = +f.dataset.id || 0

    const it = {
      id: id || nid(),
      name: f.name.value.trim(),
      price: +f.price.value,
      qty: Math.max(1, Math.floor(+f.qty.value)),
      icon: f.ic.value,
      eaters: $$('[name=e]:checked', f).map(x => x.value)
    }

    st.items = id
      ? st.items.map(x => x.id === id ? it : x)
      : [...st.items, it]

    $('#dI').close()
    render()
  }

  $('#fF').onsubmit = e => {
    e.preventDefault()

    const n = e.target.fname.value.trim()

    if (n && !all().includes(n)) {
      friends.push(n)
      S.set('friends', friends)
      st.sel.push(n)
      st.items.forEach(i => i.eaters.push(n))
    }

    e.target.reset()
    $('#dF').close()
    render()
  }

  $('#dF [data-close]').onclick = () => $('#dF').close()

  document.addEventListener('dragover', e => {
    const drop = e.target.closest('.receipt-drop')
    if (!drop) return

    e.preventDefault()
    drop.classList.add('drag')
  })

  document.addEventListener('dragleave', e => {
    const drop = e.target.closest('.receipt-drop')

    if (drop && !drop.contains(e.relatedTarget)) {
      drop.classList.remove('drag')
    }
  })

  document.addEventListener('drop', e => {
    const drop = e.target.closest('.receipt-drop')
    if (!drop) return

    e.preventDefault()
    drop.classList.remove('drag')
    readReceiptFile(e.dataTransfer?.files?.[0])
  })

  render()
})().catch(err => console.error(err))

// โฟลว์สร้างบิล 5 ขั้น — ข้อมูลเก็บชั่วคราวใน localStorage (ภายหลังเปลี่ยนเป็นเรียก API)
(() => {
  const me = getProfile().name, COL = ['#bfeadd', '#ffd9a8', '#d9d0ff', '#c5dcff', '#ffc9c0', '#fff0a8']
  const ICONS = ['🍽️', '🍕', '🍗', '🥤', '🍟', '🍜', '🍣', '🥩', '🍰']
  const STEPS = ['ข้อมูลบิล', 'สมาชิก', 'รายการอาหาร', 'วิธีหาร', 'สรุป']
  const TITLES = [['สร้างบิลใหม่', 'กรอกข้อมูลบิลเบื้องต้น'], ['เพิ่มสมาชิก', 'เลือกเพื่อนที่ร่วมมื้ออาหาร'], ['เพิ่มรายการอาหาร', 'ระบุว่าใครกินอะไร เพื่อคำนวณอย่างแฟร์'], ['เลือกวิธีหาร', 'เลือกวิธีที่เหมาะกับมื้อนี้ที่สุด'], ['สรุปและชำระ', 'ตรวจสอบยอดก่อนบันทึกบิล']]
  const METHODS = [['equal', '⚖️', 'หารเท่ากัน', 'Equal Split'], ['item', '🍕', 'ตามรายการที่กิน', 'Item Based'], ['percentage', '％', 'ตามสัดส่วน', 'Percentage'], ['treat', '🎁', 'เลี้ยงเพื่อน', 'Treat Someone'], ['custom', '✏️', 'กำหนดเอง', 'Custom Split']]
  let friends = S.get('friends', [])
  const st = { step: 0, info: { shop: '', date: new Date().toISOString().slice(0, 10), time: '', note: '' }, sel: [me], items: [], disc: 0, svc: 10, vat: 7, method: 'equal', pct: {}, treat: me, custom: {}, paid: { [me]: true }, code: 'SM-' + Math.floor(1000 + Math.random() * 9000), msg: '' }
  const all = () => [me, ...friends], color = n => COL[all().indexOf(n) % COL.length]
  const av = (n, s = 40) => `<div class="av" style="background:${color(n)};width:${s}px;height:${s}px">${esc((n[0] || '?').toUpperCase())}</div>`
  const money = n => '฿' + (+n).toLocaleString('th-TH', { maximumFractionDigits: 2 })
  const nid = () => Math.max(0, ...st.items.map(i => i.id)) + 1

  function calc() {
    const m = st.sel, n = m.length, zero = () => Object.fromEntries(m.map(x => [x, 0]))
    const sub = st.items.reduce((a, i) => a + i.price * i.qty, 0), disc = Math.min(+st.disc || 0, sub)
    const svc = sub * st.svc / 100, vat = sub * st.vat / 100, net = sub - disc + svc + vat
    const byItem = () => { const f = zero(); st.items.forEach(i => { const e = i.eaters.filter(x => m.includes(x)), t = e.length ? e : m; t.forEach(x => f[x] += i.price * i.qty / t.length) }); return f }
    let f = null
    if (st.method === 'item') f = byItem()
    else if (st.method === 'percentage') f = Object.fromEntries(m.map(x => [x, sub * (st.pct[x] ?? 100 / n) / 100]))
    else if (st.method === 'treat') f = Object.fromEntries(m.map(x => [x, x === st.treat ? sub : 0]))
    else if (st.method === 'equal') f = Object.fromEntries(m.map(x => [x, sub / n]))
    const share = Object.fromEntries(m.map(x => [x, f ? (sub ? f[x] / sub * net : 0) : (+st.custom[x] || 0)]))
    const ib = byItem(), dev = net ? m.reduce((a, x) => a + Math.abs(share[x] - (sub ? ib[x] / sub * net : 0)), 0) / (2 * net) : 0
    return { sub, disc, svc, vat, net, share, fair: Math.max(0, Math.round(100 - dev * 100)), sum: Object.values(share).reduce((a, b) => a + b, 0) }
  }
  const pctSum = () => st.sel.reduce((a, x) => a + (st.pct[x] ?? 100 / st.sel.length), 0)
  function check() {
    if (st.step === 0 && !st.info.shop.trim()) return 'กรุณากรอกชื่อร้านอาหาร'
    if (st.step === 2 && !st.items.length) return 'เพิ่มรายการอาหารอย่างน้อย 1 รายการ'
    if (st.step === 3) {
      if (st.method === 'percentage' && Math.abs(pctSum() - 100) > .01) return 'สัดส่วนรวมต้องเท่ากับ 100%'
      if (st.method === 'custom' && Math.abs(calc().sum - calc().net) > .5) return 'ยอดที่กำหนดต้องรวมเท่ากับยอดสุทธิ'
    }
    return ''
  }

  const body = [
    () => `<div class="grid g2e"><div class="card"><h2 style="margin-bottom:14px">ข้อมูลบิล</h2>
      <div class="field"><label>ชื่อร้านอาหาร</label><input class="in" data-in="shop" maxlength="60" placeholder="เช่น MK Restaurant" value="${esc(st.info.shop)}"></div>
      <div class="grid g2e"><div class="field"><label>วันที่</label><input type="date" class="in" data-in="date" value="${st.info.date}"></div><div class="field"><label>เวลา</label><input type="time" class="in" data-in="time" value="${st.info.time}"></div></div>
      <div class="field"><label>โน้ต (ไม่บังคับ)</label><textarea class="in" data-in="note" rows="3" placeholder="เช่น มื้อฉลองสอบเสร็จ...">${esc(st.info.note)}</textarea></div></div>
      <div class="card"><div class="row sp"><h2>สแกนใบเสร็จ</h2><span class="tag">AI POWERED</span></div><div class="drop" style="margin-top:14px"><div class="ico" style="background:#fff">🧾</div><b>ลากใบเสร็จมาวางที่นี่</b><span class="muted">รองรับ JPG, PNG, PDF (ยังไม่เปิดใช้งาน)</span></div></div></div>`,
    () => `<div class="grid g2e"><div class="card"><div class="row sp"><h2>ใครอยู่ในบิลนี้?</h2><span class="tag">${st.sel.length} คน</span></div><p class="muted">เลือกเพื่อนที่ร่วมมื้ออาหาร</p>
      <div class="mgrid">${all().map(n => `<button class="mc${st.sel.includes(n) ? ' on' : ''}" data-act="tm" data-n="${esc(n)}">${av(n, 44)}<b>${esc(n)}</b><span class="muted">${n === me ? 'คุณ' : 'เพื่อน'}</span></button>`).join('')}
      <button class="mc" data-act="af"><div class="ico" style="background:var(--g-l);color:var(--g-d);font-size:22px">＋</div><b>เพิ่มเพื่อน</b><span class="muted">เชิญใหม่</span></button></div></div>
      <div class="card" style="align-content:center;display:grid;gap:10px"><span class="tag" style="width:fit-content">LIVE BILL</span><h2>ชวนเพื่อนเข้าร่วมบิล</h2><p class="muted">ส่งรหัสบิลนี้ให้เพื่อนเพื่อเลือกเมนูที่กิน (ใช้งานได้เมื่อเชื่อมระบบ)</p><span class="code" style="width:fit-content">${st.code}</span><button class="btn ghost" style="width:fit-content" data-act="cc">คัดลอกรหัส</button></div></div>`,
    () => {
      const c = calc(); return `<div class="head" style="margin-bottom:12px"><div><h2>รายการอาหาร</h2><p class="muted">ระบุว่าใครกินอะไร เพื่อคำนวณอย่างแฟร์</p></div><button class="btn" data-act="ai">＋ เพิ่มรายการอาหาร</button></div>
      <div class="grid split"><div class="grid" style="align-content:start">${st.items.length ? st.items.map(i => `<div class="card"><div class="row sp"><div class="row"><div class="ico" style="background:#fff1e0">${i.icon}</div><div><b>${esc(i.name)}</b><div class="muted">จำนวน ${i.qty} · ${money(i.price)}/ชิ้น</div></div></div><div class="row"><b>${money(i.price * i.qty)}</b><button class="btn ghost sm" data-act="ei" data-id="${i.id}">แก้ไข</button><button class="btn danger sm" data-act="di" data-id="${i.id}">ลบ</button></div></div>
        <div class="chips" style="margin-top:12px;align-items:center"><span class="muted">กินโดย</span>${st.sel.map(n => `<button class="chip${i.eaters.includes(n) ? ' on' : ''}" data-act="te" data-id="${i.id}" data-n="${esc(n)}">${esc(n)}${i.eaters.includes(n) ? ' ✓' : ''}</button>`).join('')}</div></div>`).join('') : `<div class="card">${empty('🍽️', 'ยังไม่มีรายการอาหาร', 'กด "เพิ่มรายการอาหาร" เพื่อเริ่มใส่เมนู')}</div>`}</div>
      <div class="card sum" style="align-self:start"><h2 style="margin-bottom:8px">สรุปยอด</h2><div class="kv"><span class="muted">ยอดอาหาร</span><b>${money(c.sub)}</b></div>
        <div class="kv"><span class="muted">ส่วนลด (บาท)</span><input class="in num" type="number" min="0" data-bind="disc" value="${st.disc}"></div>
        <div class="kv"><span class="muted">Service charge (%)</span><input class="in num" type="number" min="0" max="100" data-bind="svc" value="${st.svc}"></div>
        <div class="kv"><span class="muted">VAT (%)</span><input class="in num" type="number" min="0" max="100" data-bind="vat" value="${st.vat}"></div>
        <div class="kv" style="font-size:12px"><span class="muted">Service / VAT</span><span>${money(c.svc)} / ${money(c.vat)}</span></div>
        <div class="kv"><b>ยอดรวมสุทธิ</b><b style="color:var(--g-d)">${money(c.net)}</b></div></div></div>`
    },
    () => {
      const c = calc(), rec = st.items.some(i => i.eaters.length && i.eaters.length < st.sel.length) ? 'item' : 'equal'
      const panel = {
        equal: `<p class="muted">ทุกคนจ่ายเท่ากัน คนละ ${money(c.net / st.sel.length)}</p>`,
        item: `<div class="list">${st.items.map(i => `<div><span>${i.icon}</span><div style="flex:1"><b>${esc(i.name)}</b><div class="muted">${money(i.price * i.qty)} · ${esc((i.eaters.length ? i.eaters : st.sel).join(', '))}</div></div></div>`).join('')}</div><p class="muted">ปรับว่าใครกินอะไรได้ที่ขั้น "รายการอาหาร"</p>`,
        percentage: `${st.sel.map(n => `<div class="kv"><span class="row">${av(n, 30)}${esc(n)}</span><span><input class="in num" type="number" min="0" max="100" data-bind="pct" data-n="${esc(n)}" value="${+(st.pct[n] ?? 100 / st.sel.length).toFixed(2)}"> %</span></div>`).join('')}<p class="${Math.abs(pctSum() - 100) > .01 ? 'err' : 'muted'}">รวม ${+pctSum().toFixed(2)}% (ต้องเท่ากับ 100%)</p>`,
        treat: `<div class="field"><label>ใครเป็นคนเลี้ยงทั้งโต๊ะ?</label><select class="in" data-bind="treat">${st.sel.map(n => `<option${st.treat === n ? ' selected' : ''}>${esc(n)}</option>`).join('')}</select></div>`,
        custom: `${st.sel.map(n => `<div class="kv"><span class="row">${av(n, 30)}${esc(n)}</span><span><input class="in num" type="number" min="0" data-bind="custom" data-n="${esc(n)}" value="${st.custom[n] ?? ''}"> ฿</span></div>`).join('')}<p class="${Math.abs(c.sum - c.net) > .5 ? 'err' : 'muted'}">รวม ${money(c.sum)} จากยอดสุทธิ ${money(c.net)}</p>`
      }[st.method]
      return `<div class="mm">${METHODS.map(([k, ic, t, e]) => `<button class="mth${st.method === k ? ' on' : ''}" data-act="sm" data-k="${k}">${k === rec ? '<span class="badge">แนะนำ</span>' : ''}<div class="ico" style="background:var(--bg)">${ic}</div><b>${t}</b><small>${e}</small></button>`).join('')}</div>
      <div class="grid g2"><div class="card"><h2 style="margin-bottom:10px">ตรวจสอบการแบ่ง</h2>${panel}</div>
      <div class="card"><h2>ยอดแบบเรียลไทม์</h2><div class="list">${st.sel.map(n => `<div>${av(n, 34)}<b style="flex:1">${esc(n)}</b><b>${money(c.share[n])}</b></div>`).join('')}</div></div></div>`
    },
    () => {
      const c = calc(), mx = Math.max(...Object.values(c.share), 1), lab = c.fair >= 85 ? 'แฟร์มาก' : c.fair >= 70 ? 'ค่อนข้างแฟร์' : 'ควรทบทวนวิธีหาร', done = st.sel.filter(n => st.paid[n]).length
      return `<div class="grid split" style="grid-template-columns:1fr 300px"><div class="card" style="padding:0"><div class="row sp" style="padding:20px;border-bottom:1px solid var(--line)"><div class="row"><div class="ico" style="background:#fff1e0">🍽️</div><div><h2>${esc(st.info.shop)}</h2><span class="muted">${fmtDate(st.info.date)} · ${st.sel.length} คน</span></div></div><div style="text-align:right"><span class="muted">ยอดรวมสุทธิ</span><div style="font-size:28px;font-weight:700;color:var(--g-d)">${money(c.net)}</div></div></div>
        <div style="padding:8px 20px">${st.sel.map(n => `<div style="padding:12px 0"><div class="row sp"><span class="row">${av(n, 32)}<b>${esc(n)}</b>${n === me ? '<span class="pill paid">คุณ</span>' : ''}</span><b>${money(c.share[n])}</b></div><div class="bar"><i style="width:${c.share[n] / mx * 100}%"></i></div></div>`).join('')}</div>
        <div class="grid g4" style="gap:0;border-top:1px solid var(--line);text-align:center">${[['ยอดอาหาร', c.sub], ['ส่วนลด', -c.disc], ['Service ' + st.svc + '%', c.svc], ['VAT ' + st.vat + '%', c.vat]].map(([l, v]) => `<div style="padding:12px"><span class="muted">${l}</span><br><b>${v < 0 ? '-' : ''}${money(Math.abs(v))}</b></div>`).join('')}</div></div>
      <div class="grid" style="align-content:start"><div class="card"><span class="tag">FAIRNESS CHECK</span><div class="row" style="margin-top:12px"><div class="ring" style="--p:${c.fair}"><span>${c.fair}</span></div><div><b>${lab}</b><div class="muted">เทียบกับการแบ่งตามที่กินจริง</div></div></div></div>
        <div class="card"><div class="row sp"><h2>สถานะการชำระ</h2><span class="pill pending">รอ ${st.sel.length - done} คน</span></div><div class="bar"><i style="width:${done / st.sel.length * 100}%"></i></div>
          ${st.sel.map(n => `<label class="row" style="margin-top:10px"><input type="checkbox" data-act="tp" data-n="${esc(n)}"${st.paid[n] ? ' checked' : ''}>${esc(n)}<span class="muted" style="margin-left:auto">${money(c.share[n])}</span></label>`).join('')}</div></div></div>`
    }
  ]

  function render() {
    const [t, d] = TITLES[st.step]
    $('#app').innerHTML = `<div class="head"><div><span class="tag">SMART BILL CREATOR</span><h1>${t}</h1><p class="muted">${d}</p></div></div>
      <div class="card steps">${STEPS.map((s, i) => `<div class="${i <= st.step ? 'on' : ''}"><i>${i < st.step ? '✓' : i + 1}</i>${s}</div>`).join('')}</div>${body[st.step]()}
      <div class="err" style="margin-top:12px;text-align:right">${esc(st.msg)}</div><div class="row sp" style="margin-top:6px"><button class="btn ghost" data-act="bk"${st.step ? '' : ' disabled'}>← ย้อนกลับ</button><button class="btn" data-act="nx">${st.step === 4 ? '✓ บันทึกบิล' : 'ถัดไป →'}</button></div>`
  }
  function openItem(id) {
    const it = st.items.find(x => x.id === id) || { name: '', price: '', qty: 1, icon: ICONS[0], eaters: [...st.sel] }
    $('#fI').dataset.id = id || ''
    $('#fI').innerHTML = `<h2 style="margin-bottom:14px">${id ? 'แก้ไขรายการ' : 'เพิ่มรายการอาหาร'}</h2><div class="field"><label>ชื่อเมนู</label><input class="in" name="name" required maxlength="40" value="${esc(it.name)}"></div>
      <div class="grid g2e"><div class="field"><label>ราคาต่อชิ้น (บาท)</label><input class="in" type="number" name="price" min="0.01" step="0.01" required value="${it.price}"></div><div class="field"><label>จำนวน</label><input class="in" type="number" name="qty" min="1" step="1" required value="${it.qty}"></div></div>
      <div class="field"><label>ไอคอน</label><div class="emoji-pick">${ICONS.map(e => `<label><input type="radio" name="ic" value="${e}"${e === it.icon ? ' checked' : ''}><span>${e}</span></label>`).join('')}</div></div>
      <div class="field"><label>ใครกินบ้าง</label><div class="chips">${st.sel.map(n => `<label class="chip"><input type="checkbox" name="e" value="${esc(n)}"${it.eaters.includes(n) ? ' checked' : ''}> ${esc(n)}</label>`).join('')}</div></div>
      <div class="row" style="justify-content:flex-end"><button type="button" class="btn ghost" data-act="cx">ยกเลิก</button><button class="btn">${id ? 'บันทึก' : 'เพิ่มรายการ'}</button></div>`
    $('#dI').showModal()
  }
  function save() {
    const c = calc(), l = S.get('bills', []), id = newId(l), i = st.info
    l.push({ id, shop: i.shop.trim(), date: i.date, time: i.time, note: i.note.trim(), total: Math.round(c.net * 100) / 100, people: st.sel.length, status: st.sel.every(n => st.paid[n]) ? 'paid' : 'pending', method: st.method, items: st.items, members: st.sel, shares: c.share, sub: c.sub, disc: c.disc, svc: c.svc, vat: c.vat, fair: c.fair, paid: st.paid, code: st.code })
    S.set('bills', l); notify('สร้างบิล "' + i.shop.trim() + '" ยอด ' + money(c.net) + ' เรียบร้อยแล้ว'); location = 'bill-detail.html?id=' + id
  }
  document.addEventListener('click', e => {
    const b = e.target.closest('[data-act]'); if (!b) return
    const a = b.dataset.act, n = b.dataset.n, id = +b.dataset.id
    if (a === 'tp') { st.paid[n] = b.checked; return render() }
    if (a === 'tm') { if (n !== me) { st.sel = st.sel.includes(n) ? st.sel.filter(x => x !== n) : [...st.sel, n]; if (st.sel.includes(n)) st.items.forEach(i => i.eaters.push(n)) } }
    else if (a === 'af') { $('#dF').showModal(); return }
    else if (a === 'cc') navigator.clipboard?.writeText(st.code)
    else if (a === 'ai') return openItem()
    else if (a === 'ei') return openItem(id)
    else if (a === 'cx') { $('#dI').close(); return }
    else if (a === 'di') st.items = st.items.filter(i => i.id !== id)
    else if (a === 'te') { const it = st.items.find(i => i.id === id); it.eaters = it.eaters.includes(n) ? it.eaters.filter(x => x !== n) : [...it.eaters, n] }
    else if (a === 'sm') st.method = b.dataset.k
    else if (a === 'bk') { st.step--; st.msg = '' }
    else if (a === 'nx') { st.msg = check(); if (!st.msg) { if (st.step === 4) return save(); st.step++ } }
    render()
  })
  document.addEventListener('input', e => { const k = e.target.dataset.in; if (k) st.info[k] = e.target.value })
  document.addEventListener('change', e => {
    const t = e.target, k = t.dataset.bind; if (!k) return
    if (k === 'treat') st.treat = t.value
    else if (k === 'pct' || k === 'custom') st[k][t.dataset.n] = +t.value
    else st[k] = Math.max(0, +t.value || 0)
    render()
  })
  $('#fI').onsubmit = e => {
    e.preventDefault(); const f = e.target, id = +f.dataset.id || 0
    const it = { id: id || nid(), name: f.name.value.trim(), price: +f.price.value, qty: Math.max(1, Math.floor(+f.qty.value)), icon: f.ic.value, eaters: $$('[name=e]:checked', f).map(x => x.value) }
    st.items = id ? st.items.map(x => x.id === id ? it : x) : [...st.items, it]; $('#dI').close(); render()
  }
  $('#fF').onsubmit = e => {
    e.preventDefault(); const n = e.target.fname.value.trim()
    if (n && !all().includes(n)) { friends.push(n); S.set('friends', friends); st.sel.push(n); st.items.forEach(i => i.eaters.push(n)) }
    e.target.reset(); $('#dF').close(); render()
  }
  $('#dF [data-close]').onclick = () => $('#dF').close()
  render()
})()

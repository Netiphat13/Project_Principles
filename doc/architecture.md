# Architecture

## Layered Architecture

- Presentation: `controller/api`, `controller/web`
- Service: `service`, `service/impl`
- Data Access: `repository`
- Domain: `model`
- API Contract: `dto/request`, `dto/response`
- Mapping: `mapper`
- Cross-cutting: `config`, `exception`

## Authentication

- ล็อกอินผ่านหน้าเว็บ `POST /login` แล้วเก็บ `userId` ไว้ใน session (รหัสผ่านเก็บแบบ BCrypt)
- หน้าเว็บที่ยังไม่ล็อกอินจะถูก redirect ไป `/login`
- REST API ทุกตัวต้องล็อกอินก่อน (ไม่งั้นได้ 401) ยกเว้น `POST /api/v1/users` (สมัครสมาชิก)
- เจ้าของข้อมูลมาจาก session เสมอ ไม่รับ `createdById` จาก client

## Main REST resources

- `POST /api/v1/users` — สมัครสมาชิก
- `GET /api/v1/users/me` — ผู้ใช้ที่ล็อกอิน + เบอร์โทร/แนะนำตัว + การตั้งค่าแจ้งเตือน
- `PUT /api/v1/users/me/profile` — แก้ชื่อ เบอร์โทร แนะนำตัว (อีเมลใช้ล็อกอิน แก้ไม่ได้)
- `PATCH /api/v1/users/me/settings/notification` — เปิด/ปิดการแจ้งเตือน
- `GET /api/v1/users/lookup?email=` — หาเพื่อนจากอีเมลแบบตรงตัว เพื่อเชิญเข้าบิล
- `GET /api/v1/users/{id}`, `PUT/DELETE /api/v1/users/{id}` — เฉพาะบัญชีตัวเอง (403 ถ้าไม่ใช่)
- `GET/POST /api/v1/bills` — บิลที่ผู้ใช้สร้าง + บิลที่เข้าร่วม (`?page=0&size=10&sort=createdAt,desc`)
- `GET /api/v1/bills/{id}`, `GET /api/v1/bills/{id}/members` — เจ้าของหรือสมาชิกของบิล
- `PUT/DELETE /api/v1/bills/{id}`, `PATCH /api/v1/bills/{id}/status` — เจ้าของบิลเท่านั้น
- `GET /api/v1/bills/join-code/next`, `POST /api/v1/bills/join` — รหัสเข้าร่วมบิล (เช่น `SM-AB12CD`)
- `POST/GET /api/v1/bills/{id}/slip` — อัปโหลด (เจ้าของ) / ดู (สมาชิก) สลิปหรือใบเสร็จ เก็บไฟล์ที่ `uploads/slips`
- `POST/GET /api/v1/bills/{id}/invitations` — เชิญผู้ใช้เข้าบิล / คำเชิญที่รอตอบ (สมาชิกของบิล)
- `GET /api/v1/invitations`, `PUT /api/v1/invitations/{id}/accept|reject` — คำเชิญที่ฉันได้รับ

หน้าเว็บ `/join?code=SM-XXXXXX` เป็นลิงก์เชิญ ถ้ายังไม่ล็อกอินจะจำรหัสไว้แล้วพาไปหน้า login ก่อน

Swagger UI: `/swagger-ui.html`

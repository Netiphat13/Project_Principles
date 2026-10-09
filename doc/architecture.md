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
- `GET /api/v1/users/me`, `GET /api/v1/users/{id}`
- `PUT/DELETE /api/v1/users/{id}` — เฉพาะบัญชีตัวเอง (403 ถ้าไม่ใช่)
- `GET/POST /api/v1/groups` — กลุ่มของผู้ใช้ที่ล็อกอิน
- `GET /api/v1/groups/{id}` — เจ้าของหรือสมาชิก; `PUT/DELETE` — เจ้าของเท่านั้น
- `GET/POST /api/v1/bills` — บิลของผู้ใช้ที่ล็อกอิน (`?page=0&size=10&sort=createdAt,desc`)
- `GET/PUT/DELETE /api/v1/bills/{id}` — เจ้าของบิลเท่านั้น

Swagger UI: `/swagger-ui.html`

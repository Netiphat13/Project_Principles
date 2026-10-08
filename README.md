# SplitMate — HTML + Spring Boot Backend

โปรเจกต์นี้รวม UI จาก `splitbill (5)` เข้ากับ Backend/REST API จาก `principle pak` และเปลี่ยนหน้าหลักจากข้อมูล mock ใน `localStorage` ให้เรียก REST API + PostgreSQL จริง

## สิ่งที่เชื่อมแล้ว
- สมัครสมาชิก / เข้าสู่ระบบด้วย Spring MVC + Session
- Dashboard, Bill History, Bill Detail ใช้ข้อมูลจาก `/api/v1/bills`
- สร้างบิล 5 ขั้นและบันทึกลง PostgreSQL พร้อมรายการอาหาร วิธีหาร และรายละเอียดการแบ่งใน `split_configs`
- เปลี่ยนสถานะบิล / ลบบิลผ่าน REST API
- สร้างกลุ่มและเก็บ creator เป็นสมาชิก OWNER
- รายชื่อสมาชิกกลุ่มผ่าน REST API
- โปรไฟล์ ชื่อ อีเมล เบอร์โทร และ bio ใช้ข้อมูล Backend
- การตั้งค่าแจ้งเตือนบันทึกใน `user_settings`

## Database
ใช้ PostgreSQL ตาม `docker-compose.yml`

```bash
docker compose up -d
```

ค่าเริ่มต้นคือ DB `splitmate`, user `splitmate`, password `splitmate`, port `5432`

## Run

```bash
./mvnw spring-boot:run
```

เปิด `http://localhost:8080`

Swagger: `http://localhost:8080/swagger-ui.html`

## โครงสร้างหน้าเว็บ

โปรเจกต์ใช้ HTML อยู่คนละบทบาทอย่างชัดเจน เพื่อไม่ให้มีไฟล์ชื่อซ้ำและไม่เกิดปัญหาแก้ไฟล์ผิดชุด:

- `src/main/resources/templates/auth/` — มีเฉพาะ `login.html` และ `register.html` ซึ่งต้องผ่าน Thymeleaf เพื่อแสดง flash message จาก Spring MVC
- `src/main/resources/static/` — หน้าแอปทั้งหมด เช่น Dashboard, Bills, Bill Detail, Create Bill, Groups, Stats และ Profile ใช้เป็น Static HTML และเรียก REST API ผ่าน JavaScript
- URL แบบ `/home`, `/bills`, `/stats`, `/profile` ฯลฯ เป็น route alias ที่ตรวจ session ก่อน แล้ว redirect ไปยัง Static HTML ที่ตรงกัน

ดังนั้น **ห้ามสร้าง HTML หน้าแอปซ้ำไว้ใน `templates/`** ให้แก้หน้าใช้งานจริงใน `static/` เท่านั้น

## หมายเหตุ
การคำนวณยอดแบ่งเงินของหน้าสร้างบิลยังทำใน Browser เพื่อให้เห็นยอดแบบเรียลไทม์ จากนั้นส่งผลลัพธ์และรายละเอียดการแบ่งไปเก็บที่ Backend ใน `split_configs.config_data` เพื่อให้โหลดกลับมาแสดงในหน้ารายละเอียดบิลได้

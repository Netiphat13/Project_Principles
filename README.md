# Project ระบบแชร์ค่าอาหาร

SplitMate เป็นเว็บแอปพลิเคชันสำหรับจัดการบิลและแบ่งค่าใช้จ่ายระหว่างเพื่อนหรือสมาชิกในกลุ่ม  
ผู้ใช้สามารถสมัครสมาชิก เข้าสู่ระบบ สร้างบิล เพิ่มรายการอาหารและราคา แล้วจัดการสมาชิกที่ร่วมบิลได้  
ระบบรองรับการเชิญเพื่อนเข้าร่วมบิลผ่านบัญชีผู้ใช้หรือรหัสเข้าร่วมบิล รวมถึงการจัดการสถานะการชำระเงิน  
ผู้ใช้สามารถแนบสลิป/ใบเสร็จ ดูสถิติ และจัดการข้อมูลโปรไฟล์ของตนเองได้  
พัฒนาด้วย Java และ Spring Boot ใช้ Thymeleaf สำหรับหน้าเว็บ PostgreSQL สำหรับฐานข้อมูล และ REST API สำหรับการทำงานหลัก

**รายวิชา:** CP353002 Principles of Software Design and Development

**Deployment URL:** [https://splitmate-x3db.onrender.com/login](https://splitmate-x3db.onrender.com/login) Swagger UI : https://splitmate-x3db.onrender.com/swagger-ui/index.html

---

## สมาชิกกลุ่ม

| ลำดับ | รหัสนักศึกษา | ชื่อ-นามสกุล | Emaill | Branch | หน้าที่รับผิดชอบ |
| :---: | :---: | :--- | :--- | :---: | :--- |
| 1 | 673380413-9 | ปิยพันธ์ แก้วเก็บคำ | piyapan.k@kkumail.com | piyapan_6733804139_04 | Backend 1 Authentication (JWT/Spring Security), User และ Profile, Bill / BillItem / BillMember / Assignment (CRUD + Pagination), Payment + State Pattern, Notification + Observer Pattern, Global Exception Handler และ Swagger API Documentation |
| 2 | 673380416-3 | พัทธดนย์ คำนัน | pattadon.kh@kkumail.com | pattadon_6733804163_04 | Database + Deployer Flyway Migration, ER Diagram, Data Dictionary, Entity และ Repository ทั้งระบบ, Group Module (CRUD + QR/Invite Code), Statistics (JPQL Aggregate), Dockerfile, Docker Compose, Deploy ระบบ และ GitHub Actions (CI/CD) |
| 3 | 673380426-0 | วรปรัชญ์ พิมพ์อุบล | woraprat.p@kkumail.com | woraprat_6733804260_04 | Backend 2 + Tester Split Strategy ทั้ง 5 รูปแบบ, Fairness Score, What-if Simulator, Smart Settlement, การคำนวณ Service Charge / VAT / ส่วนลด, Unit Test และ Integration Test, Test Report |
| 4 | 673380434-1 | เนติภัทร ภูครองเพชร | netiphat.p@kkumail.com | netiphat_6733804341_04 | Frontend พัฒนา UI จาก Figma ด้วย React, เชื่อมต่อ API ทุกหน้า, พัฒนา Create Bill Flow ทั้ง 5 ขั้นตอน, รองรับ Responsive Design |

## Tech Stack

| ส่วน | เทคโนโลยี |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot |
| Build Tool | Maven |
| Frontend | Thymeleaf, HTML, CSS, JavaScript |
| Database | PostgreSQL |
| Database Migration | Flyway |
| ORM / Data Access | Spring Data JPA / Hibernate |
| API Documentation | Springdoc OpenAPI / Swagger UI |
| Password Hashing | BCrypt ผ่าน Spring Security Crypto |
| Container | Docker, Docker Compose |
| Hosting | Render |
| Cloud Database | PostgreSQL บน Neon (ตามการตั้งค่าที่อยู่ในไฟล์โปรเจกต์) |
| Testing | JUnit 5, Spring Boot Test |

## System Architecture

ระบบใช้ **Layered Architecture** แยกความรับผิดชอบของแต่ละส่วน เพื่อให้ง่ายต่อการดูแลและพัฒนาต่อ

```text
User / Browser
      |
      v
Presentation Layer
  +-- controller/web   : หน้าเว็บ Thymeleaf
  +-- controller/api   : REST API
      |
      v
Service Layer
  +-- service          : Business Logic
  +-- service/impl     : Implementation
      |
      v
Data Access Layer
  +-- repository       : Spring Data JPA
      |
      v
Domain Layer
  +-- model            : Entity และ Enum
      |
      v
PostgreSQL Database

Supporting Components:
  +-- dto/request, dto/response : รูปแบบข้อมูลรับเข้า/ส่งออก
  +-- mapper                    : แปลง Entity และ DTO
  +-- config                    : การตั้งค่าระบบ
  +-- exception                 : การจัดการข้อผิดพลาด
  +-- db/migration               : Flyway SQL migrations
```

### ภาพรวมการทำงาน

1. ผู้ใช้สมัครสมาชิกและเข้าสู่ระบบ
2. ผู้ใช้สร้างบิล เพิ่มรายการและกำหนดสมาชิกที่ร่วมค่าใช้จ่าย
3. สมาชิกเข้าร่วมผ่านรหัสเข้าร่วมบิลหรือคำเชิญ
4. ระบบบันทึกข้อมูลบิล รายการ สมาชิก และข้อมูลการชำระเงินใน PostgreSQL
5. ผู้ใช้สามารถอัปโหลดหรือดูสลิป/ใบเสร็จ และตรวจสอบสถิติของบิลได้

## Database Design (ER Diagram)

ระบบใช้ฐานข้อมูล PostgreSQL เพื่อจัดเก็บข้อมูลบัญชีผู้ใช้ บิล รายการค่าใช้จ่าย สมาชิก และการชำระเงิน โดยโครงสร้างตารางและความสัมพันธ์ระหว่างตารางแสดงไว้ใน ER Diagram ด้านล่าง

![SplitMate ER Diagram]<img width="2048" height="1972" alt="er" src="<img width="945" height="1024" alt="er" src="https://github.com/user-attachments/assets/4fcfe931-4668-4cac-9364-9f26b199ac7c" />
" /># SplitMate — ระบบจัดการและแบ่งบิลค่าใช้จ่าย

### คำอธิบาย ER Diagram

ER Diagram แสดงตารางหลักของระบบและการเชื่อมโยงกันผ่าน Primary Key (PK) และ Foreign Key (FK) โดยมีรายละเอียดดังนี้

#### 1. ข้อมูลผู้ใช้และการตั้งค่า

- **`USERS`** เก็บข้อมูลบัญชีผู้ใช้ เช่น ชื่อผู้ใช้ อีเมล รหัสผ่าน และวันเวลาที่สร้างหรือแก้ไขข้อมูล โดย `id` เป็น Primary Key
- **`PROFILES`** เก็บข้อมูลเพิ่มเติมของผู้ใช้ เช่น ชื่อที่แสดง รูปโปรไฟล์ เบอร์โทรศัพท์ และประวัติย่อ เชื่อมกับ `USERS` ผ่าน `user_id`
- **`USER_SETTINGS`** เก็บค่าการตั้งค่าของผู้ใช้ เช่น ภาษา สกุลเงิน และการเปิดหรือปิดการแจ้งเตือน
- **`NOTIFICATIONS`** เก็บการแจ้งเตือนที่ส่งถึงผู้ใช้ เช่น ประเภท หัวข้อ ข้อความ สถานะอ่านแล้ว และวันเวลาที่สร้าง

#### 2. กลุ่มและสมาชิก

- **`GROUPS`** เก็บข้อมูลกลุ่ม เช่น ชื่อ คำอธิบาย และผู้ใช้ที่สร้างกลุ่ม
- **`GROUP_MEMBERS`** เป็นตารางเชื่อมระหว่างกลุ่มกับผู้ใช้ โดยเก็บ `group_id`, `user_id`, บทบาทของสมาชิก และเวลาที่เข้าร่วม ทำให้กลุ่มหนึ่งมีสมาชิกได้หลายคน และผู้ใช้สามารถเป็นสมาชิกของหลายกลุ่มได้
- **`BILL_MEMBERS`** เก็บสมาชิกที่เกี่ยวข้องกับแต่ละบิล รองรับทั้งสมาชิกที่มีบัญชีผู้ใช้และสมาชิกแบบ Guest ซึ่งอาจใช้ `guest_name` และ `guest_token`

#### 3. บิลและรายการค่าใช้จ่าย

- **`BILLS`** เก็บข้อมูลบิล เช่น ผู้สร้างบิล ชื่อร้าน วันที่และเวลา หมายเหตุ ยอดรวมย่อย ส่วนลด ค่าบริการ VAT ยอดสุทธิ และสถานะบิล
- **`BILL_ITEMS`** เก็บรายการอาหารหรือค่าใช้จ่ายแต่ละรายการในบิล เช่น ชื่อ จำนวน ราคาต่อหน่วย และราคารวม โดยแต่ละรายการอ้างอิงบิลผ่าน `bill_id`
- **`SPLIT_CONFIGS`** เก็บการตั้งค่าวิธีแบ่งค่าใช้จ่ายของบิล เช่น วิธีแบ่งและข้อมูลประกอบการคำนวณใน `config_data`
- **`ITEM_ASSIGNMENTS`** เชื่อมรายการใน `BILL_ITEMS` กับสมาชิกใน `BILL_MEMBERS` และเก็บจำนวนเงินที่กำหนดให้สมาชิกแต่ละคน จึงใช้ระบุได้ว่าใครรับผิดชอบค่าใช้จ่ายส่วนใด

#### 4. การชำระเงินและการเคลียร์ยอด

- **`PAYMENTS`** เก็บข้อมูลการชำระเงินของสมาชิกในบิล เช่น จำนวนเงิน สถานะ วิธีชำระเงิน และวันที่ชำระ โดยอ้างอิงสมาชิกผ่าน `bill_member_id`
- **`SETTLEMENTS`** เก็บข้อมูลการเคลียร์ยอดระหว่างสมาชิก โดยระบุบิล ผู้จ่าย (`from_member_id`) ผู้รับเงิน (`to_member_id`) จำนวนเงิน สถานะ และเวลาที่เคลียร์ยอด

### วิธีอ่านความสัมพันธ์ในแผนภาพ

- `PK` (Primary Key) คือคีย์หลักที่ใช้ระบุแถวข้อมูลแต่ละรายการในตาราง
- `FK` (Foreign Key) คือคีย์ที่ใช้อ้างอิงข้อมูลจากอีกตาราง เพื่อเชื่อมโยงข้อมูลเข้าด้วยกัน
- ตารางเชื่อม เช่น `GROUP_MEMBERS` และ `ITEM_ASSIGNMENTS` ใช้แทนความสัมพันธ์ที่สมาชิกหรือรายการหนึ่ง ๆ เชื่อมโยงกับข้อมูลหลายรายการ
- ตัวอย่างเช่น `BILLS` เชื่อมกับ `BILL_ITEMS` เพื่อเก็บรายการในบิล และเชื่อมกับ `BILL_MEMBERS` เพื่อระบุสมาชิกที่เกี่ยวข้อง ส่วน `ITEM_ASSIGNMENTS` ระบุว่าสมาชิกแต่ละคนรับผิดชอบยอดของรายการใด

> **หมายเหตุ:** คำอธิบายส่วนนี้อธิบายตามตารางที่ปรากฏในภาพ ER Diagram ที่แนบมา ภาพนี้มีตาราง `GROUPS` และ `GROUP_MEMBERS` ดังนั้นควรตรวจสอบให้แน่ใจว่าแผนภาพตรงกับ migration/schema ที่ใช้จริงในโปรเจกต์ก่อนส่งงาน

## Installation & Setup

### สิ่งที่ต้องมี

- JDK 17
- Maven (หรือใช้ Maven Wrapper ที่อยู่ในโฟลเดอร์ `code/`)
- Docker Desktop และ Docker Compose หากต้องการรันฐานข้อมูลผ่าน Docker
- PostgreSQL หากต้องการรันโดยไม่ใช้ Docker

### 1. Clone โปรเจกต์

```bash
git clone <GITHUB_REPOSITORY_URL>
cd Project_Principles
```

แทนที่ `<GITHUB_REPOSITORY_URL>` ด้วย URL ของ Repository จริง

### 2. ตั้งค่าฐานข้อมูล

โปรเจกต์ต้องใช้ PostgreSQL และ Flyway จะรัน migration เพื่อสร้าง/ปรับ schema เมื่อเริ่มแอป

**เพื่อความปลอดภัย ห้าม commit username/password หรือ connection string ของฐานข้อมูลจริงลง GitHub** ให้ตั้งค่าฐานข้อมูลผ่าน Environment Variables และปรับ `code/src/main/resources/application.properties` ให้ใช้ค่าจาก environment เช่น:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

spring.flyway.url=${DB_URL}
spring.flyway.user=${DB_USER}
spring.flyway.password=${DB_PASSWORD}
```

ตั้งค่า Environment Variables ในเครื่องตามฐานข้อมูลที่จะใช้:

```text
DB_URL=jdbc:postgresql://localhost:5432/splitmate
DB_USER=splitmate
DB_PASSWORD=กำหนดรหัสผ่านของคุณเอง
```

> **ข้อควรระวังด้านความปลอดภัย:** ไฟล์ `application.properties` ใน ZIP ที่นำมาตรวจมีข้อมูลเชื่อมต่อฐานข้อมูลแบบระบุค่าไว้โดยตรง หากเป็นข้อมูลฐานข้อมูลจริง ควรเปลี่ยน/rotate รหัสผ่านดังกล่าวทันที และเปลี่ยนเป็น Environment Variables ก่อนเผยแพร่ Repository

### 3. ติดตั้ง Dependencies / Build

Windows PowerShell:

```powershell
cd code
.\mvnw.cmd clean package
```

macOS / Linux:

```bash
cd code
chmod +x mvnw
./mvnw clean package
```

## How to Run

### วิธีที่ 1: Docker Compose

จากโฟลเดอร์โปรเจกต์:

```bash
cd code
docker compose up --build
```

เมื่อแอปเริ่มทำงานแล้ว เปิดเว็บไซต์ในเครื่องที่:

- Web App: http://localhost:8080
- Login: http://localhost:8080/login
- Swagger UI: http://localhost:8080/swagger-ui.html

**สำคัญ:** ก่อนรัน Docker Compose ให้ตรวจสอบว่าค่า datasource ใน `application.properties` ใช้ Environment Variables ตามตัวอย่างข้างต้น เพราะค่า datasource ที่ระบุเป็นค่าคงที่อาจทำให้แอปเชื่อมต่อฐานข้อมูลอื่นแทน service `postgres` ใน Compose

### วิธีที่ 2: รันด้วย Maven

เปิด PostgreSQL ให้ทำงานก่อน จากนั้น:

```bash
cd code
./mvnw spring-boot:run
```

บน Windows PowerShell ใช้:

```powershell
cd code
.\mvnw.cmd spring-boot:run
```

จากนั้นเข้า `http://localhost:8080`

## API Documentation

REST API หลักอยู่ภายใต้ `/api/v1` และ API ส่วนใหญ่ต้องเข้าสู่ระบบก่อน ยกเว้น API สมัครสมาชิก

### Users

| Method | Endpoint | รายละเอียด |
|---|---|---|
| `POST` | `/api/v1/users` | สมัครสมาชิก |
| `GET` | `/api/v1/users/me` | ดูข้อมูลผู้ใช้ที่เข้าสู่ระบบ |
| `PUT` | `/api/v1/users/me/profile` | แก้ไขข้อมูลโปรไฟล์ |
| `PATCH` | `/api/v1/users/me/settings/notification` | เปิด/ปิดการแจ้งเตือน |
| `GET` | `/api/v1/users/lookup?email={email}` | ค้นหาผู้ใช้ด้วยอีเมลเพื่อเชิญเข้าบิล |
| `GET` | `/api/v1/users/{id}` | ดูข้อมูลผู้ใช้ตาม ID (บัญชีตนเอง) |
| `PUT` | `/api/v1/users/{id}` | แก้ไขข้อมูลผู้ใช้ตาม ID (บัญชีตนเอง) |
| `DELETE` | `/api/v1/users/{id}` | ลบบัญชีผู้ใช้ตาม ID (บัญชีตนเอง) |

### Bills

| Method | Endpoint | รายละเอียด |
|---|---|---|
| `GET` | `/api/v1/bills` | ดูบิลที่สร้างหรือเข้าร่วม |
| `POST` | `/api/v1/bills` | สร้างบิล |
| `POST` | `/api/v1/bills/draft` | สร้างบิลแบบร่าง |
| `GET` | `/api/v1/bills/{id}` | ดูรายละเอียดบิล |
| `PUT` | `/api/v1/bills/{id}` | แก้ไขบิล (เจ้าของบิล) |
| `PATCH` | `/api/v1/bills/{id}/status` | เปลี่ยนสถานะบิล (เจ้าของบิล) |
| `DELETE` | `/api/v1/bills/{id}` | ลบบิล (เจ้าของบิล) |
| `GET` | `/api/v1/bills/join-code/next` | ขอรหัสเข้าร่วมบิล |
| `POST` | `/api/v1/bills/join` | เข้าร่วมบิลด้วยรหัส |
| `GET` | `/api/v1/bills/{id}/members` | ดูสมาชิกในบิล |
| `DELETE` | `/api/v1/bills/{id}/members/{userId}` | นำสมาชิกออกจากบิล |
| `POST` | `/api/v1/bills/{id}/slip` | อัปโหลดสลิป/ใบเสร็จของบิล |
| `GET` | `/api/v1/bills/{id}/slip` | ดูสลิป/ใบเสร็จของบิล |
| `GET` | `/api/v1/bills/{id}/payments` | ดูข้อมูลการชำระเงิน |
| `PUT` | `/api/v1/bills/{id}/payments` | บันทึก/ปรับข้อมูลการชำระเงิน |
| `PATCH` | `/api/v1/bills/{id}/payments/{paymentId}/status` | เปลี่ยนสถานะการชำระเงิน |
| `POST` | `/api/v1/bills/{id}/payments/{paymentId}/slip` | อัปโหลดสลิปของสมาชิก |
| `GET` | `/api/v1/bills/{id}/payments/{paymentId}/slip` | ดูสลิปของสมาชิก |
| `DELETE` | `/api/v1/bills/{id}/payments/{paymentId}/slip` | ลบสลิปของสมาชิก |

### Invitations

| Method | Endpoint | รายละเอียด |
|---|---|---|
| `POST` | `/api/v1/bills/{billId}/invitations` | เชิญผู้ใช้เข้าร่วมบิล |
| `GET` | `/api/v1/bills/{billId}/invitations` | ดูคำเชิญของบิล |
| `GET` | `/api/v1/invitations` | ดูคำเชิญที่ได้รับ |
| `PUT` | `/api/v1/invitations/{id}/accept` | ยอมรับคำเชิญ |
| `PUT` | `/api/v1/invitations/{id}/reject` | ปฏิเสธคำเชิญ |

### Statistics

| Method | Endpoint | รายละเอียด |
|---|---|---|
| `GET` | `/api/statistics/bills` | ดูสถิติบิล |

### Swagger UI

- Local: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Production: [https://splitmate-x3db.onrender.com/swagger-ui.html](https://splitmate-x3db.onrender.com/swagger-ui.html)

API ที่ต้องยืนยันตัวตนจำเป็นต้องส่ง session/cookie ที่ผ่านการเข้าสู่ระบบแล้ว รายละเอียด request/response สามารถตรวจสอบเพิ่มเติมได้จาก Swagger UI หากเปิดใช้งานบน deployment

## How to Run Tests

มี test source อยู่ที่ `test/java/com/example/splitbill/` โดยใช้ Spring Boot Test

จากโฟลเดอร์ `code/`:

```bash
./mvnw test
```

บน Windows PowerShell:

```powershell
.\mvnw.cmd test
```

หรือสั่ง build พร้อมทดสอบ:

```bash
./mvnw clean verify
```

> ควรรันคำสั่งทดสอบใน environment ที่ตั้งค่า Java/Maven ถูกต้อง และตรวจสอบผลลัพธ์จาก terminal ก่อนระบุจำนวน test ที่ผ่านในรายงาน เนื่องจาก README นี้ไม่ได้อ้างจำนวน test ที่ยังไม่ได้ตรวจยืนยัน

## Deployment URL

- **Production Web App:** [https://splitmate-x3db.onrender.com/login](https://splitmate-x3db.onrender.com/login)
- **Hosting Platform:** Render
- **Containerization:** Dockerfile อยู่ที่ `code/Dockerfile`
- **Local Services:** Docker Compose อยู่ที่ `code/docker-compose.yml`
- **Database:** PostgreSQL; ไฟล์โปรเจกต์มีการตั้งค่าการเชื่อมต่อ Neon อยู่ด้วย

> URL ข้างต้นเป็น URL ที่ผู้พัฒนาให้มา ควรตรวจสอบการเข้าใช้งานจริงหลัง deploy และทดสอบ flow สำคัญ เช่น สมัครสมาชิก เข้าสู่ระบบ สร้างบิล เชิญสมาชิก และอัปโหลดสลิป ก่อนส่งงาน


## Project Structure

```text
Project_Principles/
├── .github/                         # การตั้งค่า GitHub
├── code/
│   ├── Dockerfile                   # สร้าง Docker image ของแอป
│   ├── docker-compose.yml           # แอปและ PostgreSQL สำหรับ local
│   ├── pom.xml                      # Maven dependencies และ build
│   ├── mvnw                         # Maven Wrapper (macOS/Linux)
│   ├── mvnw.cmd                     # Maven Wrapper (Windows)
│   └── src/
│       ├── main/
│       │   ├── java/com/example/splitbill/
│       │   │   ├── config/          # การตั้งค่าระบบ
│       │   │   ├── controller/
│       │   │   │   ├── api/         # REST API
│       │   │   │   └── web/         # Web controllers
│       │   │   ├── dto/             # Request / Response DTOs
│       │   │   ├── exception/       # Exception handling
│       │   │   ├── mapper/          # Mapping ระหว่าง model และ DTO
│       │   │   ├── model/           # Entity และ Enum
│       │   │   ├── repository/      # Data access
│       │   │   ├── service/         # Business logic
│       │   │   └── SplitbillApplication.java
│       │   └── resources/
│       │       ├── db/migration/    # Flyway SQL migrations
│       │       ├── templates/html/  # Thymeleaf templates
│       │       ├── static/css/      # CSS
│       │       ├── static/js/       # JavaScript
│       │       └── application.properties
│       └── test/                    # (ตรวจสอบตำแหน่ง test ตาม pom.xml)
├── doc/
│   ├── architecture.md              # เอกสารสถาปัตยกรรม
│   ├── HELP.md                      # เอกสารช่วยเหลือ
│   └── solid-analysis.md            # วิเคราะห์ SOLID
├── img/                             # ภาพประกอบหน้าจอ
└── README.md
```



# Project ระบบแชร์ค่าอาหาร
# รายชื่อสมาชิกกลุ่ม
| ลำดับ | รหัสนักศึกษา | ชื่อ-นามสกุล | Emaill | Branch | หน้าที่รับผิดชอบ |
| :---: | :---: | :--- | :--- | :---: | :--- |
| 1 | 673380413-9 | ปิยพันธ์ แก้วเก็บคำ | piyapan.k@kkumail.com | | |
| 2 | 673380416-3 | พัทธดนย์ คำนัน | pattadon.kh@kkumail.com | | |
| 3 | 673380426-0 | วรปรัชญ์ พิมพ์อุบล | woraprat.p@kkumail.com | | |
| 4 | 673380434-1 | เนติภัทร ภูครองเพชร | netiphat.p@kkumail.com | | |

# โครงสร้าง Repository
```
├── 📁 code/   # Source code + Configuration (pom.xml, mvnw, src/main, application.properties)
├── 📁 test/   # การทดสอบทั้งหมด (JUnit)
├── 📁 doc/    # เอกสารทั้งหมดและสไลด์
└── 📁 img/    # ไฟล์มัลติมีเดีย
```

# วิธีรันโปรเจกต์
```bash
cd code
./mvnw spring-boot:run     # รันแอป
./mvnw test                # รันเทสต์ (อ่านจากโฟลเดอร์ ../test/java)
```

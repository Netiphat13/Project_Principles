# SOLID Analysis — SplitMate

| Principle | จุดที่แสดงในโค้ด | เหตุผล |
|---|---|---|
| S — Single Responsibility | `service/UserServiceImpl.java`, `GroupServiceImpl.java`, `BillServiceImpl.java` | Service รับผิดชอบ business logic ของ resource ตัวเอง; Controller รับ HTTP และ Service รับ business logic; Mapper รับการแปลง Entity → DTO |
| O — Open/Closed | `service/*.java` + `service/impl/*.java` | Controller ขึ้นกับ interface ทำให้เปลี่ยน implementation หรือเพิ่ม implementation ได้โดยไม่ต้องแก้ Controller |
| L — Liskov Substitution | `UserServiceImpl implements UserService`, `GroupServiceImpl implements GroupService`, `BillServiceImpl implements BillService` | Controller สามารถใช้ implementation ใด ๆ ที่ทำตาม contract ของ interface ได้ |
| I — Interface Segregation | `UserService`, `GroupService`, `BillService` | แยก interface ตาม resource ไม่สร้าง interface ใหญ่ที่รวมทุก operation |
| D — Dependency Inversion | Constructor ของ `*ServiceImpl` และ `*RestController` | Service/Controller รับ dependency ผ่าน constructor และ service implementation ทำงานผ่าน repository abstraction |

## Layer rule

`controller → service → repository → model`

Controller ไม่เรียก Repository โดยตรง และ API response ไม่ส่ง Entity ออกไปโดยตรง แต่ผ่าน DTO + Mapper

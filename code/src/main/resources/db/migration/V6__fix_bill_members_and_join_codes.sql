-- แก้ตามรีวิว PR #6 (เป็น V6 เพราะ V3–V5 ถูกรันบนฐานข้อมูลของทีมไปแล้ว — ห้ามแก้ไฟล์ที่รันแล้ว)

-- 1) บทบาทสมาชิก: V3 ตั้งทุกแถวเดิมเป็น MEMBER แม้แต่ผู้สร้างบิล -> แก้ให้ผู้สร้างเป็น OWNER
UPDATE bill_members m SET role = 'OWNER'
FROM bills b
WHERE m.bill_id = b.id AND m.user_id = b.created_by AND m.role IS DISTINCT FROM 'OWNER';
UPDATE bill_members SET role = 'MEMBER' WHERE role IS NULL;

-- 2) ลบแถวสมาชิกซ้ำ (ผู้ใช้เดียวกันในบิลเดียวกัน) ที่อาจเกิดจากกดเข้าร่วมซ้ำ ก่อนสร้าง unique index
--    เก็บแถว OWNER ไว้ก่อน ถ้าไม่มีเก็บแถวที่เข้าร่วมก่อน (id น้อยสุด)
DELETE FROM bill_members m
USING (
    SELECT id, ROW_NUMBER() OVER (
               PARTITION BY bill_id, user_id
               ORDER BY (role = 'OWNER') DESC, id) AS rn
    FROM bill_members
    WHERE user_id IS NOT NULL
) d
WHERE m.id = d.id AND d.rn > 1;

-- 3) บิลที่ผู้สร้างยังไม่อยู่ใน bill_members: เพิ่มเป็น OWNER ให้เหมือนบิลที่สร้างใหม่
INSERT INTO bill_members (bill_id, user_id, role, joined_at)
SELECT b.id, b.created_by, 'OWNER', b.created_at
FROM bills b
WHERE NOT EXISTS (
    SELECT 1 FROM bill_members m WHERE m.bill_id = b.id AND m.user_id = b.created_by
);

-- 4) ผู้ใช้หนึ่งคนเป็นสมาชิกของบิลเดียวกันได้ครั้งเดียว (กันกดเข้าร่วม/ยอมรับคำเชิญซ้ำพร้อมกัน)
CREATE UNIQUE INDEX IF NOT EXISTS ux_bill_members_bill_user
    ON bill_members (bill_id, user_id)
    WHERE user_id IS NOT NULL;

-- 5) รหัสเข้าร่วมที่ V3 คำนวณจาก MD5(id || 'splitmate') เดาได้ (repo เป็น public)
--    สุ่มใหม่เฉพาะบิลที่ยังใช้รหัสสูตรนั้นอยู่ ใช้ตัวอักษรชุดเดียวกับ BillServiceImpl.CODE_CHARS
DO $$
DECLARE
    r RECORD;
    new_code TEXT;
BEGIN
    FOR r IN
        SELECT id FROM bills
        WHERE join_code IS NULL
           OR join_code = 'SM-' || UPPER(SUBSTRING(MD5(id::text || 'splitmate') FROM 1 FOR 6))
    LOOP
        LOOP
            SELECT 'SM-' || string_agg(
                       substr('ABCDEFGHJKLMNPQRSTUVWXYZ23456789', 1 + floor(random() * 32)::int, 1), '')
            INTO new_code
            FROM generate_series(1, 6);
            EXIT WHEN NOT EXISTS (SELECT 1 FROM bills WHERE join_code = new_code);
        END LOOP;
        UPDATE bills SET join_code = new_code WHERE id = r.id;
    END LOOP;
END $$;

-- 6) ระบบเดิมสร้างบิลเป็น DRAFT แต่หน้าเว็บใหม่ใช้ PENDING เป็นสถานะเริ่มต้น (เลือก DRAFT ในหน้าบิลไม่ได้)
UPDATE bills SET status = 'PENDING' WHERE status = 'DRAFT' OR status IS NULL;

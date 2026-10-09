-- ฟีเจอร์จาก SplitMate-connected: รหัสเข้าร่วมบิล, คำเชิญเข้าบิล, สลิปของบิล และยกเลิกระบบกลุ่ม

-- บทบาทของสมาชิกในบิล: OWNER = ผู้สร้างบิล, MEMBER = เพื่อนที่เข้าร่วม
ALTER TABLE bill_members ADD COLUMN IF NOT EXISTS role VARCHAR(50);
UPDATE bill_members SET role = 'MEMBER' WHERE role IS NULL;

-- รหัสให้เพื่อนกรอกเพื่อเข้าร่วมบิล (เติมให้บิลเดิมที่ยังไม่มีรหัส)
ALTER TABLE bills ADD COLUMN IF NOT EXISTS join_code VARCHAR(12);
UPDATE bills
SET join_code = 'SM-' || UPPER(SUBSTRING(MD5(id::text || 'splitmate') FROM 1 FOR 6))
WHERE join_code IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_bills_join_code ON bills (join_code);

-- ชื่อไฟล์สลิป/ใบเสร็จที่อัปโหลดไว้บน server
ALTER TABLE bills ADD COLUMN IF NOT EXISTS slip_image VARCHAR(255);

-- คำเชิญเข้าบิล
CREATE TABLE IF NOT EXISTS invitations (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    inviter_id BIGINT NOT NULL,
    invitee_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_invitations_bill
        FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE,
    CONSTRAINT fk_invitations_inviter
        FOREIGN KEY (inviter_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_invitations_invitee
        FOREIGN KEY (invitee_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ck_invitations_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'))
);

-- เชิญคนเดิมเข้าบิลเดิมซ้ำไม่ได้ ระหว่างที่คำเชิญยังรอตอบ
CREATE UNIQUE INDEX IF NOT EXISTS ux_invitations_pending
    ON invitations (bill_id, invitee_id)
    WHERE status = 'PENDING';
CREATE INDEX IF NOT EXISTS ix_invitations_invitee ON invitations (invitee_id);
CREATE INDEX IF NOT EXISTS ix_invitations_bill ON invitations (bill_id);

-- ยกเลิกระบบกลุ่ม (ใช้รหัสเข้าร่วมบิล + คำเชิญแทน)
DROP TABLE IF EXISTS group_members;
DROP TABLE IF EXISTS groups;

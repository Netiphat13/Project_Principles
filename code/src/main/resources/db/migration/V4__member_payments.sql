-- สถานะการชำระเงินและสลิปรายสมาชิก (ฟีเจอร์จาก splitbill-responsive)
-- แถวหนึ่งแถว = คนหนึ่งคนในบิล (อ้างด้วยชื่อ เพราะเพื่อนที่ไม่มีบัญชีก็ต้องมีสถานะจ่ายเงินเหมือนกัน)
CREATE TABLE IF NOT EXISTS bill_payments (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    member_name VARCHAR(100) NOT NULL,
    amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    slip_image VARCHAR(255),
    slip_uploaded_at TIMESTAMP,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_bill_payments_bill
        FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE,
    CONSTRAINT ck_bill_payments_status
        CHECK (status IN ('PENDING', 'PAID', 'OVERDUE')),
    CONSTRAINT uk_bill_payments_member
        UNIQUE (bill_id, member_name)
);

CREATE INDEX IF NOT EXISTS ix_bill_payments_bill ON bill_payments (bill_id);

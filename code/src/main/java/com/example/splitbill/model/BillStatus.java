package com.example.splitbill.model;

import java.util.Set;

/**
 * สถานะของบิล (เก็บเป็น String ในคอลัมน์ bills.status)
 * บิลที่บันทึกจากหน้าเว็บเป็น PENDING (รอจ่าย) — ระหว่างสร้างบิล (ขั้นที่ 2 เป็นต้นไป) เป็น DRAFT
 * เพื่อให้เพื่อนกรอกรหัสเข้าร่วมได้ก่อนบันทึกบิล
 */
public final class BillStatus {
    public static final String DRAFT = "DRAFT";
    public static final String PENDING = "PENDING";
    public static final String PAID = "PAID";
    public static final String OVERDUE = "OVERDUE";
    public static final String CANCELLED = "CANCELLED";

    public static final String DEFAULT = PENDING;
    public static final Set<String> ALL = Set.of(DRAFT, PENDING, PAID, OVERDUE, CANCELLED);

    private BillStatus() {
    }
}

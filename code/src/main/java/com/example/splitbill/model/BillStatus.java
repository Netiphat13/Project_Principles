package com.example.splitbill.model;

import java.util.Set;

/**
 * สถานะของบิล (เก็บเป็น String ในคอลัมน์ bills.status)
 * บิลที่สร้างจากหน้าเว็บเริ่มที่ PENDING (รอจ่าย) — หน้าเว็บไม่มีขั้นฉบับร่างแล้ว
 * DRAFT ยังรับได้เพื่อรองรับข้อมูลเก่า
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

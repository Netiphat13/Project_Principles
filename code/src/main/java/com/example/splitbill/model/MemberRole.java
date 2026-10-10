package com.example.splitbill.model;

/**
 * บทบาทของสมาชิกในบิล (เก็บเป็น String ในคอลัมน์ bill_members.role)
 */
public final class MemberRole {
    /** ผู้สร้างบิล */
    public static final String OWNER = "OWNER";
    /** เพื่อนที่เข้าร่วมด้วยรหัสหรือคำเชิญ */
    public static final String MEMBER = "MEMBER";

    private MemberRole() {
    }
}

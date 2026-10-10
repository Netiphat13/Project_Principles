package com.example.splitbill.dto.response;

// ข้อมูลผู้ใช้ที่ล็อกอินอยู่ + โปรไฟล์ + การตั้งค่า (ใช้เติมข้อมูลทุกหน้า)
public record SessionResponse(Long id, String username, String email,
                              String phone, String bio, Boolean notificationEnabled) {}

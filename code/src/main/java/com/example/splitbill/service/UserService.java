package com.example.splitbill.service;

import com.example.splitbill.dto.request.ProfileRequest;
import com.example.splitbill.dto.request.UserRequest;
import com.example.splitbill.dto.response.SessionResponse;
import com.example.splitbill.dto.response.UserResponse;

public interface UserService {
    UserResponse create(UserRequest request);
    UserResponse getById(Long id);
    // แก้ไข/ลบได้เฉพาะบัญชีของตัวเอง
    UserResponse update(Long id, UserRequest request, Long currentUserId);
    void delete(Long id, Long currentUserId);
    UserResponse authenticate(String email, String password);

    // หาผู้ใช้จากอีเมลแบบตรงตัว (ใช้ตอนเชิญเพื่อนเข้าบิล) — ค้นไม่เจอบ่อยเกินไปจะได้ 429
    UserResponse findByEmail(String email, Long requesterId);

    // ข้อมูลผู้ใช้ที่ล็อกอิน + โปรไฟล์ + การตั้งค่า
    SessionResponse session(Long userId);

    // แก้ชื่อ เบอร์โทร และแนะนำตัวของตัวเอง
    SessionResponse updateProfile(Long userId, ProfileRequest request);

    boolean setNotificationEnabled(Long userId, boolean enabled);
}

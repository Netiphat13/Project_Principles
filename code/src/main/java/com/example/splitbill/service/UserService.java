package com.example.splitbill.service;

import com.example.splitbill.dto.request.UserRequest;
import com.example.splitbill.dto.response.UserResponse;

public interface UserService {
    UserResponse create(UserRequest request);
    UserResponse getById(Long id);
    // แก้ไข/ลบได้เฉพาะบัญชีของตัวเอง
    UserResponse update(Long id, UserRequest request, Long currentUserId);
    void delete(Long id, Long currentUserId);
    UserResponse authenticate(String email, String password);
}

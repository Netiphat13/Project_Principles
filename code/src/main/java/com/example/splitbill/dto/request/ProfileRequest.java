package com.example.splitbill.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// แก้ไขโปรไฟล์ของตัวเอง — อีเมลใช้ล็อกอิน จึงไม่ให้แก้จากตรงนี้
public record ProfileRequest(
        @NotBlank @Size(max = 100) String username,
        @Size(max = 50) String phone,
        @Size(max = 500) String bio
) {}

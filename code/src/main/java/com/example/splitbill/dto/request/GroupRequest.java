package com.example.splitbill.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ผู้สร้างกลุ่มมาจาก session ของผู้ใช้ที่ล็อกอิน ไม่รับจาก client
public record GroupRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 1000) String description
) {}

package com.example.splitbill.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// ผู้สร้างบิลมาจาก session ของผู้ใช้ที่ล็อกอิน ไม่รับจาก client
public record BillRequest(
        @NotBlank @Size(max = 255) String restaurantName,
        LocalDate billDate,
        LocalTime billTime,
        String note,
        @DecimalMin("0.00") BigDecimal discount,
        @DecimalMin("0.00") BigDecimal serviceCharge,
        @DecimalMin("0.00") BigDecimal vat,
        @Size(max = 50) String splitMethod,
        // รายละเอียดการแบ่ง (JSON string) ที่หน้าสร้างบิลคำนวณไว้
        @Size(max = 100_000) String splitConfigData,
        @Valid List<BillItemRequest> items,
        // รหัสเข้าร่วมที่หน้าเว็บแสดงไว้ (ถ้าซ้ำหรือว่าง server จะสุ่มใหม่)
        @Size(max = 12) String joinCode
) {}

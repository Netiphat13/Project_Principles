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
        @Valid List<BillItemRequest> items
) {}

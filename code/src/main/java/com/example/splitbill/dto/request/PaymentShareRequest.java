package com.example.splitbill.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

// ยอดที่สมาชิกแต่ละคนต้องจ่าย (paid = จ่ายแล้วตั้งแต่ตอนสร้างบิล)
public record PaymentShareRequest(
        @NotBlank @Size(max = 100) String name,
        @DecimalMin("0.00") BigDecimal amount,
        Boolean paid
) {}

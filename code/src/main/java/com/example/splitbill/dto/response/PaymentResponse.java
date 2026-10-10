package com.example.splitbill.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// สถานะการจ่ายเงินของสมาชิกหนึ่งคนในบิล (owner = เจ้าของบิล ไม่ต้องส่งสลิป)
public record PaymentResponse(Long id, String memberName, BigDecimal amount, String status,
                              boolean owner, boolean hasSlip,
                              LocalDateTime slipUploadedAt, LocalDateTime paidAt) {}

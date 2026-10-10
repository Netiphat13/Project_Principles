package com.example.splitbill.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

// รายชื่อสมาชิกพร้อมยอดที่ต้องจ่าย ใช้สร้างรายการชำระเงินให้บิลเก่าที่ยังไม่มี
public record PaymentRosterRequest(
        @NotNull @Valid @Size(max = 100) List<PaymentShareRequest> payments
) {}

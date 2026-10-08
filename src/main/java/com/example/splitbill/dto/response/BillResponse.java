package com.example.splitbill.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record BillResponse(Long id, String restaurantName, LocalDate billDate,
                           LocalTime billTime, String note, BigDecimal subtotal,
                           BigDecimal discount, BigDecimal serviceCharge,
                           BigDecimal vat, BigDecimal totalAmount,
                           String status, Long createdById, String createdByName,
                           String splitMethod, String splitConfigData,
                           List<BillItemResponse> items) {}

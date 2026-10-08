package com.example.splitbill.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record BillRequest(
        @NotNull Long createdById,
        @NotBlank @Size(max = 255) String restaurantName,
        LocalDate billDate,
        LocalTime billTime,
        String note,
        BigDecimal discount,
        BigDecimal serviceCharge,
        BigDecimal vat,
        @Size(max = 50) String splitMethod,
        String splitConfigData,
        @Valid List<BillItemRequest> items
) {}

package com.example.splitbill.dto.response;

import java.math.BigDecimal;

public record BillItemResponse(Long id, String name, Integer quantity,
                               BigDecimal unitPrice, BigDecimal totalPrice) {}

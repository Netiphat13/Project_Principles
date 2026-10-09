package com.example.splitbill.dto.response;

import java.math.BigDecimal;

public record BillSummary(long billCount, BigDecimal total, BigDecimal average) {}

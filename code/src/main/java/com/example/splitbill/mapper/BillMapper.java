package com.example.splitbill.mapper;

import com.example.splitbill.dto.response.*;
import com.example.splitbill.model.Bill;
import org.springframework.stereotype.Component;

@Component
public class BillMapper {
    public BillResponse toResponse(Bill bill) {
        var items = bill.getItems().stream()
                .map(i -> new BillItemResponse(i.getId(), i.getName(), i.getQuantity(),
                        i.getUnitPrice(), i.getTotalPrice()))
                .toList();
        var config = bill.getSplitConfig();
        return new BillResponse(
                bill.getId(), bill.getRestaurantName(), bill.getBillDate(), bill.getBillTime(), bill.getNote(),
                bill.getSubtotal(), bill.getDiscount(), bill.getServiceCharge(), bill.getVat(),
                bill.getTotalAmount(), bill.getStatus(), bill.getCreatedBy().getId(),
                bill.getCreatedBy().getUsername(),
                config == null ? null : config.getSplitMethod(),
                config == null ? null : config.getConfigData(),
                items,
                bill.getJoinCode(),
                bill.getSlipImage() != null && !bill.getSlipImage().isBlank()
        );
    }
}

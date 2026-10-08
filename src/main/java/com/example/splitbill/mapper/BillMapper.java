package com.example.splitbill.mapper;

import com.example.splitbill.dto.response.*;
import com.example.splitbill.model.Bill;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class BillMapper {
    public BillResponse toResponse(Bill bill) {
        var items = bill.getItems().stream()
                .map(i -> new BillItemResponse(i.getId(), i.getName(), i.getQuantity(),
                        i.getUnitPrice(), i.getTotalPrice()))
                .toList();
        var creator = bill.getCreatedBy();
        var splitConfig = bill.getSplitConfig();
        return new BillResponse(
                bill.getId(), bill.getRestaurantName(), bill.getBillDate(), bill.getBillTime(), bill.getNote(),
                bill.getSubtotal(), bill.getDiscount(), bill.getServiceCharge(), bill.getVat(),
                bill.getTotalAmount(), bill.getStatus(), creator == null ? null : creator.getId(),
                creator == null ? null : creator.getUsername(),
                splitConfig == null ? null : splitConfig.getSplitMethod(),
                splitConfig == null ? null : splitConfig.getConfigData(),
                items
        );
    }
}

package com.example.splitbill.service.impl;

import com.example.splitbill.dto.request.BillRequest;
import com.example.splitbill.dto.response.BillResponse;
import com.example.splitbill.dto.response.BillSummary;
import com.example.splitbill.exception.ForbiddenException;
import com.example.splitbill.exception.ResourceNotFoundException;
import com.example.splitbill.mapper.BillMapper;
import com.example.splitbill.model.Bill;
import com.example.splitbill.model.BillItem;
import com.example.splitbill.model.SplitConfig;
import com.example.splitbill.repository.BillItemRepository;
import com.example.splitbill.repository.BillRepository;
import com.example.splitbill.repository.SplitConfigRepository;
import com.example.splitbill.repository.UserRepository;
import com.example.splitbill.service.BillService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional
public class BillServiceImpl implements BillService {
    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final SplitConfigRepository splitConfigRepository;
    private final UserRepository userRepository;
    private final BillMapper mapper;

    public BillServiceImpl(BillRepository billRepository, BillItemRepository billItemRepository,
                           SplitConfigRepository splitConfigRepository, UserRepository userRepository,
                           BillMapper mapper) {
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
        this.splitConfigRepository = splitConfigRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public BillResponse create(BillRequest request, Long currentUserId) {
        Bill bill = new Bill();
        bill.setCreatedBy(userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId)));
        bill.setStatus("DRAFT");
        applyFields(bill, request);
        Bill saved = billRepository.save(bill);
        persistItems(saved, request);
        persistSplitConfig(saved, request);
        return mapper.toResponse(saved);
    }

    @Override @Transactional(readOnly = true)
    public BillResponse getById(Long id, Long currentUserId) {
        return mapper.toResponse(findOwned(id, currentUserId));
    }

    @Override @Transactional(readOnly = true)
    public Page<BillResponse> findByCreator(Long userId, Pageable pageable) {
        return billRepository.findByCreatedById(userId, pageable).map(mapper::toResponse);
    }

    @Override
    public BillResponse update(Long id, BillRequest request, Long currentUserId) {
        Bill bill = findOwned(id, currentUserId);
        applyFields(bill, request);
        billItemRepository.deleteAll(bill.getItems());
        bill.getItems().clear();
        Bill saved = billRepository.save(bill);
        persistItems(saved, request);
        persistSplitConfig(saved, request);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id, Long currentUserId) {
        billRepository.delete(findOwned(id, currentUserId));
    }

    @Override @Transactional(readOnly = true)
    public BillSummary summarize(Long userId) {
        long count = billRepository.countByCreatedById(userId);
        BigDecimal total = billRepository.sumTotalAmountByCreatedById(userId).setScale(2, RoundingMode.HALF_UP);
        BigDecimal average = count == 0 ? BigDecimal.ZERO.setScale(2)
                : total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        return new BillSummary(count, total, average);
    }

    private void applyFields(Bill bill, BillRequest request) {
        bill.setRestaurantName(request.restaurantName());
        bill.setBillDate(request.billDate());
        bill.setBillTime(request.billTime());
        bill.setNote(request.note());
        bill.setDiscount(zero(request.discount()));
        bill.setServiceCharge(zero(request.serviceCharge()));
        bill.setVat(zero(request.vat()));

        BigDecimal subtotal = request.items() == null ? BigDecimal.ZERO :
                request.items().stream()
                        .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        bill.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        bill.setTotalAmount(subtotal.subtract(bill.getDiscount()).add(bill.getServiceCharge()).add(bill.getVat())
                .setScale(2, RoundingMode.HALF_UP));
    }

    private void persistItems(Bill bill, BillRequest request) {
        if (request.items() == null || request.items().isEmpty()) return;
        var items = request.items().stream().map(i -> {
            BillItem item = new BillItem();
            item.setBill(bill);
            item.setName(i.name());
            item.setQuantity(i.quantity());
            item.setUnitPrice(i.unitPrice());
            item.setTotalPrice(i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())).setScale(2, RoundingMode.HALF_UP));
            return item;
        }).toList();
        // ใส่กลับเข้า bill ด้วย เพื่อให้ response ที่ map จาก entity เดิมมีรายการครบ
        bill.getItems().addAll(billItemRepository.saveAll(items));
    }

    private void persistSplitConfig(Bill bill, BillRequest request) {
        if (request.splitMethod() == null || request.splitMethod().isBlank()) return;
        SplitConfig config = splitConfigRepository.findByBill_Id(bill.getId())
                .orElseGet(SplitConfig::new);
        config.setBill(bill);
        config.setSplitMethod(request.splitMethod());
        if (config.getConfigData() == null) config.setConfigData("{}");
        bill.setSplitConfig(splitConfigRepository.save(config));
    }

    private BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    private Bill findOwned(Long id, Long currentUserId) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + id));
        if (!bill.getCreatedBy().getId().equals(currentUserId)) {
            throw new ForbiddenException("You do not have access to bill " + id);
        }
        return bill;
    }
}

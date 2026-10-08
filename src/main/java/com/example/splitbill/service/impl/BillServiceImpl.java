package com.example.splitbill.service.impl;

import com.example.splitbill.dto.request.BillItemRequest;
import com.example.splitbill.dto.request.BillRequest;
import com.example.splitbill.dto.response.BillResponse;
import com.example.splitbill.exception.ResourceNotFoundException;
import com.example.splitbill.mapper.BillMapper;
import com.example.splitbill.model.Bill;
import com.example.splitbill.model.BillItem;
import com.example.splitbill.model.SplitConfig;
import com.example.splitbill.repository.BillItemRepository;
import com.example.splitbill.repository.BillMemberRepository;
import com.example.splitbill.repository.ItemAssignmentRepository;
import com.example.splitbill.repository.PaymentRepository;
import com.example.splitbill.repository.SettlementRepository;
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
    private final BillMemberRepository billMemberRepository;
    private final ItemAssignmentRepository itemAssignmentRepository;
    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;
    private final SplitConfigRepository splitConfigRepository;
    private final UserRepository userRepository;
    private final BillMapper mapper;

    public BillServiceImpl(BillRepository billRepository, BillItemRepository billItemRepository,
                           BillMemberRepository billMemberRepository,
                           ItemAssignmentRepository itemAssignmentRepository,
                           PaymentRepository paymentRepository,
                           SettlementRepository settlementRepository,
                           SplitConfigRepository splitConfigRepository, UserRepository userRepository,
                           BillMapper mapper) {
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
        this.billMemberRepository = billMemberRepository;
        this.itemAssignmentRepository = itemAssignmentRepository;
        this.paymentRepository = paymentRepository;
        this.settlementRepository = settlementRepository;
        this.splitConfigRepository = splitConfigRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public BillResponse create(BillRequest request) {
        Bill bill = new Bill();
        applyFields(bill, request);
        Bill saved = billRepository.save(bill);
        persistItems(saved, request);
        persistSplitConfig(saved, request);
        return mapper.toResponse(find(saved.getId()));
    }

    @Override @Transactional(readOnly = true)
    public BillResponse getById(Long id) {
        return mapper.toResponse(billRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + id)));
    }

    @Override @Transactional(readOnly = true)
    public Page<BillResponse> findAll(Pageable pageable) {
        return billRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Override @Transactional(readOnly = true)
    public Page<BillResponse> findByCreator(Long userId, Pageable pageable) {
        return billRepository.findByCreatedById(userId, pageable).map(mapper::toResponse);
    }

    @Override
    public BillResponse update(Long id, BillRequest request) {
        Bill bill = find(id);
        applyFields(bill, request);
        billItemRepository.deleteAll(bill.getItems());
        bill.getItems().clear();
        Bill saved = billRepository.save(bill);
        persistItems(saved, request);
        persistSplitConfig(saved, request);
        return mapper.toResponse(find(saved.getId()));
    }

    @Override
    public BillResponse updateStatus(Long id, String status) {
        Bill bill = find(id);
        String normalized = status == null ? "PENDING" : status.trim().toUpperCase();
        if (!normalized.matches("PENDING|PAID|OVERDUE|DRAFT|CANCELLED")) {
            throw new IllegalArgumentException("Invalid bill status: " + status);
        }
        bill.setStatus(normalized);
        return mapper.toResponse(billRepository.save(bill));
    }

    @Override
    public void delete(Long id) {
        if (!billRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bill not found: " + id);
        }

        // ลบข้อมูลลูกตามลำดับ Foreign Key ก่อนลบบิล
        // เพื่อให้ทำงานได้ทั้งกับฐานข้อมูลที่มี ON DELETE CASCADE และข้อมูลเก่าที่อาจมี relation ค้างอยู่
        settlementRepository.deleteAllByBillIdNative(id);
        itemAssignmentRepository.deleteAllByBillIdNative(id);
        paymentRepository.deleteAllByBillIdNative(id);
        splitConfigRepository.deleteAllByBillIdNative(id);
        billItemRepository.deleteAllByBillIdNative(id);
        billMemberRepository.deleteAllByBillIdNative(id);

        int deleted = billRepository.deleteByIdNative(id);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Bill not found: " + id);
        }
    }

    private void applyFields(Bill bill, BillRequest request) {
        bill.setCreatedBy(userRepository.findById(request.createdById())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.createdById())));
        bill.setRestaurantName(request.restaurantName());
        bill.setBillDate(request.billDate());
        bill.setBillTime(request.billTime());
        bill.setNote(request.note());
        bill.setDiscount(zero(request.discount()));
        bill.setServiceCharge(zero(request.serviceCharge()));
        bill.setVat(zero(request.vat()));
        bill.setStatus(bill.getStatus() == null ? "PENDING" : bill.getStatus());

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
        billItemRepository.saveAll(items);
    }

    private void persistSplitConfig(Bill bill, BillRequest request) {
        if (request.splitMethod() == null || request.splitMethod().isBlank()) return;
        SplitConfig config = splitConfigRepository.findByBillId(bill.getId())
                .orElseGet(SplitConfig::new);
        config.setBill(bill);
        config.setSplitMethod(request.splitMethod());
        config.setConfigData(request.splitConfigData() == null ? "{}" : request.splitConfigData());
        splitConfigRepository.save(config);
    }

    private BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    private Bill find(Long id) {
        return billRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + id));
    }
}

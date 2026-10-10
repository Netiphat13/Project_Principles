package com.example.splitbill.service.impl;

import com.example.splitbill.dto.request.BillRequest;
import com.example.splitbill.dto.response.BillMemberResponse;
import com.example.splitbill.dto.response.BillResponse;
import com.example.splitbill.dto.response.BillSummary;
import com.example.splitbill.exception.ConflictException;
import com.example.splitbill.exception.ForbiddenException;
import com.example.splitbill.exception.ResourceNotFoundException;
import com.example.splitbill.mapper.BillMapper;
import com.example.splitbill.model.Bill;
import com.example.splitbill.model.BillItem;
import com.example.splitbill.model.BillMember;
import com.example.splitbill.model.BillStatus;
import com.example.splitbill.model.MemberRole;
import com.example.splitbill.model.SplitConfig;
import com.example.splitbill.model.User;
import com.example.splitbill.repository.BillItemRepository;
import com.example.splitbill.repository.BillMemberRepository;
import com.example.splitbill.repository.BillRepository;
import com.example.splitbill.repository.SplitConfigRepository;
import com.example.splitbill.repository.UserRepository;
import com.example.splitbill.service.AttemptLimiter;
import com.example.splitbill.service.BillService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class BillServiceImpl implements BillService {
    // ตัวนับการกรอกรหัสบิลผิด (AttemptLimiter)
    private static final String JOIN_ACTION = "bill-join";
    private static final int MAX_JOIN_FAILURES = 10;
    private static final String CODE_PREFIX = "SM-";
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final long MAX_SLIP_BYTES = 8L * 1024 * 1024;
    // นามสกุลไฟล์ -> content type ที่ใช้ตอนส่งไฟล์กลับ
    private static final Map<String, String> SLIP_TYPES = Map.of(
            "jpg", "image/jpeg", "png", "image/png", "webp", "image/webp",
            "gif", "image/gif", "pdf", "application/pdf");

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final SplitConfigRepository splitConfigRepository;
    private final UserRepository userRepository;
    private final BillMemberRepository billMemberRepository;
    private final BillMapper mapper;
    private final AttemptLimiter attemptLimiter;
    private final JsonMapper jsonMapper; // Spring Boot 4 ใช้ Jackson 3 (package tools.jackson)
    private final SecureRandom random = new SecureRandom();

    @Value("${app.upload-dir:uploads/slips}")
    private String uploadDir;

    public BillServiceImpl(BillRepository billRepository, BillItemRepository billItemRepository,
                           SplitConfigRepository splitConfigRepository, UserRepository userRepository,
                           BillMemberRepository billMemberRepository, BillMapper mapper,
                           AttemptLimiter attemptLimiter, JsonMapper jsonMapper) {
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
        this.splitConfigRepository = splitConfigRepository;
        this.userRepository = userRepository;
        this.billMemberRepository = billMemberRepository;
        this.mapper = mapper;
        this.attemptLimiter = attemptLimiter;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public BillResponse create(BillRequest request, Long currentUserId) {
        requireValidJson(request.splitConfigData());
        Bill bill = new Bill();
        bill.setCreatedBy(findUser(currentUserId));
        bill.setStatus(BillStatus.DEFAULT);
        bill.setJoinCode(resolveJoinCode(request.joinCode()));
        applyFields(bill, request);
        Bill saved = billRepository.save(bill);
        addMember(saved, saved.getCreatedBy(), MemberRole.OWNER);
        persistItems(saved, request);
        persistSplitConfig(saved, request);
        return mapper.toResponse(saved);
    }

    @Override @Transactional(readOnly = true)
    public BillResponse getById(Long id, Long currentUserId) {
        return mapper.toResponse(findAccessible(id, currentUserId));
    }

    @Override @Transactional(readOnly = true)
    public Page<BillResponse> findByCreator(Long userId, Pageable pageable) {
        return billRepository.findByCreatedById(userId, pageable).map(mapper::toResponse);
    }

    @Override @Transactional(readOnly = true)
    public Page<BillResponse> findVisibleByUser(Long userId, Pageable pageable) {
        return billRepository.findVisibleByUser(userId, pageable).map(mapper::toResponse);
    }

    @Override
    public BillResponse update(Long id, BillRequest request, Long currentUserId) {
        Bill bill = findOwned(id, currentUserId);
        requireValidJson(request.splitConfigData());
        applyFields(bill, request);
        billItemRepository.deleteAll(bill.getItems());
        bill.getItems().clear();
        Bill saved = billRepository.save(bill);
        persistItems(saved, request);
        persistSplitConfig(saved, request);
        return mapper.toResponse(saved);
    }

    @Override
    public BillResponse updateStatus(Long id, String status, Long currentUserId) {
        Bill bill = findOwned(id, currentUserId);
        String normalized = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!BillStatus.ALL.contains(normalized)) {
            throw new IllegalArgumentException("Invalid bill status: " + status);
        }
        bill.setStatus(normalized);
        return mapper.toResponse(billRepository.save(bill));
    }

    @Override
    public void delete(Long id, Long currentUserId) {
        Bill bill = findOwned(id, currentUserId);
        String slip = bill.getSlipImage();
        billRepository.deleteBillById(bill.getId());
        deleteSlipFileAfterCommit(slip);
    }

    @Override @Transactional(readOnly = true)
    public BillSummary summarize(Long userId) {
        long count = billRepository.countByCreatedById(userId);
        BigDecimal total = billRepository.sumTotalAmountByCreatedById(userId).setScale(2, RoundingMode.HALF_UP);
        BigDecimal average = count == 0 ? BigDecimal.ZERO.setScale(2)
                : total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        return new BillSummary(count, total, average);
    }

    // ---------- รหัสเข้าร่วมบิล ----------

    @Override @Transactional(readOnly = true)
    public String nextJoinCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_PREFIX);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (billRepository.existsByJoinCode(code));
        return code;
    }

    @Override
    public BillResponse join(String code, Long currentUserId) {
        String normalized = normalizeCode(code);
        if (normalized.isEmpty()) throw new IllegalArgumentException("กรุณากรอกรหัสบิล");
        // กันสุ่มเดารหัส: กรอกผิดเกินกำหนดภายในช่วงเวลาหนึ่งจะได้ 429
        attemptLimiter.check(JOIN_ACTION, currentUserId, MAX_JOIN_FAILURES);
        Bill bill = billRepository.findByJoinCode(normalized)
                // เผื่อผู้ใช้พิมพ์มาแค่ AB12CD โดยไม่มี SM-
                .or(() -> billRepository.findByJoinCode(CODE_PREFIX + normalized))
                .orElse(null);
        if (bill == null) {
            attemptLimiter.recordFailure(JOIN_ACTION, currentUserId);
            throw new ResourceNotFoundException("ไม่พบบิลที่ใช้รหัสนี้");
        }
        attemptLimiter.reset(JOIN_ACTION, currentUserId);
        if (BillStatus.CANCELLED.equals(bill.getStatus())) throw new ConflictException("บิลนี้ถูกยกเลิกแล้ว");
        // ถ้ากดเข้าร่วมซ้ำพร้อมกัน unique index ux_bill_members_bill_user จะกันแถวซ้ำ (ได้ 409)
        if (!isOwner(bill, currentUserId) && !billMemberRepository.existsByBill_IdAndUser_Id(bill.getId(), currentUserId)) {
            addMember(bill, findUser(currentUserId), MemberRole.MEMBER);
        }
        return mapper.toResponse(bill);
    }

    @Override @Transactional(readOnly = true)
    public List<BillMemberResponse> members(Long billId, Long currentUserId) {
        findAccessible(billId, currentUserId);
        return billMemberRepository.findUsersByBillId(billId);
    }

    // ---------- สลิป/ใบเสร็จ ----------

    @Override
    public void saveSlip(Long billId, Long currentUserId, MultipartFile file) {
        Bill bill = findOwned(billId, currentUserId);
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("ไม่พบไฟล์ที่อัปโหลด");
        if (file.getSize() > MAX_SLIP_BYTES) throw new IllegalArgumentException("ไฟล์ใหญ่เกิน 8 MB");
        try {
            // ดูชนิดไฟล์จากเนื้อไฟล์จริง ไม่เชื่อ content type ที่ browser ส่งมา
            String ext = detectExtension(file);
            if (ext == null) throw new IllegalArgumentException("รองรับเฉพาะไฟล์ JPG, PNG, WEBP, GIF หรือ PDF");
            Path dir = slipDir();
            Files.createDirectories(dir);
            String name = UUID.randomUUID() + "." + ext;
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dir.resolve(name));
            }
            String old = bill.getSlipImage();
            bill.setSlipImage(name);
            billRepository.save(bill);
            deleteSlipFileAfterCommit(old);
        } catch (IOException e) {
            throw new IllegalStateException("บันทึกไฟล์สลิปไม่สำเร็จ", e);
        }
    }

    @Override @Transactional(readOnly = true)
    public SlipFile loadSlip(Long billId, Long currentUserId) {
        Bill bill = findAccessible(billId, currentUserId);
        String name = bill.getSlipImage();
        if (name == null || name.isBlank()) throw new ResourceNotFoundException("บิลนี้ยังไม่มีสลิป");
        Path dir = slipDir();
        Path file = dir.resolve(name).normalize();
        if (!file.startsWith(dir) || !Files.isRegularFile(file)) throw new ResourceNotFoundException("ไม่พบไฟล์สลิป");
        String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return new SlipFile(new FileSystemResource(file), SLIP_TYPES.getOrDefault(ext, "application/octet-stream"));
    }

    private static String detectExtension(MultipartFile file) throws IOException {
        byte[] h = new byte[12];
        int n;
        try (InputStream in = file.getInputStream()) {
            n = in.readNBytes(h, 0, h.length);
        }
        if (n >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) return "jpg";
        if (n >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') return "png";
        String head = new String(h, 0, n, StandardCharsets.ISO_8859_1);
        if (head.startsWith("GIF8")) return "gif";
        if (head.startsWith("%PDF")) return "pdf";
        if (n >= 12 && head.startsWith("RIFF") && head.startsWith("WEBP", 8)) return "webp";
        return null;
    }

    private Path slipDir() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    // ลบไฟล์เมื่อบันทึกฐานข้อมูลสำเร็จแล้วเท่านั้น (ถ้า rollback ไฟล์เดิมต้องยังอยู่)
    private void deleteSlipFileAfterCommit(String name) {
        if (name == null || name.isBlank()) return;
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteSlipFile(name);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteSlipFile(name);
            }
        });
    }

    private void deleteSlipFile(String name) {
        if (name == null || name.isBlank()) return;
        try {
            Path dir = slipDir();
            Path file = dir.resolve(name).normalize();
            if (file.startsWith(dir)) Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // ไฟล์เก่าลบไม่ได้ก็ไม่เป็นไร ข้อมูลในฐานข้อมูลถูกต้องแล้ว
        }
    }

    // ---------- helpers ----------

    private String resolveJoinCode(String requested) {
        String code = normalizeCode(requested);
        boolean valid = code.matches("SM-[A-Z0-9]{4,9}");
        return (!valid || billRepository.existsByJoinCode(code)) ? nextJoinCode() : code;
    }

    private static String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    private void addMember(Bill bill, User user, String role) {
        BillMember member = new BillMember();
        member.setBill(bill);
        member.setUser(user);
        member.setRole(role);
        billMemberRepository.save(member);
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
        String data = request.splitConfigData();
        config.setConfigData(data == null || data.isBlank() ? "{}" : data);
        bill.setSplitConfig(splitConfigRepository.save(config));
    }

    // splitConfigData มาจากหน้าเว็บและเก็บลงคอลัมน์ jsonb — ต้องเป็น JSON object/array ที่ถูกต้อง
    // ไม่อย่างนั้นฐานข้อมูลจะ error เป็น 500 แทนที่จะตอบ 400
    private void requireValidJson(String data) {
        if (data == null || data.isBlank()) return;
        try {
            var node = jsonMapper.readTree(data);
            if (node == null || !(node.isObject() || node.isArray())) {
                throw new IllegalArgumentException("splitConfigData ต้องเป็น JSON object หรือ array");
            }
        } catch (JacksonException e) {
            throw new IllegalArgumentException("splitConfigData ไม่ใช่ JSON ที่ถูกต้อง");
        }
    }

    private BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    private static boolean isOwner(Bill bill, Long userId) {
        return bill.getCreatedBy().getId().equals(userId);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private Bill find(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + id));
    }

    // เจ้าของบิลหรือสมาชิกของบิล
    private Bill findAccessible(Long id, Long currentUserId) {
        Bill bill = find(id);
        if (!isOwner(bill, currentUserId) && !billMemberRepository.existsByBill_IdAndUser_Id(id, currentUserId)) {
            throw new ForbiddenException("You do not have access to bill " + id);
        }
        return bill;
    }

    // เฉพาะเจ้าของบิล
    private Bill findOwned(Long id, Long currentUserId) {
        Bill bill = find(id);
        if (!isOwner(bill, currentUserId)) {
            throw new ForbiddenException("Only the bill owner can modify bill " + id);
        }
        return bill;
    }
}

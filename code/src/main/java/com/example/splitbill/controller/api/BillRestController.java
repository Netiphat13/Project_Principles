package com.example.splitbill.controller.api;

import com.example.splitbill.config.WebConfig;
import com.example.splitbill.dto.request.BillRequest;
import com.example.splitbill.dto.request.PaymentRosterRequest;
import com.example.splitbill.dto.response.BillMemberResponse;
import com.example.splitbill.dto.response.BillResponse;
import com.example.splitbill.dto.response.PaymentResponse;
import com.example.splitbill.service.BillService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/bills")
public class BillRestController {
    private final BillService service;
    public BillRestController(BillService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<BillResponse> create(@Valid @RequestBody BillRequest request,
                                               @SessionAttribute(WebConfig.USER_ID) Long userId) {
        BillResponse created = service.create(request, userId);
        return ResponseEntity.created(URI.create("/api/v1/bills/" + created.id())).body(created);
    }

    // สร้างบิลฉบับร่างตอนเข้าขั้นที่ 2 ของหน้าสร้างบิล เพื่อให้รหัสเข้าร่วมใช้ได้ทันที
    @PostMapping("/draft")
    public ResponseEntity<BillResponse> createDraft(@Valid @RequestBody BillRequest request,
                                                    @SessionAttribute(WebConfig.USER_ID) Long userId) {
        BillResponse created = service.createDraft(request, userId);
        return ResponseEntity.created(URI.create("/api/v1/bills/" + created.id())).body(created);
    }

    // บิลที่ผู้ใช้สร้าง + บิลที่เข้าร่วม เรียงจากใหม่ไปเก่า
    @GetMapping
    public Page<BillResponse> list(@SessionAttribute(WebConfig.USER_ID) Long userId,
                                   @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.findVisibleByUser(userId, pageable);
    }

    @GetMapping("/{id}")
    public BillResponse get(@PathVariable Long id, @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.getById(id, userId);
    }

    @PutMapping("/{id}")
    public BillResponse update(@PathVariable Long id, @Valid @RequestBody BillRequest request,
                               @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.update(id, request, userId);
    }

    @PatchMapping("/{id}/status")
    public BillResponse status(@PathVariable Long id, @RequestBody Map<String, String> body,
                               @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.updateStatus(id, body.get("status"), userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @SessionAttribute(WebConfig.USER_ID) Long userId) {
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ---------- รหัสเข้าร่วมบิล ----------

    @GetMapping("/join-code/next")
    public Map<String, String> nextJoinCode() {
        return Map.of("code", service.nextJoinCode());
    }

    @PostMapping("/join")
    public BillResponse join(@RequestBody Map<String, String> body, @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.join(body.get("code"), userId);
    }

    @GetMapping("/{id}/members")
    public List<BillMemberResponse> members(@PathVariable Long id, @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.members(id, userId);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable("userId") Long memberUserId,
                                             @SessionAttribute(WebConfig.USER_ID) Long userId) {
        service.removeMember(id, memberUserId, userId);
        return ResponseEntity.noContent().build();
    }

    // ---------- สลิป/ใบเสร็จ ----------

    @PostMapping(value = "/{id}/slip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Boolean> uploadSlip(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                                           @SessionAttribute(WebConfig.USER_ID) Long userId) {
        service.saveSlip(id, userId, file);
        return Map.of("ok", true);
    }

    @GetMapping("/{id}/slip")
    public ResponseEntity<Resource> slip(@PathVariable Long id, @SessionAttribute(WebConfig.USER_ID) Long userId) {
        BillService.SlipFile slip = service.loadSlip(id, userId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(slip.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
                .body(slip.resource());
    }

    // ---------- สถานะการจ่ายเงินและสลิปรายสมาชิก ----------

    @GetMapping("/{id}/payments")
    public List<PaymentResponse> payments(@PathVariable Long id, @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.payments(id, userId);
    }

    // เจ้าของบิลสร้าง/ปรับรายการชำระเงินจากรายชื่อและยอดที่ต้องจ่าย
    @PutMapping("/{id}/payments")
    public List<PaymentResponse> syncPayments(@PathVariable Long id, @Valid @RequestBody PaymentRosterRequest request,
                                              @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.syncPayments(id, userId, request.payments());
    }

    // เจ้าของบิลตรวจสลิปแล้วเปลี่ยนสถานะ: PENDING / PAID / OVERDUE
    @PatchMapping("/{id}/payments/{paymentId}/status")
    public PaymentResponse paymentStatus(@PathVariable Long id, @PathVariable Long paymentId,
                                         @RequestBody Map<String, String> body,
                                         @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.updatePaymentStatus(id, paymentId, body.get("status"), userId);
    }

    @PostMapping(value = "/{id}/payments/{paymentId}/slip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PaymentResponse uploadPaymentSlip(@PathVariable Long id, @PathVariable Long paymentId,
                                             @RequestParam("file") MultipartFile file,
                                             @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.savePaymentSlip(id, paymentId, userId, file);
    }

    @DeleteMapping("/{id}/payments/{paymentId}/slip")
    public PaymentResponse removePaymentSlip(@PathVariable Long id, @PathVariable Long paymentId,
                                             @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.removePaymentSlip(id, paymentId, userId);
    }

    @GetMapping("/{id}/payments/{paymentId}/slip")
    public ResponseEntity<Resource> paymentSlip(@PathVariable Long id, @PathVariable Long paymentId,
                                                @SessionAttribute(WebConfig.USER_ID) Long userId) {
        BillService.SlipFile slip = service.loadPaymentSlip(id, paymentId, userId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(slip.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
                .body(slip.resource());
    }
}

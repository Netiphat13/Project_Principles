package com.example.splitbill.service;

import com.example.splitbill.dto.request.BillRequest;
import com.example.splitbill.dto.request.PaymentShareRequest;
import com.example.splitbill.dto.response.BillMemberResponse;
import com.example.splitbill.dto.response.BillResponse;
import com.example.splitbill.dto.response.BillSummary;
import com.example.splitbill.dto.response.PaymentResponse;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BillService {
    BillResponse create(BillRequest request, Long currentUserId);

    // ดูได้เฉพาะเจ้าของบิลหรือสมาชิกของบิล
    BillResponse getById(Long id, Long currentUserId);

    Page<BillResponse> findByCreator(Long userId, Pageable pageable);

    // บิลที่ผู้ใช้สร้างเอง + บิลที่เข้าร่วม
    Page<BillResponse> findVisibleByUser(Long userId, Pageable pageable);

    // แก้ไข/เปลี่ยนสถานะ/ลบ ได้เฉพาะเจ้าของบิล
    BillResponse update(Long id, BillRequest request, Long currentUserId);

    BillResponse updateStatus(Long id, String status, Long currentUserId);

    void delete(Long id, Long currentUserId);

    BillSummary summarize(Long userId);

    // รหัสเข้าร่วมบิล
    String nextJoinCode();

    BillResponse join(String code, Long currentUserId);

    List<BillMemberResponse> members(Long billId, Long currentUserId);

    // สลิป/ใบเสร็จของบิล
    void saveSlip(Long billId, Long currentUserId, MultipartFile file);

    SlipFile loadSlip(Long billId, Long currentUserId);

    // ---------- สถานะการจ่ายเงินและสลิปรายสมาชิก ----------

    // เจ้าของบิลและสมาชิกดูสถานะของทุกคนได้
    List<PaymentResponse> payments(Long billId, Long currentUserId);

    // เจ้าของบิลสร้าง/ปรับรายการชำระเงินจากรายชื่อและยอดที่ต้องจ่าย (ใช้กับบิลเก่าที่ยังไม่มีรายการ)
    List<PaymentResponse> syncPayments(Long billId, Long currentUserId, List<PaymentShareRequest> roster);

    // เปลี่ยนสถานะการจ่ายของสมาชิก: เฉพาะเจ้าของบิล
    PaymentResponse updatePaymentStatus(Long billId, Long paymentId, String status, Long currentUserId);

    // ส่ง/ลบสลิปของตัวเอง: เฉพาะสมาชิกคนนั้น
    PaymentResponse savePaymentSlip(Long billId, Long paymentId, Long currentUserId, MultipartFile file);

    PaymentResponse removePaymentSlip(Long billId, Long paymentId, Long currentUserId);

    // ดูสลิปได้เฉพาะเจ้าของบิลและเจ้าของสลิป
    SlipFile loadPaymentSlip(Long billId, Long paymentId, Long currentUserId);

    record SlipFile(Resource resource, String contentType) {}
}

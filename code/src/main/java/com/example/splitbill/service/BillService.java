package com.example.splitbill.service;

import com.example.splitbill.dto.request.BillRequest;
import com.example.splitbill.dto.response.BillMemberResponse;
import com.example.splitbill.dto.response.BillResponse;
import com.example.splitbill.dto.response.BillSummary;
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

    record SlipFile(Resource resource, String contentType) {}
}

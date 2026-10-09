package com.example.splitbill.service;

import com.example.splitbill.dto.request.GroupRequest;
import com.example.splitbill.dto.response.GroupResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupService {
    GroupResponse create(GroupRequest request, Long currentUserId);

    // ดูได้เฉพาะเจ้าของกลุ่มหรือสมาชิกในกลุ่ม
    GroupResponse getById(Long id, Long currentUserId);

    Page<GroupResponse> findByCreator(Long userId, Pageable pageable);

    // แก้ไข/ลบได้เฉพาะเจ้าของกลุ่ม
    GroupResponse update(Long id, GroupRequest request, Long currentUserId);

    void delete(Long id, Long currentUserId);
}

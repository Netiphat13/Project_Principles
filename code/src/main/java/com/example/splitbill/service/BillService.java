package com.example.splitbill.service;

import com.example.splitbill.dto.request.BillRequest;
import com.example.splitbill.dto.response.BillResponse;
import com.example.splitbill.dto.response.BillSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BillService {
    BillResponse create(BillRequest request, Long currentUserId);
    BillResponse getById(Long id, Long currentUserId);
    Page<BillResponse> findByCreator(Long userId, Pageable pageable);
    BillResponse update(Long id, BillRequest request, Long currentUserId);
    void delete(Long id, Long currentUserId);
    BillSummary summarize(Long userId);
}

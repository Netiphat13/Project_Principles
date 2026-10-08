package com.example.splitbill.service;

import com.example.splitbill.dto.request.BillRequest;
import com.example.splitbill.dto.response.BillResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BillService {
    BillResponse create(BillRequest request);
    BillResponse getById(Long id);
    Page<BillResponse> findAll(Pageable pageable);
    Page<BillResponse> findByCreator(Long userId, Pageable pageable);
    BillResponse update(Long id, BillRequest request);

    BillResponse updateStatus(Long id, String status);
    void delete(Long id);
}

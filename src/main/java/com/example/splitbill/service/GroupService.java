package com.example.splitbill.service;

import com.example.splitbill.dto.request.GroupRequest;
import com.example.splitbill.dto.response.GroupResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupService {
    GroupResponse create(GroupRequest request);

    GroupResponse getById(Long id);

    Page<GroupResponse> findAll(Pageable pageable);

    Page<GroupResponse> findByCreator(Long userId, Pageable pageable);

    GroupResponse update(Long id, GroupRequest request);

    void delete(Long id);
}
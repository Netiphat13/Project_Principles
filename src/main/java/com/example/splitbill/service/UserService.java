package com.example.splitbill.service;

import com.example.splitbill.dto.request.UserRequest;
import com.example.splitbill.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse create(UserRequest request);
    UserResponse getById(Long id);
    Page<UserResponse> findAll(Pageable pageable);
    UserResponse update(Long id, UserRequest request);
    void delete(Long id);
    UserResponse authenticate(String email, String password);
}

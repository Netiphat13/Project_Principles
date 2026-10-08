package com.example.splitbill.dto.response;

public record ProfileResponse(Long userId, String displayName, String avatarUrl,
                              String phone, String bio) {}

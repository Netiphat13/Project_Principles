package com.example.splitbill.dto.response;

public record SessionResponse(Long id, String username, String email,
                              String phone, String bio, Boolean notificationEnabled) {}

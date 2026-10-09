package com.example.splitbill.dto.response;

import java.time.LocalDateTime;

public record GroupResponse(Long id, String name, String description,
                            Long createdById, String createdByName,
                            int memberCount, LocalDateTime createdAt) {}

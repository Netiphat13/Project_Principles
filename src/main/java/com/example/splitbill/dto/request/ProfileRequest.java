package com.example.splitbill.dto.request;

import jakarta.validation.constraints.Size;

public record ProfileRequest(
        @Size(max = 30) String phone,
        @Size(max = 120) String bio
) {}

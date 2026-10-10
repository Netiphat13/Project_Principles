package com.example.splitbill.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserSettingRequest(@NotNull Boolean notificationEnabled) {}

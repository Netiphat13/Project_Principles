package com.example.splitbill.dto.request;

import jakarta.validation.constraints.NotNull;

public record InvitationRequest(@NotNull Long inviteeId) {}

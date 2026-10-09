package com.example.splitbill.dto.response;

import java.time.LocalDateTime;

public record InvitationResponse(Long id, Long billId, String billName,
                                 Long inviterId, String inviterName,
                                 Long inviteeId, String inviteeName,
                                 String status, LocalDateTime createdAt) {}

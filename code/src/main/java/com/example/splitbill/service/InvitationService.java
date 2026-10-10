package com.example.splitbill.service;

import com.example.splitbill.dto.response.InvitationResponse;

import java.util.List;

public interface InvitationService {
    // สมาชิกของบิลเชิญผู้ใช้คนอื่นเข้าบิล
    InvitationResponse invite(Long billId, Long inviterId, Long inviteeId);

    // คำเชิญที่ผู้ใช้ได้รับและยังไม่ได้ตอบ
    List<InvitationResponse> myInvitations(Long userId);

    // คำเชิญที่ยังรอตอบของบิล (ดูได้เฉพาะสมาชิกบิล)
    List<InvitationResponse> pendingForBill(Long billId, Long requesterId);

    void accept(Long invitationId, Long userId);

    void reject(Long invitationId, Long userId);
}

package com.example.splitbill.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.splitbill.model.Invitation;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    // คำเชิญที่ผู้ใช้ได้รับ (ยังไม่ตอบ)
    List<Invitation> findByInvitee_IdAndStatusOrderByCreatedAtDesc(Long inviteeId, String status);

    // คำเชิญที่ยังรอตอบของบิลหนึ่ง
    List<Invitation> findByBill_IdAndStatusOrderByCreatedAtDesc(Long billId, String status);

    boolean existsByBill_IdAndInvitee_IdAndStatus(Long billId, Long inviteeId, String status);
}

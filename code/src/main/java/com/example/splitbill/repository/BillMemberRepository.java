
package com.example.splitbill.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.splitbill.model.BillMember;

public interface BillMemberRepository extends JpaRepository<BillMember, Long> {

    // ดึงสมาชิกทั้งหมดของบิล เรียงตามเวลาที่เข้าร่วม
    List<BillMember> findByBill_IdOrderByJoinedAtAsc(Long billId);

    // ค้นหาสมาชิกจาก User ID
    List<BillMember> findByUser_Id(Long userId);

    // ค้นหาสมาชิกจาก Guest Token
    Optional<BillMember> findByGuestToken(String guestToken);
}
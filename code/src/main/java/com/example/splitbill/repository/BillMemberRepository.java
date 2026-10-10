
package com.example.splitbill.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.splitbill.dto.response.BillMemberResponse;
import com.example.splitbill.model.BillMember;

public interface BillMemberRepository extends JpaRepository<BillMember, Long> {

    // ดึงสมาชิกทั้งหมดของบิล เรียงตามเวลาที่เข้าร่วม
    List<BillMember> findByBill_IdOrderByJoinedAtAsc(Long billId);

    // ค้นหาสมาชิกจาก User ID
    List<BillMember> findByUser_Id(Long userId);

    // ค้นหาสมาชิกจาก Guest Token
    Optional<BillMember> findByGuestToken(String guestToken);

    // เช็กว่าผู้ใช้เป็นสมาชิกของบิลหรือยัง
    boolean existsByBill_IdAndUser_Id(Long billId, Long userId);

    Optional<BillMember> findByBill_IdAndUser_Id(Long billId, Long userId);

    // รายชื่อสมาชิกที่มีบัญชีของบิล (สำหรับแสดงในหน้ารายละเอียดบิล)
    @Query("SELECT new com.example.splitbill.dto.response.BillMemberResponse("
            + "m.id, m.user.id, m.user.username, m.user.email, m.role) "
            + "FROM BillMember m WHERE m.bill.id = :billId AND m.user IS NOT NULL ORDER BY m.joinedAt")
    List<BillMemberResponse> findUsersByBillId(@Param("billId") Long billId);
}
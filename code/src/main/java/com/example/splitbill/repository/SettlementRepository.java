package com.example.splitbill.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.splitbill.model.Settlement;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    // ดึงรายการโอนเงินทั้งหมดของบิล
    List<Settlement> findByBill_Id(Long billId);

    // ดึงรายการโอนเงินที่สมาชิกต้องจ่าย
    List<Settlement> findByFromMember_Id(Long memberId);

    // ค้นหารายการโอนเงินตามสถานะ
    List<Settlement> findByStatus(String status);
}


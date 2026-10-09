
package com.example.splitbill.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.splitbill.model.Bill;

public interface BillRepository extends JpaRepository<Bill, Long> {

    // ค้นหาบิลที่สร้างโดย User คนหนึ่ง
    List<Bill> findByCreatedBy_Id(Long userId);

    // ค้นหาบิลของ User และเรียงจากบิลใหม่ไปเก่า
    List<Bill> findByCreatedBy_IdOrderByCreatedAtDesc(Long userId);

    // ค้นหาบิลตามสถานะ
    List<Bill> findByStatus(String status);

    // ค้นหาบิลตามช่วงวันที่
    List<Bill> findByBillDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );
}
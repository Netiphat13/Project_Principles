package com.example.splitbill.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.splitbill.model.BillItem;

public interface BillItemRepository extends JpaRepository<BillItem, Long> {

    // ดึงรายการอาหารทั้งหมดของบิล
    List<BillItem> findByBill_Id(Long billId);

    // ดึงรายการอาหารของบิล เรียงตามชื่อ
    List<BillItem> findByBill_IdOrderByNameAsc(Long billId);

    // ค้นหารายการอาหารตามชื่อ
    List<BillItem> findByNameContainingIgnoreCase(String name);

    // ดึงรายการอาหารของบิล เรียงตามราคารวมจากมากไปน้อย
    List<BillItem> findByBill_IdOrderByTotalPriceDesc(Long billId);
}

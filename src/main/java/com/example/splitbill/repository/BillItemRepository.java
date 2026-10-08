package com.example.splitbill.repository;

import com.example.splitbill.model.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BillItemRepository extends JpaRepository<BillItem, Long> {
    @Modifying
    @Query(value = "DELETE FROM bill_items WHERE bill_id = :billId", nativeQuery = true)
    int deleteAllByBillIdNative(@Param("billId") Long billId);
}

package com.example.splitbill.repository;

import com.example.splitbill.model.ItemAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemAssignmentRepository extends JpaRepository<ItemAssignment, Long> {
    @Modifying
    @Query(value = "DELETE FROM item_assignments WHERE bill_item_id IN (SELECT id FROM bill_items WHERE bill_id = :billId)", nativeQuery = true)
    int deleteAllByBillIdNative(@Param("billId") Long billId);
}

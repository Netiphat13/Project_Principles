package com.example.splitbill.repository;

import com.example.splitbill.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Modifying
    @Query(value = "DELETE FROM payments WHERE bill_member_id IN (SELECT id FROM bill_members WHERE bill_id = :billId)", nativeQuery = true)
    int deleteAllByBillIdNative(@Param("billId") Long billId);
}

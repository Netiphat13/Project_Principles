package com.example.splitbill.repository;

import com.example.splitbill.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    @Modifying
    @Query(value = "DELETE FROM settlements WHERE bill_id = :billId", nativeQuery = true)
    int deleteAllByBillIdNative(@Param("billId") Long billId);
}

package com.example.splitbill.repository;

import com.example.splitbill.model.SplitConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface SplitConfigRepository extends JpaRepository<SplitConfig, Long> {
    Optional<SplitConfig> findByBillId(Long billId);

    @Modifying
    @Query(value = "DELETE FROM split_configs WHERE bill_id = :billId", nativeQuery = true)
    int deleteAllByBillIdNative(@Param("billId") Long billId);
}

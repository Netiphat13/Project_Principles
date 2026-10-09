package com.example.splitbill.repository;

import com.example.splitbill.model.SplitConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SplitConfigRepository extends JpaRepository<SplitConfig, Long> {
    Optional<SplitConfig> findByBillId(Long billId);
}

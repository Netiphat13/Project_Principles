package com.example.splitbill.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.splitbill.model.SplitConfig;

public interface SplitConfigRepository extends JpaRepository<SplitConfig, Long> {

    // ค้นหาการตั้งค่าการแบ่งเงินจาก Bill ID
    Optional<SplitConfig> findByBill_Id(Long billId);

    // ค้นหาการตั้งค่าตามวิธีแบ่งเงิน
    java.util.List<SplitConfig> findBySplitMethod(String splitMethod);
}

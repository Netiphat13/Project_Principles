package com.example.splitbill.repository;

import com.example.splitbill.model.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Long> {
    Page<Bill> findByCreatedById(Long userId, Pageable pageable);
}

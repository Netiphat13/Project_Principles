package com.example.splitbill.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.splitbill.model.BillPayment;

public interface BillPaymentRepository extends JpaRepository<BillPayment, Long> {

    // สถานะการจ่ายของทุกคนในบิล
    List<BillPayment> findByBill_IdOrderByIdAsc(Long billId);

    // ใช้ตรวจว่ารายการชำระเงินนี้เป็นของบิลที่ขอจริง
    Optional<BillPayment> findByIdAndBill_Id(Long id, Long billId);
}

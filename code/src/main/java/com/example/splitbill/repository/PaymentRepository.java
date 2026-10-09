package com.example.splitbill.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.splitbill.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // ดึงประวัติการชำระเงินของสมาชิก
    List<Payment> findByBillMember_Id(Long billMemberId);

    // ค้นหาการชำระเงินตามสถานะ
    List<Payment> findByStatus(String status);

    // ดึงประวัติการชำระเงินของสมาชิก เรียงจากใหม่ไปเก่า
    List<Payment> findByBillMember_IdOrderByPaidAtDesc(Long billMemberId);

    // ค้นหาการชำระเงินตามวิธีชำระ
    List<Payment> findByPaymentMethod(String paymentMethod);
}


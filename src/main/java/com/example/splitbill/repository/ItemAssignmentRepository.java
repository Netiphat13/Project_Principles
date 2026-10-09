package com.example.splitbill.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.splitbill.model.ItemAssignment;

public interface ItemAssignmentRepository extends JpaRepository<ItemAssignment, Long> {

    // ดึงสมาชิกทั้งหมดที่ถูกกำหนดให้รับผิดชอบรายการอาหารหนึ่ง
    List<ItemAssignment> findByBillItem_Id(Long billItemId);

    // ดึงรายการอาหารทั้งหมดที่สมาชิกคนหนึ่งต้องจ่าย
    List<ItemAssignment> findByBillMember_Id(Long billMemberId);

    // ดึงการแบ่งค่าอาหารของสมาชิกในรายการอาหารหนึ่ง
    List<ItemAssignment> findByBillItem_IdAndBillMember_Id(
            Long billItemId,
            Long billMemberId
    );
}




package com.example.splitbill.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.splitbill.model.Bill;

public interface BillRepository extends JpaRepository<Bill, Long> {

    // ค้นหาบิลที่สร้างโดย User คนหนึ่ง
    List<Bill> findByCreatedBy_Id(Long userId);

    // ค้นหาบิลของ User และเรียงจากบิลใหม่ไปเก่า
    List<Bill> findByCreatedBy_IdOrderByCreatedAtDesc(Long userId);

    // ค้นหาบิลตามสถานะ
    List<Bill> findByStatus(String status);

    @Query("SELECT COUNT(b) FROM Bill b")
    long countAllBills();

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b")
    java.math.BigDecimal sumTotalAmount();

    // ค้นหาบิลตามช่วงวันที่
    List<Bill> findByBillDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    // ลบบิลด้วย SQL ตรง ๆ ให้ฐานข้อมูลลบรายการอาหาร/สมาชิก/วิธีหาร/คำเชิญตาม (ON DELETE CASCADE)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Bill b WHERE b.id = :id")
    void deleteBillById(@Param("id") Long id);

    // ค้นหาบิลจากรหัสเข้าร่วม (ใช้ตอนเพื่อนกรอกรหัส)
    java.util.Optional<Bill> findByJoinCode(String joinCode);

    boolean existsByJoinCode(String joinCode);

    // บิลที่ผู้ใช้เห็นได้ = บิลที่ตัวเองสร้าง + บิลที่เข้าร่วมเป็นสมาชิก
    @Query("SELECT b FROM Bill b WHERE b.createdBy.id = :userId "
            + "OR EXISTS (SELECT m FROM BillMember m WHERE m.bill = b AND m.user.id = :userId)")
    Page<Bill> findVisibleByUser(@Param("userId") Long userId, Pageable pageable);

    // ดึงบิลของ User แบบแบ่งหน้า (ใช้ใน BillServiceImpl)
    Page<Bill> findByCreatedById(Long userId, Pageable pageable);

    // สถิติของ User (ใช้ในหน้า /stats) — คำนวณใน DB ไม่ต้องโหลดบิลทั้งหมดมา
    long countByCreatedById(Long userId);

    // ไม่นับบิลฉบับร่าง (ยังสร้างไม่เสร็จ ยอดเป็น 0) เพื่อไม่ให้ค่าเฉลี่ยเพี้ยน
    @Query("SELECT COUNT(b) FROM Bill b WHERE b.createdBy.id = :userId AND (b.status IS NULL OR b.status <> 'DRAFT')")
    long countNonDraftByCreatedById(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.createdBy.id = :userId")
    java.math.BigDecimal sumTotalAmountByCreatedById(@Param("userId") Long userId);
}

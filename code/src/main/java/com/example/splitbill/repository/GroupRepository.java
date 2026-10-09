package com.example.splitbill.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.splitbill.model.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByInviteCode(String inviteCode);
    
    boolean existsByInviteCode(String inviteCode);

    // ค้นหากลุ่มที่สร้างโดย User คนหนึ่ง
    List<Group> findByCreatedBy_Id(Long userId);

    // ค้นหากลุ่มจากชื่อ โดยไม่สนตัวพิมพ์เล็กหรือใหญ่
    List<Group> findByNameContainingIgnoreCase(String name);

    // ดึงกลุ่มของ User แบบแบ่งหน้า (ใช้ใน GroupServiceImpl)
    Page<Group> findByCreatedById(Long userId, Pageable pageable);
}

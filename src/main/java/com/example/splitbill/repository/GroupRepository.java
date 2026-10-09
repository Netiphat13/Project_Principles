package com.example.splitbill.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.splitbill.model.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {

    // ค้นหากลุ่มที่สร้างโดย User คนหนึ่ง
    List<Group> findByCreatedBy_Id(Long userId);

    // ค้นหากลุ่มจากชื่อ โดยไม่สนตัวพิมพ์เล็กหรือใหญ่
    List<Group> findByNameContainingIgnoreCase(String name);
}

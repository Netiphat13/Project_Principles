package com.example.splitbill.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.splitbill.model.GroupMember;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    // ค้นหาสมาชิกทั้งหมดในกลุ่ม
    List<GroupMember> findByGroup_Id(Long groupId);

    boolean existsByGroup_IdAndUser_Id(Long groupId, Long userId);

    // ค้นหากลุ่มทั้งหมดที่ User เข้าร่วม
    List<GroupMember> findByUser_Id(Long userId);
}

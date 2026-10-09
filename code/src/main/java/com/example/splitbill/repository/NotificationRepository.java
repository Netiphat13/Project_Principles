package com.example.splitbill.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.splitbill.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // ดึงการแจ้งเตือนทั้งหมดของ User
    List<Notification> findByUser_Id(Long userId);

    // ดึงการแจ้งเตือนของ User เรียงจากใหม่ไปเก่า
    List<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId);
}


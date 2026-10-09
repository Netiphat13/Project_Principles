package com.example.splitbill.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.splitbill.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}

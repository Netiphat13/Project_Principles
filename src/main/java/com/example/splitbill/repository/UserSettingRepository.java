package com.example.splitbill.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.splitbill.model.UserSetting;

public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {

    // ค้นหาการตั้งค่าจาก User ID
    Optional<UserSetting> findByUser_Id(Long userId);
}


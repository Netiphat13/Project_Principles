package com.example.splitbill.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.splitbill.model.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    // ค้นหาโปรไฟล์จาก User ID
    Optional<Profile> findByUser_Id(Long userId);
}


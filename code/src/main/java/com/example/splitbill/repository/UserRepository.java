package com.example.splitbill.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.splitbill.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // ค้นหาผู้ใช้ด้วยอีเมล
    Optional<User> findByEmail(String email);

    // ค้นหาผู้ใช้ด้วยชื่อผู้ใช้
    Optional<User> findByUsername(String username);

    // เช็กว่าอีเมลถูกใช้แล้วหรือยัง (ใช้ใน UserServiceImpl)
    boolean existsByEmail(String email);
}

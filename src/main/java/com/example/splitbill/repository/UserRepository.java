package com.example.splitbill.repository;

import com.example.splitbill.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
package com.example.splitbill.repository;

import com.example.splitbill.model.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Page<Group> findByCreatedById(Long userId, Pageable pageable);
}

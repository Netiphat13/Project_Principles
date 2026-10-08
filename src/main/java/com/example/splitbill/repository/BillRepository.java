package com.example.splitbill.repository;

import com.example.splitbill.model.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
    Page<Bill> findByCreatedById(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy", "items", "splitConfig"})
    @Query("select b from Bill b where b.id = :id")
    Optional<Bill> findDetailById(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM bills WHERE id = :id", nativeQuery = true)
    int deleteByIdNative(@Param("id") Long id);
}

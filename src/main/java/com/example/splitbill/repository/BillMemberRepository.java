package com.example.splitbill.repository;

import com.example.splitbill.model.BillMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillMemberRepository extends JpaRepository<BillMember, Long> {
}

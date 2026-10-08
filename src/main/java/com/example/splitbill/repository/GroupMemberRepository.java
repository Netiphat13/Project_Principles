package com.example.splitbill.repository;

import com.example.splitbill.model.GroupMember;
import com.example.splitbill.dto.response.UserResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    @Query("select new com.example.splitbill.dto.response.UserResponse(gm.user.id, gm.user.username, gm.user.email) from GroupMember gm where gm.group.id = :groupId order by gm.joinedAt")
    List<UserResponse> findUsersByGroupId(Long groupId);
}

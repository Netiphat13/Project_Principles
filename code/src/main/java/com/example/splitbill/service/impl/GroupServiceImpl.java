package com.example.splitbill.service.impl;

import com.example.splitbill.dto.request.GroupRequest;
import com.example.splitbill.dto.response.GroupResponse;
import com.example.splitbill.exception.ForbiddenException;
import com.example.splitbill.exception.ResourceNotFoundException;
import com.example.splitbill.mapper.GroupMapper;
import com.example.splitbill.model.Group;
import com.example.splitbill.repository.GroupMemberRepository;
import com.example.splitbill.repository.GroupRepository;
import com.example.splitbill.repository.UserRepository;
import com.example.splitbill.service.GroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@Transactional
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupMapper mapper;
    private final SecureRandom random = new SecureRandom();

    private static final String INVITE_CODE_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_CODE_LENGTH = 8;

    public GroupServiceImpl(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository,
                            UserRepository userRepository, GroupMapper mapper) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public GroupResponse create(GroupRequest request, Long currentUserId) {
        Group group = new Group();
        group.setName(request.name());
        group.setDescription(request.description());
        group.setCreatedBy(userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId)));
        group.setInviteCode(generateUniqueInviteCode());
        return mapper.toResponse(groupRepository.save(group));
    }

    @Override @Transactional(readOnly = true)
    public GroupResponse getById(Long id, Long currentUserId) {
        Group group = find(id);
        boolean isOwner = group.getCreatedBy().getId().equals(currentUserId);
        if (!isOwner && !groupMemberRepository.existsByGroup_IdAndUser_Id(id, currentUserId)) {
            throw new ForbiddenException("You do not have access to group " + id);
        }
        return mapper.toResponse(group);
    }

    @Override @Transactional(readOnly = true)
    public Page<GroupResponse> findByCreator(Long userId, Pageable pageable) {
        return groupRepository.findByCreatedById(userId, pageable).map(mapper::toResponse);
    }

    @Override
    public GroupResponse update(Long id, GroupRequest request, Long currentUserId) {
        Group group = findOwned(id, currentUserId);
        group.setName(request.name());
        group.setDescription(request.description());
        return mapper.toResponse(groupRepository.save(group));
    }

    @Override
    public void delete(Long id, Long currentUserId) {
        groupRepository.delete(findOwned(id, currentUserId));
    }

    // สร้างรหัสเชิญเข้ากลุ่มที่ไม่ซ้ำกับกลุ่มอื่น
    private String generateUniqueInviteCode() {
        String code;
        do {
            StringBuilder builder = new StringBuilder(INVITE_CODE_LENGTH);
            for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
                builder.append(INVITE_CODE_CHARACTERS.charAt(random.nextInt(INVITE_CODE_CHARACTERS.length())));
            }
            code = builder.toString();
        } while (groupRepository.existsByInviteCode(code));
        return code;
    }

    private Group find(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
    }

    private Group findOwned(Long id, Long currentUserId) {
        Group group = find(id);
        if (!group.getCreatedBy().getId().equals(currentUserId)) {
            throw new ForbiddenException("Only the group owner can modify group " + id);
        }
        return group;
    }
}

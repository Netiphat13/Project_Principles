package com.example.splitbill.service.impl;

import com.example.splitbill.dto.request.GroupRequest;
import com.example.splitbill.dto.response.GroupResponse;
import com.example.splitbill.exception.ResourceNotFoundException;
import com.example.splitbill.mapper.GroupMapper;
import com.example.splitbill.model.Group;
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
    private final UserRepository userRepository;
    private final GroupMapper mapper;
    private final SecureRandom random = new SecureRandom();

    private static final String INVITE_CODE_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_CODE_LENGTH = 8;

    public GroupServiceImpl(GroupRepository groupRepository, UserRepository userRepository, GroupMapper mapper) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public GroupResponse create(GroupRequest request) {
        Group group = new Group();
        group.setName(request.name());
        group.setDescription(request.description());
        group.setCreatedBy(userRepository.findById(request.createdById())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.createdById())));
        return mapper.toResponse(saveGroupWithInviteCode(group));
    }

    @Override @Transactional(readOnly = true)
    public GroupResponse getById(Long id) { return mapper.toResponse(find(id)); }

    @Override @Transactional(readOnly = true)
    public Page<GroupResponse> findAll(Pageable pageable) {
        return groupRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Override @Transactional(readOnly = true)
    public Page<GroupResponse> findByCreator(Long userId, Pageable pageable) {
        return groupRepository.findByCreatedById(userId, pageable).map(mapper::toResponse);
    }

    @Override
    public GroupResponse update(Long id, GroupRequest request) {
        Group group = find(id);
        group.setName(request.name());
        group.setDescription(request.description());
        if (request.createdById() != null) {
            group.setCreatedBy(userRepository.findById(request.createdById())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.createdById())));
        }
        return mapper.toResponse(groupRepository.save(group));
    }

    @Override
    public void delete(Long id) {
        if (!groupRepository.existsById(id)) throw new ResourceNotFoundException("Group not found: " + id);
        groupRepository.deleteById(id);
    }

    @Override @Transactional(readOnly = true)
    public String generateUniqueInviteCode() {
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

    @Override
    public Group saveGroupWithInviteCode(Group group) {
        group.setInviteCode(generateUniqueInviteCode());
        return groupRepository.save(group);
    }

    private Group find(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
    }
}

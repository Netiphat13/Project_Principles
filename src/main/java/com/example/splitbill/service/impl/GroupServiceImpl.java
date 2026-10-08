package com.example.splitbill.service.impl;

import com.example.splitbill.dto.request.GroupRequest;
import com.example.splitbill.dto.response.GroupResponse;
import com.example.splitbill.exception.ResourceNotFoundException;
import com.example.splitbill.mapper.GroupMapper;
import com.example.splitbill.model.Group;
import com.example.splitbill.repository.GroupRepository;
import com.example.splitbill.repository.GroupMemberRepository;
import com.example.splitbill.model.GroupMember;
import com.example.splitbill.repository.UserRepository;
import com.example.splitbill.service.GroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMapper mapper;

    public GroupServiceImpl(GroupRepository groupRepository, UserRepository userRepository, GroupMemberRepository groupMemberRepository, GroupMapper mapper) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.mapper = mapper;
    }

    @Override
    public GroupResponse create(GroupRequest request) {
        Group group = new Group();
        group.setName(request.name());
        group.setDescription(request.description());
        var creator = userRepository.findById(request.createdById())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.createdById()));
        group.setCreatedBy(creator);
        Group saved = groupRepository.save(group);
        GroupMember owner = new GroupMember();
        owner.setGroup(saved);
        owner.setUser(creator);
        owner.setRole("OWNER");
        saved.getMembers().add(owner);
        groupMemberRepository.save(owner);
        return mapper.toResponse(saved);
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

    private Group find(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
    }
}

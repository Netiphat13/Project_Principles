package com.example.splitbill.controller.api;

import com.example.splitbill.dto.request.GroupRequest;
import com.example.splitbill.dto.response.GroupResponse;
import com.example.splitbill.service.GroupService;
import com.example.splitbill.repository.GroupMemberRepository;
import com.example.splitbill.dto.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import com.example.splitbill.exception.ConflictException;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupRestController {
    private final GroupService service;
    private final GroupMemberRepository groupMemberRepository;
    public GroupRestController(GroupService service, GroupMemberRepository groupMemberRepository) { this.service = service; this.groupMemberRepository = groupMemberRepository; }

    @PostMapping
    public ResponseEntity<GroupResponse> create(@Valid @RequestBody GroupRequest request, HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");
        if (!sessionUserId.equals(request.createdById())) throw new ConflictException("Group creator must be the logged-in user");
        GroupResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/groups/" + created.id())).body(created);
    }
    @GetMapping("/{id}")
    public GroupResponse get(@PathVariable Long id) { return service.getById(id); }
    @GetMapping
    public Page<GroupResponse> list(@RequestParam(required = false) Long createdById,
                                    @PageableDefault(size = 12, sort = "createdAt") Pageable pageable,
                                    HttpSession session) {
        Long sessionUserId = (Long) session.getAttribute("userId");
        Long ownerId = createdById == null ? sessionUserId : createdById;
        return service.findByCreator(ownerId, pageable);
    }
    @GetMapping("/{id}/members")
    public java.util.List<UserResponse> members(@PathVariable Long id) {
        service.getById(id);
        return groupMemberRepository.findUsersByGroupId(id);
    }

    @PutMapping("/{id}")
    public GroupResponse update(@PathVariable Long id, @Valid @RequestBody GroupRequest request) {
        return service.update(id, request);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

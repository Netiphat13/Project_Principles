package com.example.splitbill.controller.api;

import com.example.splitbill.config.WebConfig;
import com.example.splitbill.dto.request.GroupRequest;
import com.example.splitbill.dto.response.GroupResponse;
import com.example.splitbill.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupRestController {
    private final GroupService service;
    public GroupRestController(GroupService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<GroupResponse> create(@Valid @RequestBody GroupRequest request,
                                                @SessionAttribute(WebConfig.USER_ID) Long userId) {
        GroupResponse created = service.create(request, userId);
        return ResponseEntity.created(URI.create("/api/v1/groups/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public GroupResponse get(@PathVariable Long id, @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.getById(id, userId);
    }

    // กลุ่มที่ผู้ใช้ที่ล็อกอินอยู่เป็นคนสร้าง เรียงจากใหม่ไปเก่า
    @GetMapping
    public Page<GroupResponse> list(@SessionAttribute(WebConfig.USER_ID) Long userId,
                                    @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.findByCreator(userId, pageable);
    }

    @PutMapping("/{id}")
    public GroupResponse update(@PathVariable Long id, @Valid @RequestBody GroupRequest request,
                                @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.update(id, request, userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @SessionAttribute(WebConfig.USER_ID) Long userId) {
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}

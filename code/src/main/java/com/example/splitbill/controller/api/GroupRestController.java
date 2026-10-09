package com.example.splitbill.controller.api;

import com.example.splitbill.dto.request.GroupRequest;
import com.example.splitbill.dto.response.GroupResponse;
import com.example.splitbill.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<GroupResponse> create(@Valid @RequestBody GroupRequest request) {
        GroupResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/groups/" + created.id())).body(created);
    }
    @GetMapping("/{id}")
    public GroupResponse get(@PathVariable Long id) { return service.getById(id); }
    @GetMapping
    public Page<GroupResponse> list(@PageableDefault(size = 12, sort = "createdAt") Pageable pageable) {
        return service.findAll(pageable);
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

package com.example.splitbill.controller.api;

import com.example.splitbill.dto.request.UserRequest;
import com.example.splitbill.dto.response.UserResponse;
import com.example.splitbill.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {
    private final UserService service;
    public UserRestController(UserService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + created.id())).body(created);
    }
    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) { return service.getById(id); }
    @GetMapping
    public Page<UserResponse> list(@PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return service.findAll(pageable);
    }
    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return service.update(id, request);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

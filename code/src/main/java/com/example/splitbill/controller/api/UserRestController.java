package com.example.splitbill.controller.api;

import com.example.splitbill.config.WebConfig;
import com.example.splitbill.dto.request.UserRequest;
import com.example.splitbill.dto.response.UserResponse;
import com.example.splitbill.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {
    private final UserService service;
    public UserRestController(UserService service) { this.service = service; }

    // สมัครสมาชิก — endpoint เดียวที่เรียกได้โดยไม่ต้องล็อกอิน (ดู WebConfig)
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + created.id())).body(created);
    }

    @GetMapping("/me")
    public UserResponse me(@SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.getById(userId);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) { return service.getById(id); }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest request,
                               @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.update(id, request, userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @SessionAttribute(WebConfig.USER_ID) Long userId,
                                       HttpSession session) {
        service.delete(id, userId);
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}

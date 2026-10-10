package com.example.splitbill.controller.api;

import com.example.splitbill.config.WebConfig;
import com.example.splitbill.dto.request.ProfileRequest;
import com.example.splitbill.dto.request.UserRequest;
import com.example.splitbill.dto.request.UserSettingRequest;
import com.example.splitbill.dto.response.SessionResponse;
import com.example.splitbill.dto.response.UserResponse;
import com.example.splitbill.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.Map;

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

    // ผู้ใช้ที่ล็อกอินอยู่ พร้อมโปรไฟล์และการตั้งค่า
    @GetMapping("/me")
    public SessionResponse me(@SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.session(userId);
    }

    @PutMapping("/me/profile")
    public SessionResponse updateProfile(@Valid @RequestBody ProfileRequest request,
                                         @SessionAttribute(WebConfig.USER_ID) Long userId,
                                         HttpSession session) {
        SessionResponse updated = service.updateProfile(userId, request);
        session.setAttribute("username", updated.username());
        return updated;
    }

    @PatchMapping("/me/settings/notification")
    public Map<String, Boolean> setNotification(@Valid @RequestBody UserSettingRequest request,
                                                @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return Map.of("notificationEnabled", service.setNotificationEnabled(userId, request.notificationEnabled()));
    }

    // หาเพื่อนจากอีเมลแบบตรงตัว เพื่อเชิญเข้าบิล
    @GetMapping("/lookup")
    public UserResponse lookup(@RequestParam String email, @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.findByEmail(email, userId);
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

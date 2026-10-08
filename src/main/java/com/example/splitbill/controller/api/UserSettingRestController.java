package com.example.splitbill.controller.api;

import com.example.splitbill.dto.request.UserSettingRequest;
import com.example.splitbill.model.UserSetting;
import com.example.splitbill.repository.UserRepository;
import com.example.splitbill.repository.UserSettingRepository;
import com.example.splitbill.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/settings")
public class UserSettingRestController {
    private final UserSettingRepository repository;
    private final UserRepository userRepository;

    public UserSettingRestController(UserSettingRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @PatchMapping("/notification")
    public boolean setNotification(@PathVariable Long userId, @Valid @RequestBody UserSettingRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        UserSetting setting = repository.findByUserId(userId).orElseGet(UserSetting::new);
        setting.setUser(user);
        setting.setNotificationEnabled(request.notificationEnabled());
        if (setting.getLanguage() == null) setting.setLanguage("th");
        if (setting.getCurrency() == null) setting.setCurrency("THB");
        repository.save(setting);
        return setting.getNotificationEnabled();
    }
}

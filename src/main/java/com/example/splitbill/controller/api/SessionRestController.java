package com.example.splitbill.controller.api;

import com.example.splitbill.dto.response.SessionResponse;
import com.example.splitbill.exception.ResourceNotFoundException;
import com.example.splitbill.model.Profile;
import com.example.splitbill.model.UserSetting;
import com.example.splitbill.repository.ProfileRepository;
import com.example.splitbill.repository.UserRepository;
import com.example.splitbill.repository.UserSettingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/session")
public class SessionRestController {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserSettingRepository settingRepository;

    public SessionRestController(UserRepository userRepository, ProfileRepository profileRepository,
                                 UserSettingRepository settingRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.settingRepository = settingRepository;
    }

    @GetMapping("/me")
    public SessionResponse me(HttpSession session) {
        Object value = session.getAttribute("userId");
        if (!(value instanceof Long userId)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Profile profile = profileRepository.findByUserId(userId).orElse(null);
        UserSetting setting = settingRepository.findByUserId(userId).orElse(null);
        return new SessionResponse(user.getId(), user.getUsername(), user.getEmail(),
                profile == null ? null : profile.getPhone(),
                profile == null ? null : profile.getBio(),
                setting == null || setting.getNotificationEnabled() == null ? Boolean.TRUE : setting.getNotificationEnabled());
    }
}

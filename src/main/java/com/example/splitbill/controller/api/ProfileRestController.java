package com.example.splitbill.controller.api;

import com.example.splitbill.dto.request.ProfileRequest;
import com.example.splitbill.dto.response.ProfileResponse;
import com.example.splitbill.exception.ResourceNotFoundException;
import com.example.splitbill.model.Profile;
import com.example.splitbill.repository.ProfileRepository;
import com.example.splitbill.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/profile")
public class ProfileRestController {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileRestController(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ProfileResponse get(@PathVariable Long userId) {
        return toResponse(profileRepository.findByUserId(userId).orElse(null), userId);
    }

    @PutMapping
    public ProfileResponse update(@PathVariable Long userId, @Valid @RequestBody ProfileRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Profile profile = profileRepository.findByUserId(userId).orElseGet(Profile::new);
        profile.setUser(user);
        profile.setPhone(request.phone());
        profile.setBio(request.bio());
        profile.setDisplayName(user.getUsername());
        return toResponse(profileRepository.save(profile), userId);
    }

    private ProfileResponse toResponse(Profile p, Long userId) {
        return p == null ? new ProfileResponse(userId, null, null, null, null)
                : new ProfileResponse(userId, p.getDisplayName(), p.getAvatarUrl(), p.getPhone(), p.getBio());
    }
}

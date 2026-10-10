package com.example.splitbill.service.impl;

import com.example.splitbill.dto.request.ProfileRequest;
import com.example.splitbill.dto.request.UserRequest;
import com.example.splitbill.dto.response.SessionResponse;
import com.example.splitbill.dto.response.UserResponse;
import com.example.splitbill.exception.ConflictException;
import com.example.splitbill.exception.ForbiddenException;
import com.example.splitbill.exception.InvalidCredentialsException;
import com.example.splitbill.exception.ResourceNotFoundException;
import com.example.splitbill.mapper.UserMapper;
import com.example.splitbill.model.Profile;
import com.example.splitbill.model.User;
import com.example.splitbill.model.UserSetting;
import com.example.splitbill.repository.ProfileRepository;
import com.example.splitbill.repository.UserRepository;
import com.example.splitbill.repository.UserSettingRepository;
import com.example.splitbill.service.AttemptLimiter;
import com.example.splitbill.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final ProfileRepository profileRepository;
    private final UserSettingRepository settingRepository;
    private final AttemptLimiter attemptLimiter;
    // ตัวนับการค้นอีเมลที่ไม่พบ (กันไล่เดาว่าอีเมลไหนมีบัญชี)
    // ช่องค้นหาในหน้าเว็บค้นระหว่างพิมพ์ จึงเผื่อจำนวนครั้งไว้มากกว่ารหัสบิล
    private static final String LOOKUP_ACTION = "user-lookup";
    private static final int MAX_LOOKUP_FAILURES = 40;

    public UserServiceImpl(UserRepository userRepository, UserMapper mapper, PasswordEncoder passwordEncoder,
                           ProfileRepository profileRepository, UserSettingRepository settingRepository,
                           AttemptLimiter attemptLimiter) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.profileRepository = profileRepository;
        this.settingRepository = settingRepository;
        this.attemptLimiter = attemptLimiter;
    }

    @Override
    public UserResponse create(UserRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered");
        }
        User user = new User();
        user.setUsername(request.username().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        return mapper.toResponse(userRepository.save(user));
    }

    @Override @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return mapper.toResponse(find(id));
    }

    @Override
    public UserResponse update(Long id, UserRequest request, Long currentUserId) {
        requireSelf(id, currentUserId);
        User user = find(id);
        String email = normalizeEmail(request.email());
        if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered");
        }
        user.setUsername(request.username().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        return mapper.toResponse(userRepository.save(user));
    }

    @Override
    public void delete(Long id, Long currentUserId) {
        requireSelf(id, currentUserId);
        userRepository.delete(find(id));
    }

    @Override @Transactional(readOnly = true)
    public UserResponse authenticate(String email, String password) {
        // ล็อกอินด้วยอีเมลที่สมัครไว้ (ไม่สนช่องว่างหัวท้ายและตัวพิมพ์เล็ก-ใหญ่)
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .filter(u -> u.getPassword() != null && passwordEncoder.matches(password, u.getPassword()))
                .orElseThrow(InvalidCredentialsException::new);
        return mapper.toResponse(user);
    }

    @Override @Transactional(readOnly = true)
    public UserResponse findByEmail(String email, Long requesterId) {
        attemptLimiter.check(LOOKUP_ACTION, requesterId, MAX_LOOKUP_FAILURES);
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(email)).orElse(null);
        if (user == null) {
            attemptLimiter.recordFailure(LOOKUP_ACTION, requesterId);
            throw new ResourceNotFoundException("ไม่พบผู้ใช้ที่ใช้อีเมลนี้");
        }
        return mapper.toResponse(user);
    }

    @Override @Transactional(readOnly = true)
    public SessionResponse session(Long userId) {
        return toSession(find(userId));
    }

    @Override
    public SessionResponse updateProfile(Long userId, ProfileRequest request) {
        User user = find(userId);
        user.setUsername(request.username().trim());
        userRepository.save(user);

        Profile profile = profileRepository.findByUser_Id(userId).orElseGet(Profile::new);
        profile.setUser(user);
        profile.setDisplayName(user.getUsername());
        profile.setPhone(blankToNull(request.phone()));
        profile.setBio(blankToNull(request.bio()));
        profileRepository.save(profile);
        return toSession(user);
    }

    @Override
    public boolean setNotificationEnabled(Long userId, boolean enabled) {
        User user = find(userId);
        UserSetting setting = settingRepository.findByUser_Id(userId).orElseGet(UserSetting::new);
        setting.setUser(user);
        setting.setNotificationEnabled(enabled);
        if (setting.getLanguage() == null) setting.setLanguage("th");
        if (setting.getCurrency() == null) setting.setCurrency("THB");
        settingRepository.save(setting);
        return enabled;
    }

    private SessionResponse toSession(User user) {
        Profile profile = profileRepository.findByUser_Id(user.getId()).orElse(null);
        UserSetting setting = settingRepository.findByUser_Id(user.getId()).orElse(null);
        boolean notify = setting == null || setting.getNotificationEnabled() == null || setting.getNotificationEnabled();
        return new SessionResponse(user.getId(), user.getUsername(), user.getEmail(),
                profile == null ? null : profile.getPhone(),
                profile == null ? null : profile.getBio(),
                notify);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private void requireSelf(Long id, Long currentUserId) {
        if (!id.equals(currentUserId)) throw new ForbiddenException("You can only modify your own account");
    }

    private User find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}

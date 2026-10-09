package com.example.splitbill.service.impl;

import com.example.splitbill.dto.request.UserRequest;
import com.example.splitbill.dto.response.UserResponse;
import com.example.splitbill.exception.ConflictException;
import com.example.splitbill.exception.ForbiddenException;
import com.example.splitbill.exception.InvalidCredentialsException;
import com.example.splitbill.exception.ResourceNotFoundException;
import com.example.splitbill.mapper.UserMapper;
import com.example.splitbill.model.User;
import com.example.splitbill.repository.UserRepository;
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

    public UserServiceImpl(UserRepository userRepository, UserMapper mapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
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
        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered");
        }
        user.setUsername(request.username());
        user.setEmail(request.email());
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
        User user = userRepository.findByEmail(email)
                .filter(u -> u.getPassword() != null && passwordEncoder.matches(password, u.getPassword()))
                .orElseThrow(InvalidCredentialsException::new);
        return mapper.toResponse(user);
    }

    private void requireSelf(Long id, Long currentUserId) {
        if (!id.equals(currentUserId)) throw new ForbiddenException("You can only modify your own account");
    }

    private User find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}

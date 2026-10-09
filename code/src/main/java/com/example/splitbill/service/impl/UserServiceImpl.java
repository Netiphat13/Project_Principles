package com.example.splitbill.service.impl;

import com.example.splitbill.dto.request.UserRequest;
import com.example.splitbill.dto.response.UserResponse;
import com.example.splitbill.exception.ConflictException;
import com.example.splitbill.exception.ResourceNotFoundException;
import com.example.splitbill.mapper.UserMapper;
import com.example.splitbill.model.User;
import com.example.splitbill.repository.UserRepository;
import com.example.splitbill.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper mapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(request.password());
        return mapper.toResponse(userRepository.save(user));
    }

    @Override @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return mapper.toResponse(find(id));
    }

    @Override @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    public UserResponse update(Long id, UserRequest request) {
        User user = find(id);
        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered");
        }
        user.setUsername(request.username());
        user.setEmail(request.email());
        if (request.password() != null && !request.password().isBlank()) user.setPassword(request.password());
        return mapper.toResponse(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) throw new ResourceNotFoundException("User not found: " + id);
        userRepository.deleteById(id);
    }

    @Override @Transactional(readOnly = true)
    public UserResponse authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .filter(u -> u.getPassword().equals(password))
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));
        return mapper.toResponse(user);
    }

    private User find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}

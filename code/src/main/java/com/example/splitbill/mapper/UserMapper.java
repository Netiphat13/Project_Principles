package com.example.splitbill.mapper;

import com.example.splitbill.dto.response.UserResponse;
import com.example.splitbill.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }
}

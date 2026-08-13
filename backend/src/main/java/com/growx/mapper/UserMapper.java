package com.growx.mapper;

import com.growx.dto.response.UserResponse;
import com.growx.entity.User;
import org.springframework.stereotype.Component;

/**
 * Converts User entities to UserResponse DTOs.
 * Ensures passwordHash is never included in API responses.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

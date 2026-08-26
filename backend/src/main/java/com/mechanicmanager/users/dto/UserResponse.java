package com.mechanicmanager.users.dto;

import com.mechanicmanager.users.model.Role;
import com.mechanicmanager.users.model.UserStatus;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        UserStatus status,
        boolean mustChangePassword,
        LocalDateTime createdAt
) {
}

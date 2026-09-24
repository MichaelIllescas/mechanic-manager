package com.mechanicmanager.auth.dto;

import com.mechanicmanager.users.model.Role;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        Long userId,
        String name,
        Role role,
        boolean mustChangePassword
) {
}
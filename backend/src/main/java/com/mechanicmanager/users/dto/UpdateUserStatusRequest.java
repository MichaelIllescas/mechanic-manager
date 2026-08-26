package com.mechanicmanager.users.dto;

import com.mechanicmanager.users.model.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        
        @NotNull(message = "El estado es obligatorio")
        UserStatus status
) {
}

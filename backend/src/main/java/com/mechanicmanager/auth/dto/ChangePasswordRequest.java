package com.mechanicmanager.auth.dto;

import com.mechanicmanager.shared.validation.PasswordLength;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        
        @NotBlank(message = "La contraseña actual es obligatoria")
        @PasswordLength
        String currentPassword,
        
        @NotBlank(message = "La contraseña nueva es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @PasswordLength
        String newPassword
) {
}

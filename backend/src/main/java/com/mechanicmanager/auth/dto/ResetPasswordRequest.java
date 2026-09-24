package com.mechanicmanager.auth.dto;

import com.mechanicmanager.shared.validation.PasswordLength;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        
        @NotBlank(message = "El token es obligatorio")
        String token,
        
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @PasswordLength
        String newPassword
) {
}

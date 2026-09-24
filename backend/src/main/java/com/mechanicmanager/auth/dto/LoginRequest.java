package com.mechanicmanager.auth.dto;

import com.mechanicmanager.shared.validation.PasswordLength;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        String email,
        
        @NotBlank(message = "La contraseña es obligatoria")
        @PasswordLength
        String password
) {
}
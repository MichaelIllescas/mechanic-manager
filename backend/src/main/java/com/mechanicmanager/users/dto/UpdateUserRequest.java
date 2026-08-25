package com.mechanicmanager.users.dto;

import com.mechanicmanager.users.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String name,
        
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 255, message = "El email no puede superar los 255 caracteres")
        String email,
        
        @NotNull(message = "El rol es obligatorio")
        Role role
) {
}

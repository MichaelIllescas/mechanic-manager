package com.mechanicmanager.workshops.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Todos los campos son opcionales: el usuario puede guardar el taller
 * parcialmente cargado. Solo se valida el formato de lo que sí envía.
 */
public record UpdateWorkshopRequest(
        
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String name,
        
        @Pattern(regexp = "^\\s*$|^[0-9+()\\-\\s]{6,30}$",
                message = "El teléfono solo puede contener números, espacios y los símbolos + - ( )")
        String phone,
        
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 255, message = "El email no puede superar los 255 caracteres")
        String email,
        
        @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
        String address,
        
        @Size(max = 1000, message = "Las notas no pueden superar los 1000 caracteres")
        String notes
) {
}

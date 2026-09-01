package com.mechanicmanager.customers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 120, message = "El apellido no puede superar los 120 caracteres")
        String lastName,

        @Size(max = 30, message = "El documento no puede superar los 30 caracteres")
        String documentNumber,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^[0-9+()\\-\\s]{6,30}$",
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

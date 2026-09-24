package com.mechanicmanager.servicetypes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateServiceTypeRequest(
        
        @NotBlank(message = "El nombre del servicio es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String name,
        
        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String description,
        
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "El precio admite hasta 10 enteros y 2 decimales")
        BigDecimal basePrice
) {
}

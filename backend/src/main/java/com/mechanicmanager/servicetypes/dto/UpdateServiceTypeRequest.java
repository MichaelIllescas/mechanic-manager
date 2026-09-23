package com.mechanicmanager.servicetypes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * El estado activo/inactivo no se modifica acá: tiene su propio endpoint,
 * para que desactivar un servicio sea una acción explícita y no un efecto
 * secundario de editar el precio o el nombre.
 */
public record UpdateServiceTypeRequest(
        
        @NotBlank(message = "El nombre del servicio es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String name,
        
        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String description,
        
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
        BigDecimal basePrice
) {
}

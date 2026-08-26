package com.mechanicmanager.supplies.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateSupplyStatusRequest(
        
        @NotNull(message = "El estado es obligatorio")
        Boolean active
) {
}

package com.mechanicmanager.servicetypes.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateServiceTypeStatusRequest(
        
        @NotNull(message = "El estado es obligatorio")
        Boolean active
) {
}

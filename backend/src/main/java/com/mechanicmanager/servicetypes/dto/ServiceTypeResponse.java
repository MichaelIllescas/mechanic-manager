package com.mechanicmanager.servicetypes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceTypeResponse(
        Long id,
        String name,
        String description,
        BigDecimal basePrice,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

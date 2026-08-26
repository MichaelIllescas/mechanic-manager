package com.mechanicmanager.supplies.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SupplyResponse(
        Long id,
        String name,
        BigDecimal price,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

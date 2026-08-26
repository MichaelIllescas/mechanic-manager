package com.mechanicmanager.customers.dto;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        Long workshopId,
        String firstName,
        String lastName,
        String documentNumber,
        String phone,
        String email,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

package com.mechanicmanager.customers.dto;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String documentNumber,
        String phone,
        String email,
        String address,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

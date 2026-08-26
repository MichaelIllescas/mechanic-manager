package com.mechanicmanager.workshops.dto;

import java.time.LocalDateTime;

/**
 * Los campos de negocio pueden venir en null: el taller se crea vacío
 * y el usuario lo completa. {@code complete} le indica al front si ya
 * están cargados los datos mínimos.
 */
public record WorkshopResponse(
        Long id,
        String name,
        String phone,
        String email,
        String address,
        String notes,
        boolean complete,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

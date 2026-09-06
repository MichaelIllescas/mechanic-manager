package com.mechanicmanager.appointments.dto;

import com.mechanicmanager.appointments.model.AppointmentStatus;

import java.time.LocalDateTime;

/**
 * Incluye los datos del vehículo y del cliente que la agenda necesita mostrar
 * en cada fila, para no obligar al frontend a una request por turno.
 */
public record AppointmentResponse(
        Long id,
        LocalDateTime scheduledAt,
        AppointmentStatus status,
        String notes,
        Long vehicleId,
        String licensePlate,
        String brand,
        String model,
        Long customerId,
        String customerFirstName,
        String customerLastName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

package com.mechanicmanager.appointments.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * El estado no se recibe: todo turno nace {@code SCHEDULED} y los cambios
 * posteriores pasan por los use cases que validan la transición.
 *
 * <p>{@code scheduledAt} admite fechas pasadas: un turno pactado ayer por
 * teléfono puede cargarse hoy.
 */
public record CreateAppointmentRequest(

        @NotNull(message = "El vehículo es obligatorio")
        @Positive(message = "El identificador del vehículo debe ser mayor a cero")
        Long vehicleId,

        @NotNull(message = "La fecha y hora del turno son obligatorias")
        LocalDateTime scheduledAt,

        @Size(max = 1000, message = "Las notas no pueden superar los 1000 caracteres")
        String notes
) {
}

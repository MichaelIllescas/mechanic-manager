package com.mechanicmanager.appointments.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Reprogramar es modificar la fecha de un turno agendado, no un estado propio.
 *
 * <p>El vehículo no se puede cambiar: si el turno se cargó sobre el auto
 * equivocado se cancela y se crea otro, para no alterar el historial del
 * vehículo original.
 */
public record UpdateAppointmentRequest(

        @NotNull(message = "La fecha y hora del turno son obligatorias")
        LocalDateTime scheduledAt,

        @Size(max = 1000, message = "Las notas no pueden superar los 1000 caracteres")
        String notes
) {
}

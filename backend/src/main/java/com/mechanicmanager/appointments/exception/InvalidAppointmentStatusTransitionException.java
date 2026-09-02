package com.mechanicmanager.appointments.exception;

import com.mechanicmanager.appointments.model.AppointmentStatus;
import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * Se lanza al intentar completar o cancelar un turno que ya no está agendado:
 * {@code COMPLETED} y {@code CANCELLED} son estados terminales.
 */
public class InvalidAppointmentStatusTransitionException extends AppException {

    public InvalidAppointmentStatusTransitionException(
            AppointmentStatus current,
            AppointmentStatus target
    ) {
        super(
                "INVALID_APPOINTMENT_STATUS_TRANSITION",
                HttpStatus.CONFLICT,
                "No se puede pasar un turno de " + current + " a " + target
        );
    }
}

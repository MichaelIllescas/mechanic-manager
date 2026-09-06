package com.mechanicmanager.appointments.exception;

import com.mechanicmanager.appointments.model.AppointmentStatus;
import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * Se lanza al intentar reprogramar o editar un turno que ya no está agendado.
 *
 * <p>Distinta de {@link InvalidAppointmentStatusTransitionException}: acá nadie
 * pidió cambiar el estado, sólo modificar un turno que ya está cerrado.
 */
public class AppointmentNotEditableException extends AppException {

    public AppointmentNotEditableException(AppointmentStatus current) {
        super(
                "APPOINTMENT_NOT_EDITABLE",
                HttpStatus.CONFLICT,
                "No se puede modificar un turno en estado " + current
        );
    }
}

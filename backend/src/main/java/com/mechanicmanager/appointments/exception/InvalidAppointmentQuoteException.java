package com.mechanicmanager.appointments.exception;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * El presupuesto elegido para vincular al turno no es apto: no está
 * aprobado, no es del mismo vehículo, o ya está vinculado a otro turno.
 */
public class InvalidAppointmentQuoteException extends AppException {

    public InvalidAppointmentQuoteException(String detail) {
        super(
                "INVALID_APPOINTMENT_QUOTE",
                HttpStatus.BAD_REQUEST,
                detail
        );
    }
}

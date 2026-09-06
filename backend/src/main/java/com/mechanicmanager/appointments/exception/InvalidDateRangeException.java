package com.mechanicmanager.appointments.exception;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * Rango de fechas que la agenda no puede servir.
 *
 * <p>El tope existe para que una consulta de años no traiga todos los turnos
 * con sus vehículos y clientes en una sola respuesta.
 */
public class InvalidDateRangeException extends AppException {

    public InvalidDateRangeException(String detail) {
        super(
                "INVALID_DATE_RANGE",
                HttpStatus.BAD_REQUEST,
                detail
        );
    }
}

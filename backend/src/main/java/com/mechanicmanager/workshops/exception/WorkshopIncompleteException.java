package com.mechanicmanager.workshops.exception;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class WorkshopIncompleteException extends AppException {
    
    public WorkshopIncompleteException() {
        super("WORKSHOP_INCOMPLETE", HttpStatus.CONFLICT,
                "El taller no tiene cargados los datos mínimos (nombre, teléfono y dirección)");
    }
}

package com.mechanicmanager.auth.exception;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class PasswordNotChangedException extends AppException {
    
    public PasswordNotChangedException() {
        super("PASSWORD_NOT_CHANGED", HttpStatus.BAD_REQUEST,
                "La contraseña nueva debe ser distinta de la actual");
    }
}

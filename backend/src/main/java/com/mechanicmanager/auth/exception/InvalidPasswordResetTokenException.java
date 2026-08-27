package com.mechanicmanager.auth.exception;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidPasswordResetTokenException extends AppException {
    
    public InvalidPasswordResetTokenException() {
        super("INVALID_PASSWORD_RESET_TOKEN", HttpStatus.BAD_REQUEST,
                "El enlace de recuperación no es válido o ya venció. Solicitá uno nuevo.");
    }
}

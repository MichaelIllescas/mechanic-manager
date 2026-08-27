package com.mechanicmanager.auth.exception;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidCurrentPasswordException extends AppException {
    
    public InvalidCurrentPasswordException() {
        super("INVALID_CURRENT_PASSWORD", HttpStatus.UNAUTHORIZED,
                "La contraseña actual es incorrecta");
    }
}

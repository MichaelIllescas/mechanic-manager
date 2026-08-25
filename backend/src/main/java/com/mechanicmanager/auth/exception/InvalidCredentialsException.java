package com.mechanicmanager.auth.exception;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends AppException {

    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
    }
}

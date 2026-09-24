package com.mechanicmanager.auth.exception;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class TooManyAttemptsException extends AppException {

    public TooManyAttemptsException() {
        super("TOO_MANY_ATTEMPTS", HttpStatus.TOO_MANY_REQUESTS,
                "Demasiados intentos. Esperá unos minutos antes de volver a intentar");
    }
}

package com.mechanicmanager.users.exception;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends AppException {
    
    public EmailAlreadyExistsException(String email) {
        super("EMAIL_ALREADY_EXISTS", HttpStatus.CONFLICT,
                "Ya existe un usuario registrado con el email " + email);
    }
}

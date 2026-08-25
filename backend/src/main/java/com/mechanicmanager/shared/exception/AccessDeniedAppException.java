package com.mechanicmanager.shared.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedAppException extends AppException {

    public AccessDeniedAppException(String message) {
        super("ACCESS_DENIED", HttpStatus.FORBIDDEN, message);
    }
}

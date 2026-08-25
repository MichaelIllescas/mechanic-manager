package com.mechanicmanager.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AppException extends RuntimeException {
    
    private final String code;
    private final HttpStatus status;
    
    protected AppException(String code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }
}
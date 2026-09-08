package com.mechanicmanager.quotes.exception;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidQuoteException extends AppException {

    public InvalidQuoteException(String message) {
        super("INVALID_QUOTE", HttpStatus.BAD_REQUEST, message);
    }
}

package com.mechanicmanager.workorders.exception;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidWorkOrderException extends AppException {

    public InvalidWorkOrderException(String message) {
        super("INVALID_WORK_ORDER", HttpStatus.BAD_REQUEST, message);
    }
}

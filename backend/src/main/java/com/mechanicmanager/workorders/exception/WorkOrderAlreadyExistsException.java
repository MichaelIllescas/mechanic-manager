package com.mechanicmanager.workorders.exception;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class WorkOrderAlreadyExistsException extends AppException {

    public WorkOrderAlreadyExistsException(String message) {
        super("WORK_ORDER_ALREADY_EXISTS", HttpStatus.CONFLICT, message);
    }
}

package com.mechanicmanager.servicetypes.exception;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class ServiceTypeNameAlreadyExistsException extends AppException {
    
    public ServiceTypeNameAlreadyExistsException(String name) {
        super("SERVICE_TYPE_NAME_ALREADY_EXISTS", HttpStatus.CONFLICT,
                "Ya existe un servicio con el nombre " + name);
    }
}

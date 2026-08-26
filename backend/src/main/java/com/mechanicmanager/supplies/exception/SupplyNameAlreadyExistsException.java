package com.mechanicmanager.supplies.exception;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class SupplyNameAlreadyExistsException extends AppException {
    
    public SupplyNameAlreadyExistsException(String name) {
        super("SUPPLY_NAME_ALREADY_EXISTS", HttpStatus.CONFLICT,
                "Ya existe un insumo con el nombre " + name);
    }
}

package com.mechanicmanager.vehicles.exception;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class LicensePlateAlreadyExistsException extends AppException {

    public LicensePlateAlreadyExistsException(String licensePlate) {
        super(
                "LICENSE_PLATE_ALREADY_EXISTS",
                HttpStatus.CONFLICT,
                "Ya existe un vehículo con la patente " + licensePlate
        );
    }
}

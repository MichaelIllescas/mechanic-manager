package com.mechanicmanager.auth.exception;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class AccountDisabledException extends AppException {

    public AccountDisabledException() {
        super("ACCOUNT_DISABLED", HttpStatus.FORBIDDEN,
                "La cuenta está deshabilitada. Contactá al administrador.");
    }
}

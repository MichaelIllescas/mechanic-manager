package com.mechanicmanager.shared.email;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class EmailDeliveryException extends AppException {
    
    public EmailDeliveryException(Throwable cause) {
        super("EMAIL_DELIVERY_FAILED", HttpStatus.SERVICE_UNAVAILABLE,
                "No se pudo enviar el email. Intenta nuevamente en unos minutos.");
        initCause(cause);
    }
}
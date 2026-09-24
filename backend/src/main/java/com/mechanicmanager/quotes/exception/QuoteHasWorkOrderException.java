package com.mechanicmanager.quotes.exception;

import com.mechanicmanager.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class QuoteHasWorkOrderException extends AppException {

    public QuoteHasWorkOrderException() {
        super("QUOTE_HAS_WORK_ORDER", HttpStatus.CONFLICT,
                "El presupuesto ya tiene un trabajo realizado: no se puede cambiar su estado");
    }
}

package com.mechanicmanager.quotes.dto;

import com.mechanicmanager.quotes.model.QuoteStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateQuoteStatusRequest(

        @NotNull(message = "El estado es obligatorio")
        QuoteStatus status
) {
}

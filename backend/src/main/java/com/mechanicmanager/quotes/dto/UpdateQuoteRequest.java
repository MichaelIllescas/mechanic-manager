package com.mechanicmanager.quotes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record UpdateQuoteRequest(

        @NotNull(message = "El cliente es obligatorio")
        @Positive(message = "El identificador del cliente debe ser mayor a cero")
        Long customerId,

        @NotNull(message = "El vehículo es obligatorio")
        @Positive(message = "El identificador del vehículo debe ser mayor a cero")
        Long vehicleId,

        // Sin @FutureOrPresent a propósito: un presupuesto vencido tiene que poder
        // editarse (una nota, un precio) sin obligar a cambiar la fecha.
        LocalDate validUntil,

        @Size(max = 2000, message = "Las notas no pueden superar los 2000 caracteres")
        String notes,

        @NotEmpty(message = "El presupuesto debe tener al menos un ítem")
        List<@Valid QuoteItemRequest> items
) {
}

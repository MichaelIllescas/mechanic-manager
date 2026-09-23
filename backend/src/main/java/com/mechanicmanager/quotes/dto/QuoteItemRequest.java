package com.mechanicmanager.quotes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record QuoteItemRequest(

        @Positive(message = "El identificador del servicio debe ser mayor a cero")
        Long serviceTypeId,

        @Positive(message = "El identificador del insumo debe ser mayor a cero")
        Long supplyId,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String description,

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
        @Digits(
                integer = 9,
                fraction = 3,
                message = "La cantidad admite hasta 9 enteros y 3 decimales"
        )
        BigDecimal quantity,

        @NotNull(message = "El precio unitario es obligatorio")
        @Digits(
                integer = 10,
                fraction = 2,
                message = "El precio admite hasta 10 enteros y 2 decimales"
        )
        BigDecimal unitPrice
) {
}

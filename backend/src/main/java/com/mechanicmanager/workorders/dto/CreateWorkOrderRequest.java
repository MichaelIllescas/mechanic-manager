package com.mechanicmanager.workorders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateWorkOrderRequest(

        @NotNull(message = "El vehículo es obligatorio")
        @Positive(message = "El identificador del vehículo debe ser mayor a cero")
        Long vehicleId,

        @Positive(message = "El identificador del turno debe ser mayor a cero")
        Long appointmentId,

        @Positive(message = "El identificador del presupuesto debe ser mayor a cero")
        Long quoteId,

        @NotNull(message = "La fecha del trabajo es obligatoria")
        LocalDate performedAt,

        @PositiveOrZero(message = "El kilometraje no puede ser negativo")
        Integer mileage,

        @Size(max = 2000, message = "Las notas no pueden superar los 2000 caracteres")
        String notes,

        @NotEmpty(message = "La orden de trabajo debe tener al menos un ítem")
        List<@Valid WorkOrderItemRequest> items
) {
}

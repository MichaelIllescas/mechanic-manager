package com.mechanicmanager.vehicles.dto;

import com.mechanicmanager.vehicles.model.VehicleType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateVehicleRequest(

        @NotNull(message = "El cliente es obligatorio")
        @Positive(message = "El identificador del cliente debe ser mayor a cero")
        Long customerId,

        @NotBlank(message = "La patente es obligatoria")
        @Size(max = 20, message = "La patente no puede superar los 20 caracteres")
        String licensePlate,

        @NotBlank(message = "La marca es obligatoria")
        @Size(max = 80, message = "La marca no puede superar los 80 caracteres")
        String brand,

        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 120, message = "El modelo no puede superar los 120 caracteres")
        String model,

        @Min(value = 1886, message = "El año no puede ser anterior a 1886")
        @Max(value = 9999, message = "El año no puede superar 9999")
        Integer year,

        @Size(max = 100, message = "El número de chasis no puede superar los 100 caracteres")
        String chassisNumber,

        @Size(max = 100, message = "El número de motor no puede superar los 100 caracteres")
        String engineNumber,

        @PositiveOrZero(message = "El kilometraje no puede ser negativo")
        Integer mileage,

        @NotNull(message = "El tipo de vehículo es obligatorio")
        VehicleType vehicleType,

        @Size(max = 1000, message = "Las notas no pueden superar los 1000 caracteres")
        String notes
) {
}

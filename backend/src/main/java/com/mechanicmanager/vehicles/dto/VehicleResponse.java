package com.mechanicmanager.vehicles.dto;

import com.mechanicmanager.vehicles.model.VehicleType;

import java.time.LocalDateTime;

public record VehicleResponse(
        Long id,
        Long customerId,
        String customerFirstName,
        String customerLastName,
        String licensePlate,
        String brand,
        String model,
        Integer year,
        String chassisNumber,
        String engineNumber,
        Integer mileage,
        VehicleType vehicleType,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

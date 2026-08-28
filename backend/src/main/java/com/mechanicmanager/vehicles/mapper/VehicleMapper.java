package com.mechanicmanager.vehicles.mapper;

import com.mechanicmanager.vehicles.dto.VehicleResponse;
import com.mechanicmanager.vehicles.model.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getCustomer().getId(),
                vehicle.getCustomer().getFirstName(),
                vehicle.getCustomer().getLastName(),
                vehicle.getLicensePlate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getChassisNumber(),
                vehicle.getEngineNumber(),
                vehicle.getMileage(),
                vehicle.getVehicleType(),
                vehicle.getNotes(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }
}

package com.mechanicmanager.vehicles.service;

import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.customers.repository.CustomerRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.vehicles.dto.UpdateVehicleRequest;
import com.mechanicmanager.vehicles.dto.VehicleResponse;
import com.mechanicmanager.vehicles.exception.LicensePlateAlreadyExistsException;
import com.mechanicmanager.vehicles.mapper.VehicleMapper;
import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UpdateVehicleUseCase {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final VehicleMapper vehicleMapper;

    @Transactional
    public VehicleResponse execute(
            Long userId,
            Long vehicleId,
            UpdateVehicleRequest request
    ) {
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(vehicleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el vehículo con id " + vehicleId));

        Customer customer = customerRepository.findByIdAndUserId(
                        request.customerId(),
                        userId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el cliente indicado"));

        String licensePlate = normalizeIdentifier(request.licensePlate());

        if (vehicleRepository.existsByUserIdAndLicensePlateIgnoreCaseAndIdNot(
                userId,
                licensePlate,
                vehicleId
        )) {
            throw new LicensePlateAlreadyExistsException(licensePlate);
        }

        vehicle.setCustomer(customer);
        vehicle.setLicensePlate(licensePlate);
        vehicle.setBrand(request.brand().trim());
        vehicle.setModel(request.model().trim());
        vehicle.setYear(request.year());
        vehicle.setChassisNumber(normalizeIdentifier(request.chassisNumber()));
        vehicle.setEngineNumber(normalizeIdentifier(request.engineNumber()));
        vehicle.setMileage(request.mileage());
        vehicle.setVehicleType(request.vehicleType());
        vehicle.setNotes(trimToNull(request.notes()));

        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    private static String normalizeIdentifier(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

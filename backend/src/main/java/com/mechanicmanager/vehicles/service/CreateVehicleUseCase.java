package com.mechanicmanager.vehicles.service;

import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.customers.repository.CustomerRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.users.repository.UserRepository;
import com.mechanicmanager.vehicles.dto.CreateVehicleRequest;
import com.mechanicmanager.vehicles.dto.VehicleResponse;
import com.mechanicmanager.vehicles.exception.LicensePlateAlreadyExistsException;
import com.mechanicmanager.vehicles.mapper.VehicleMapper;
import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CreateVehicleUseCase {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final VehicleMapper vehicleMapper;

    private static final String LICENSE_PLATE_CONSTRAINT = "uq_vehicles_user_license_plate";

    @Transactional
    public VehicleResponse execute(Long userId, CreateVehicleRequest request) {
        Customer customer = customerRepository.findByIdAndUserId(
                        request.customerId(),
                        userId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el cliente indicado"));

        String licensePlate = normalizeIdentifier(request.licensePlate());

        if (vehicleRepository.existsByUserIdAndLicensePlateIgnoreCase(
                userId,
                licensePlate
        )) {
            throw new LicensePlateAlreadyExistsException(licensePlate);
        }

        Vehicle vehicle = Vehicle.builder()
                .user(userRepository.getReferenceById(userId))
                .customer(customer)
                .licensePlate(licensePlate)
                .brand(request.brand().trim())
                .model(request.model().trim())
                .year(request.year())
                .chassisNumber(normalizeIdentifierToNull(request.chassisNumber()))
                .engineNumber(normalizeIdentifierToNull(request.engineNumber()))
                .mileage(request.mileage())
                .vehicleType(request.vehicleType())
                .notes(trimToNull(request.notes()))
                .build();

        try {
            return vehicleMapper.toResponse(vehicleRepository.saveAndFlush(vehicle));
        } catch (DataIntegrityViolationException e) {
            throw translateLicensePlateConflict(e, licensePlate);
        }
    }

    /**
     * La verificación previa no es atómica: entre el exists y el insert otra
     * transacción puede registrar la misma patente. La restricción única de la
     * base es la garantía real, y su violación se traduce al error del dominio
     * para no responder un 500 (ver README de shared/exception).
     */
    private static RuntimeException translateLicensePlateConflict(
            DataIntegrityViolationException e,
            String licensePlate
    ) {
        String cause = e.getMostSpecificCause().getMessage();

        if (cause != null && cause.contains(LICENSE_PLATE_CONSTRAINT)) {
            return new LicensePlateAlreadyExistsException(licensePlate);
        }

        return e;
    }

    private static String normalizeIdentifier(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeIdentifierToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : normalizeIdentifier(value);
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

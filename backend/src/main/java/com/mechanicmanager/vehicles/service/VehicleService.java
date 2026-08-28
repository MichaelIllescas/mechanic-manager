package com.mechanicmanager.vehicles.service;

import com.mechanicmanager.vehicles.dto.CreateVehicleRequest;
import com.mechanicmanager.vehicles.dto.UpdateVehicleRequest;
import com.mechanicmanager.vehicles.dto.VehicleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final CreateVehicleUseCase createVehicleUseCase;
    private final ListVehiclesUseCase listVehiclesUseCase;
    private final SearchVehiclesUseCase searchVehiclesUseCase;
    private final UpdateVehicleUseCase updateVehicleUseCase;

    public VehicleResponse create(Long userId, CreateVehicleRequest request) {
        return createVehicleUseCase.execute(userId, request);
    }

    public List<VehicleResponse> findAll(Long userId, String search) {
        if (search == null || search.isBlank()) {
            return listVehiclesUseCase.execute(userId);
        }
        return searchVehiclesUseCase.execute(userId, search);
    }

    public VehicleResponse update(
            Long userId,
            Long vehicleId,
            UpdateVehicleRequest request
    ) {
        return updateVehicleUseCase.execute(userId, vehicleId, request);
    }
}

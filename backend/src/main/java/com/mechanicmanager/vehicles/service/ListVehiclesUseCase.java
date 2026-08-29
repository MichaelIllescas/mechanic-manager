package com.mechanicmanager.vehicles.service;

import com.mechanicmanager.vehicles.dto.VehicleResponse;
import com.mechanicmanager.vehicles.mapper.VehicleMapper;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListVehiclesUseCase {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    @Transactional(readOnly = true)
    public List<VehicleResponse> execute(Long userId) {
        return vehicleRepository
                .findAllByUserIdOrderByLicensePlateAsc(userId)
                .stream()
                .map(vehicleMapper::toResponse)
                .toList();
    }
}

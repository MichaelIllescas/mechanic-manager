package com.mechanicmanager.vehicles.service;

import com.mechanicmanager.vehicles.dto.VehicleResponse;
import com.mechanicmanager.vehicles.mapper.VehicleMapper;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
import com.mechanicmanager.vehicles.repository.VehicleSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchVehiclesUseCase {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    @Transactional(readOnly = true)
    public List<VehicleResponse> execute(Long userId, String search) {
        return vehicleRepository.findAll(
                        VehicleSpecifications.belongsToUserAndLicensePlateMatches(
                                userId,
                                search.trim()
                        ),
                        Sort.by("licensePlate")
                )
                .stream()
                .map(vehicleMapper::toResponse)
                .toList();
    }
}

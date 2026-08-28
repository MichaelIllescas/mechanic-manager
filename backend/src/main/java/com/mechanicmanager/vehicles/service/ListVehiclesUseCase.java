package com.mechanicmanager.vehicles.service;

import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.vehicles.dto.VehicleResponse;
import com.mechanicmanager.vehicles.mapper.VehicleMapper;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
import com.mechanicmanager.workshops.model.Workshop;
import com.mechanicmanager.workshops.repository.WorkshopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListVehiclesUseCase {

    private final VehicleRepository vehicleRepository;
    private final WorkshopRepository workshopRepository;
    private final VehicleMapper vehicleMapper;

    @Transactional(readOnly = true)
    public List<VehicleResponse> execute(Long userId) {
        Workshop workshop = workshopRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario no tiene un taller asociado"));

        return vehicleRepository
                .findAllByWorkshop_IdOrderByLicensePlateAsc(workshop.getId())
                .stream()
                .map(vehicleMapper::toResponse)
                .toList();
    }
}

package com.mechanicmanager.servicetypes.service;

import com.mechanicmanager.servicetypes.dto.ServiceTypeResponse;
import com.mechanicmanager.servicetypes.dto.UpdateServiceTypeStatusRequest;
import com.mechanicmanager.servicetypes.mapper.ServiceTypeMapper;
import com.mechanicmanager.servicetypes.model.ServiceType;
import com.mechanicmanager.servicetypes.repository.ServiceTypeRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reemplaza al borrado: un servicio utilizado no se elimina, se desactiva,
 * para no romper el historial que lo referencia.
 */
@Service
@RequiredArgsConstructor
public class UpdateServiceTypeStatusUseCase {

    private final ServiceTypeRepository serviceTypeRepository;
    private final ServiceTypeMapper serviceTypeMapper;

    @Transactional
    public ServiceTypeResponse execute(Long id, UpdateServiceTypeStatusRequest request, Long userId) {
        ServiceType serviceType = serviceTypeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el servicio con id " + id));

        serviceType.setActive(request.active());

        return serviceTypeMapper.toResponse(serviceTypeRepository.save(serviceType));
    }
}

package com.mechanicmanager.servicetypes.service;

import com.mechanicmanager.servicetypes.dto.ServiceTypeResponse;
import com.mechanicmanager.servicetypes.dto.UpdateServiceTypeRequest;
import com.mechanicmanager.servicetypes.exception.ServiceTypeNameAlreadyExistsException;
import com.mechanicmanager.servicetypes.mapper.ServiceTypeMapper;
import com.mechanicmanager.servicetypes.model.ServiceType;
import com.mechanicmanager.servicetypes.repository.ServiceTypeRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.shared.text.Texts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateServiceTypeUseCase {

    private final ServiceTypeRepository serviceTypeRepository;
    private final ServiceTypeMapper serviceTypeMapper;

    @Transactional
    public ServiceTypeResponse execute(Long id, UpdateServiceTypeRequest request, Long userId) {
        ServiceType serviceType = serviceTypeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el servicio con id " + id));

        String name = request.name().trim();

        if (serviceTypeRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(userId, name, id)) {
            throw new ServiceTypeNameAlreadyExistsException(name);
        }

        // Cambiar el precio no afecta presupuestos ni órdenes ya registrados:
        // esos copian el valor al crear el ítem (ADR-009).
        serviceType.setName(name);
        serviceType.setDescription(Texts.trimToNull(request.description()));
        serviceType.setBasePrice(request.basePrice());

        return serviceTypeMapper.toResponse(serviceTypeRepository.save(serviceType));
    }
}

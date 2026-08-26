package com.mechanicmanager.servicetypes.service;

import com.mechanicmanager.servicetypes.dto.CreateServiceTypeRequest;
import com.mechanicmanager.servicetypes.dto.ServiceTypeResponse;
import com.mechanicmanager.servicetypes.exception.ServiceTypeNameAlreadyExistsException;
import com.mechanicmanager.servicetypes.mapper.ServiceTypeMapper;
import com.mechanicmanager.servicetypes.model.ServiceType;
import com.mechanicmanager.servicetypes.repository.ServiceTypeRepository;
import com.mechanicmanager.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateServiceTypeUseCase {

    private final ServiceTypeRepository serviceTypeRepository;
    private final UserRepository userRepository;
    private final ServiceTypeMapper serviceTypeMapper;

    @Transactional
    public ServiceTypeResponse execute(CreateServiceTypeRequest request, Long userId) {
        String name = request.name().trim();

        if (serviceTypeRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new ServiceTypeNameAlreadyExistsException(name);
        }

        ServiceType serviceType = ServiceType.builder()
                .user(userRepository.getReferenceById(userId))
                .name(name)
                .description(trimToNull(request.description()))
                .basePrice(request.basePrice())
                .estimatedMinutes(request.estimatedMinutes())
                .active(true)
                .build();

        return serviceTypeMapper.toResponse(serviceTypeRepository.save(serviceType));
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

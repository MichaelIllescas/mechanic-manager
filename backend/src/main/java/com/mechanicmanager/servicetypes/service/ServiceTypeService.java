package com.mechanicmanager.servicetypes.service;

import com.mechanicmanager.servicetypes.dto.CreateServiceTypeRequest;
import com.mechanicmanager.servicetypes.dto.ServiceTypeResponse;
import com.mechanicmanager.servicetypes.dto.UpdateServiceTypeRequest;
import com.mechanicmanager.servicetypes.dto.UpdateServiceTypeStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceTypeService {

    private final CreateServiceTypeUseCase createServiceTypeUseCase;
    private final UpdateServiceTypeUseCase updateServiceTypeUseCase;
    private final UpdateServiceTypeStatusUseCase updateServiceTypeStatusUseCase;
    private final GetServiceTypeUseCase getServiceTypeUseCase;
    private final ListServiceTypesUseCase listServiceTypesUseCase;

    public ServiceTypeResponse create(CreateServiceTypeRequest request, Long userId) {
        return createServiceTypeUseCase.execute(request, userId);
    }

    public ServiceTypeResponse update(Long id, UpdateServiceTypeRequest request, Long userId) {
        return updateServiceTypeUseCase.execute(id, request, userId);
    }

    public ServiceTypeResponse updateStatus(Long id, UpdateServiceTypeStatusRequest request, Long userId) {
        return updateServiceTypeStatusUseCase.execute(id, request, userId);
    }

    public ServiceTypeResponse findById(Long id, Long userId) {
        return getServiceTypeUseCase.execute(id, userId);
    }

    public List<ServiceTypeResponse> findAll(Long userId, Boolean active) {
        return listServiceTypesUseCase.execute(userId, active);
    }
}

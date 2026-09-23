package com.mechanicmanager.servicetypes.mapper;

import com.mechanicmanager.servicetypes.dto.ServiceTypeResponse;
import com.mechanicmanager.servicetypes.model.ServiceType;
import org.springframework.stereotype.Component;

@Component
public class ServiceTypeMapper {
    
    public ServiceTypeResponse toResponse(ServiceType serviceType) {
        return new ServiceTypeResponse(
                serviceType.getId(),
                serviceType.getName(),
                serviceType.getDescription(),
                serviceType.getBasePrice(),
                serviceType.isActive(),
                serviceType.getCreatedAt(),
                serviceType.getUpdatedAt()
        );
    }
}

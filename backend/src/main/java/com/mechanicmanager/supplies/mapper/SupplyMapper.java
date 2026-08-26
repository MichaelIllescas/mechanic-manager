package com.mechanicmanager.supplies.mapper;

import com.mechanicmanager.supplies.dto.SupplyResponse;
import com.mechanicmanager.supplies.model.Supply;
import org.springframework.stereotype.Component;

@Component
public class SupplyMapper {
    
    public SupplyResponse toResponse(Supply supply) {
        return new SupplyResponse(
                supply.getId(),
                supply.getName(),
                supply.getPrice(),
                supply.isActive(),
                supply.getCreatedAt(),
                supply.getUpdatedAt()
        );
    }
}

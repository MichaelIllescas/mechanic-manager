package com.mechanicmanager.workshops.mapper;

import com.mechanicmanager.workshops.dto.WorkshopResponse;
import com.mechanicmanager.workshops.model.Workshop;
import org.springframework.stereotype.Component;

@Component
public class WorkshopMapper {
    
    public WorkshopResponse toResponse(Workshop workshop) {
        return new WorkshopResponse(
                workshop.getId(),
                workshop.getName(),
                workshop.getPhone(),
                workshop.getEmail(),
                workshop.getAddress(),
                workshop.getNotes(),
                workshop.isComplete(),
                workshop.getCreatedAt(),
                workshop.getUpdatedAt()
        );
    }
}

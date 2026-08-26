package com.mechanicmanager.workshops.service;

import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.workshops.dto.WorkshopResponse;
import com.mechanicmanager.workshops.mapper.WorkshopMapper;
import com.mechanicmanager.workshops.model.Workshop;
import com.mechanicmanager.workshops.repository.WorkshopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetWorkshopUseCase {
    
    private final WorkshopRepository workshopRepository;
    private final WorkshopMapper workshopMapper;
    
    @Transactional(readOnly = true)
    public WorkshopResponse execute(Long userId) {
        Workshop workshop = workshopRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario no tiene un taller asociado"));
        
        return workshopMapper.toResponse(workshop);
    }
}

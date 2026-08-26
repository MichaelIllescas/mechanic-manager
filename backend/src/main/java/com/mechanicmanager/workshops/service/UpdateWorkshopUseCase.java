package com.mechanicmanager.workshops.service;

import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.workshops.dto.UpdateWorkshopRequest;
import com.mechanicmanager.workshops.dto.WorkshopResponse;
import com.mechanicmanager.workshops.mapper.WorkshopMapper;
import com.mechanicmanager.workshops.model.Workshop;
import com.mechanicmanager.workshops.repository.WorkshopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateWorkshopUseCase {
    
    private final WorkshopRepository workshopRepository;
    private final WorkshopMapper workshopMapper;
    
    @Transactional
    public WorkshopResponse execute(Long userId, UpdateWorkshopRequest request) {
        Workshop workshop = workshopRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario no tiene un taller asociado"));
        
        workshop.setName(normalize(request.name()));
        workshop.setPhone(normalize(request.phone()));
        workshop.setAddress(normalize(request.address()));
        workshop.setNotes(normalize(request.notes()));
        
        String email = normalize(request.email());
        workshop.setEmail(email == null ? null : email.toLowerCase());
        
        return workshopMapper.toResponse(workshopRepository.save(workshop));
    }
    
    /**
     * El front envía "" cuando el usuario vacía un campo; se guarda como null
     * para no mezclar nulos y strings vacíos en la base.
     */
    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

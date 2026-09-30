package com.mechanicmanager.workshops.service;

import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.shared.text.Texts;
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
        
        workshop.setName(Texts.trimToNull(request.name()));
        workshop.setPhone(Texts.trimToNull(request.phone()));
        workshop.setAddress(Texts.trimToNull(request.address()));
        workshop.setNotes(Texts.trimToNull(request.notes()));
        
        String email = Texts.trimToNull(request.email());
        workshop.setEmail(email == null ? null : email.toLowerCase());
        
        return workshopMapper.toResponse(workshopRepository.save(workshop));
    }
}

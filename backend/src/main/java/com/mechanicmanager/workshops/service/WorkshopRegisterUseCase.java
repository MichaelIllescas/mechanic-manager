package com.mechanicmanager.workshops.service;

import com.mechanicmanager.users.model.User;
import com.mechanicmanager.workshops.model.Workshop;
import com.mechanicmanager.workshops.repository.WorkshopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkshopRegisterUseCase {
    
    private final WorkshopRepository workshopRepository;
    
    /**
     * Crea el taller vacío del usuario si todavía no lo tiene. Es idempotente:
     * se invoca tanto al dar de alta la cuenta como al promover a USER una
     * cuenta existente, y nunca pisa un taller ya cargado.
     */
    @Transactional
    public Workshop execute(User user) {
        return workshopRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Workshop workshop = new Workshop();
                    workshop.setUser(user);
                    return workshopRepository.save(workshop);
                });
    }
}

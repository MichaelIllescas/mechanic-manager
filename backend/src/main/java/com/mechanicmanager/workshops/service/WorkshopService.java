package com.mechanicmanager.workshops.service;

import com.mechanicmanager.users.model.User;
import com.mechanicmanager.workshops.dto.UpdateWorkshopRequest;
import com.mechanicmanager.workshops.dto.WorkshopResponse;
import com.mechanicmanager.workshops.model.Workshop;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkshopService {
    
    private final WorkshopRegisterUseCase workshopRegisterUseCase;
    private final GetWorkshopUseCase getWorkshopUseCase;
    private final UpdateWorkshopUseCase updateWorkshopUseCase;
    
    public Workshop registerFor(User user) {
        return workshopRegisterUseCase.execute(user);
    }
    
    public WorkshopResponse findByUser(Long userId) {
        return getWorkshopUseCase.execute(userId);
    }
    
    public WorkshopResponse update(Long userId, UpdateWorkshopRequest request) {
        return updateWorkshopUseCase.execute(userId, request);
    }
}

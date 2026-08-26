package com.mechanicmanager.workshops.controller;

import com.mechanicmanager.shared.security.AuthenticatedUser;
import com.mechanicmanager.workshops.dto.UpdateWorkshopRequest;
import com.mechanicmanager.workshops.dto.WorkshopResponse;
import com.mechanicmanager.workshops.service.WorkshopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(WorkshopRoutes.BASE)
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class WorkshopController implements WorkshopApi {
    
    private final WorkshopService workshopService;
    
    @Override
    @GetMapping(WorkshopRoutes.ME)
    public ResponseEntity<WorkshopResponse> findMine(
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(workshopService.findByUser(currentUser.id()));
    }
    
    @Override
    @PutMapping(WorkshopRoutes.ME)
    public ResponseEntity<WorkshopResponse> update(
            @Valid @RequestBody UpdateWorkshopRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(workshopService.update(currentUser.id(), request));
    }
}

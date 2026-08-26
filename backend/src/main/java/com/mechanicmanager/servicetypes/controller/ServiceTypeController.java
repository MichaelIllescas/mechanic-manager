package com.mechanicmanager.servicetypes.controller;

import com.mechanicmanager.servicetypes.dto.CreateServiceTypeRequest;
import com.mechanicmanager.servicetypes.dto.ServiceTypeResponse;
import com.mechanicmanager.servicetypes.dto.UpdateServiceTypeRequest;
import com.mechanicmanager.servicetypes.dto.UpdateServiceTypeStatusRequest;
import com.mechanicmanager.servicetypes.service.ServiceTypeService;
import com.mechanicmanager.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ServiceTypeRoutes.BASE)
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class ServiceTypeController implements ServiceTypeApi {

    private final ServiceTypeService serviceTypeService;

    @Override
    @PostMapping
    public ResponseEntity<ServiceTypeResponse> create(
            @Valid @RequestBody CreateServiceTypeRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(serviceTypeService.create(request, currentUser.id()));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<ServiceTypeResponse>> findAll(
            @RequestParam(required = false) Boolean active,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(serviceTypeService.findAll(currentUser.id(), active));
    }

    @Override
    @GetMapping(ServiceTypeRoutes.BY_ID)
    public ResponseEntity<ServiceTypeResponse> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(serviceTypeService.findById(id, currentUser.id()));
    }

    @Override
    @PutMapping(ServiceTypeRoutes.BY_ID)
    public ResponseEntity<ServiceTypeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceTypeRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(serviceTypeService.update(id, request, currentUser.id()));
    }

    @Override
    @PatchMapping(ServiceTypeRoutes.STATUS)
    public ResponseEntity<ServiceTypeResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceTypeStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(serviceTypeService.updateStatus(id, request, currentUser.id()));
    }
}

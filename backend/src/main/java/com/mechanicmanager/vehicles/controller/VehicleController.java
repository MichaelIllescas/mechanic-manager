package com.mechanicmanager.vehicles.controller;

import com.mechanicmanager.shared.security.AuthenticatedUser;
import com.mechanicmanager.vehicles.dto.CreateVehicleRequest;
import com.mechanicmanager.vehicles.dto.UpdateVehicleRequest;
import com.mechanicmanager.vehicles.dto.VehicleResponse;
import com.mechanicmanager.vehicles.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping(VehicleRoutes.BASE)
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class VehicleController implements VehicleApi {

    private final VehicleService vehicleService;

    @Override
    @GetMapping
    public ResponseEntity<List<VehicleResponse>> findAll(
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(vehicleService.findAll(currentUser.id(), search));
    }

    @Override
    @PostMapping
    public ResponseEntity<VehicleResponse> create(
            @Valid @RequestBody CreateVehicleRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            UriComponentsBuilder uriBuilder
    ) {
        VehicleResponse created = vehicleService.create(currentUser.id(), request);

        return ResponseEntity
                .created(uriBuilder.path(VehicleRoutes.BASE + VehicleRoutes.BY_ID)
                        .buildAndExpand(created.id())
                        .toUri())
                .body(created);
    }

    @Override
    @PutMapping(VehicleRoutes.BY_ID)
    public ResponseEntity<VehicleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(vehicleService.update(currentUser.id(), id, request));
    }
}

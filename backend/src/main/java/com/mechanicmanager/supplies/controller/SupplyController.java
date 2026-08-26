package com.mechanicmanager.supplies.controller;

import com.mechanicmanager.shared.security.AuthenticatedUser;
import com.mechanicmanager.supplies.dto.CreateSupplyRequest;
import com.mechanicmanager.supplies.dto.SupplyResponse;
import com.mechanicmanager.supplies.dto.UpdateSupplyRequest;
import com.mechanicmanager.supplies.dto.UpdateSupplyStatusRequest;
import com.mechanicmanager.supplies.service.SupplyService;
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
@RequestMapping(SupplyRoutes.BASE)
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class SupplyController implements SupplyApi {

    private final SupplyService supplyService;

    @Override
    @PostMapping
    public ResponseEntity<SupplyResponse> create(
            @Valid @RequestBody CreateSupplyRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(supplyService.create(request, currentUser.id()));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<SupplyResponse>> findAll(
            @RequestParam(required = false) Boolean active,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(supplyService.findAll(currentUser.id(), active));
    }

    @Override
    @GetMapping(SupplyRoutes.BY_ID)
    public ResponseEntity<SupplyResponse> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(supplyService.findById(id, currentUser.id()));
    }

    @Override
    @PutMapping(SupplyRoutes.BY_ID)
    public ResponseEntity<SupplyResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupplyRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(supplyService.update(id, request, currentUser.id()));
    }

    @Override
    @PatchMapping(SupplyRoutes.STATUS)
    public ResponseEntity<SupplyResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupplyStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(supplyService.updateStatus(id, request, currentUser.id()));
    }
}

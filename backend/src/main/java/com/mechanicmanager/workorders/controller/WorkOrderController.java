package com.mechanicmanager.workorders.controller;

import com.mechanicmanager.shared.security.AuthenticatedUser;
import com.mechanicmanager.workorders.dto.CreateWorkOrderRequest;
import com.mechanicmanager.workorders.dto.UpdateWorkOrderRequest;
import com.mechanicmanager.workorders.dto.UpdateWorkOrderStatusRequest;
import com.mechanicmanager.workorders.dto.WorkOrderPageResponse;
import com.mechanicmanager.workorders.dto.WorkOrderResponse;
import com.mechanicmanager.workorders.model.WorkOrderStatus;
import com.mechanicmanager.workorders.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;

@RestController
@RequestMapping(WorkOrderRoutes.BASE)
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class WorkOrderController implements WorkOrderApi {

    private final WorkOrderService workOrderService;

    @Override
    @GetMapping
    public ResponseEntity<WorkOrderPageResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) WorkOrderStatus status,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(workOrderService.search(
                currentUser.id(),
                search,
                status,
                vehicleId,
                from,
                to,
                page,
                size
        ));
    }

    @Override
    @GetMapping(WorkOrderRoutes.BY_ID)
    public ResponseEntity<WorkOrderResponse> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(workOrderService.findById(currentUser.id(), id));
    }

    @Override
    @PostMapping
    public ResponseEntity<WorkOrderResponse> create(
            @Valid @RequestBody CreateWorkOrderRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            UriComponentsBuilder uriBuilder
    ) {
        WorkOrderResponse created = workOrderService.create(currentUser.id(), request);

        return ResponseEntity
                .created(uriBuilder.path(WorkOrderRoutes.BASE + WorkOrderRoutes.BY_ID)
                        .buildAndExpand(created.id())
                        .toUri())
                .body(created);
    }

    @Override
    @PutMapping(WorkOrderRoutes.BY_ID)
    public ResponseEntity<WorkOrderResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkOrderRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(workOrderService.update(
                currentUser.id(),
                id,
                request
        ));
    }

    @Override
    @PatchMapping(WorkOrderRoutes.STATUS)
    public ResponseEntity<WorkOrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkOrderStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(workOrderService.updateStatus(
                currentUser.id(),
                id,
                request
        ));
    }
}

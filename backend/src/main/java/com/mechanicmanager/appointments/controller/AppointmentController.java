package com.mechanicmanager.appointments.controller;

import com.mechanicmanager.appointments.dto.AppointmentResponse;
import com.mechanicmanager.appointments.dto.CreateAppointmentRequest;
import com.mechanicmanager.appointments.dto.UpdateAppointmentRequest;
import com.mechanicmanager.appointments.service.AppointmentService;
import com.mechanicmanager.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(AppointmentRoutes.BASE)
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class AppointmentController implements AppointmentApi {

    private final AppointmentService appointmentService;

    /**
     * La fecha es obligatoria y no tiene default: un "hoy" implícito dejaría
     * que la zona horaria del servidor decida qué día está viendo el taller.
     */
    @Override
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> findByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(appointmentService.findByDate(currentUser.id(), date));
    }

    @Override
    @PostMapping
    public ResponseEntity<AppointmentResponse> create(
            @Valid @RequestBody CreateAppointmentRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            UriComponentsBuilder uriBuilder
    ) {
        AppointmentResponse created = appointmentService.create(currentUser.id(), request);

        return ResponseEntity
                .created(uriBuilder.path(AppointmentRoutes.BASE + AppointmentRoutes.BY_ID)
                        .buildAndExpand(created.id())
                        .toUri())
                .body(created);
    }

    @Override
    @PutMapping(AppointmentRoutes.BY_ID)
    public ResponseEntity<AppointmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(appointmentService.update(currentUser.id(), id, request));
    }

    @Override
    @PostMapping(AppointmentRoutes.COMPLETE)
    public ResponseEntity<AppointmentResponse> complete(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(appointmentService.complete(currentUser.id(), id));
    }

    @Override
    @PostMapping(AppointmentRoutes.CANCEL)
    public ResponseEntity<AppointmentResponse> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(appointmentService.cancel(currentUser.id(), id));
    }
}

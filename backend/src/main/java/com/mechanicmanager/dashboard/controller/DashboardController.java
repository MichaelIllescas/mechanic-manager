package com.mechanicmanager.dashboard.controller;

import com.mechanicmanager.dashboard.dto.DashboardSummaryResponse;
import com.mechanicmanager.dashboard.service.GetDashboardSummaryUseCase;
import com.mechanicmanager.shared.config.ApiRoutes;
import com.mechanicmanager.shared.exception.ErrorResponse;
import com.mechanicmanager.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiRoutes.DASHBOARD)
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Resumen operativo del taller autenticado")
public class DashboardController {

    private final GetDashboardSummaryUseCase getDashboardSummaryUseCase;

    @GetMapping("/summary")
    @Operation(summary = "Obtener el resumen operativo del dashboard")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DashboardSummaryResponse> summary(
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(getDashboardSummaryUseCase.execute(currentUser.id()));
    }
}

package com.mechanicmanager.workorders.controller;

import com.mechanicmanager.shared.exception.ErrorResponse;
import com.mechanicmanager.shared.security.AuthenticatedUser;
import com.mechanicmanager.workorders.dto.CreateWorkOrderRequest;
import com.mechanicmanager.workorders.dto.UpdateWorkOrderRequest;
import com.mechanicmanager.workorders.dto.UpdateWorkOrderStatusRequest;
import com.mechanicmanager.workorders.dto.WorkOrderPageResponse;
import com.mechanicmanager.workorders.dto.WorkOrderResponse;
import com.mechanicmanager.workorders.model.WorkOrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;

@Tag(
        name = "Órdenes de trabajo",
        description = "Historial de trabajos realizados sobre los vehículos del usuario autenticado"
)
public interface WorkOrderApi {

    @Operation(
            summary = "Buscar órdenes de trabajo",
            description = "Busca por cliente o patente y permite filtrar por estado, vehículo y rango de fechas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página obtenida correctamente"),
            @ApiResponse(responseCode = "400", description = "Paginación inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<WorkOrderPageResponse> findAll(
            @Parameter(description = "Nombre del cliente o patente") String search,
            @Parameter(description = "Estado opcional") WorkOrderStatus status,
            @Parameter(description = "Identificador de vehículo opcional") Long vehicleId,
            @Parameter(description = "Fecha desde (inclusive)") LocalDate from,
            @Parameter(description = "Fecha hasta (inclusive)") LocalDate to,
            @Parameter(description = "Página basada en cero", example = "0") int page,
            @Parameter(description = "Resultados por página, máximo 50", example = "20") int size,
            @Parameter(hidden = true) AuthenticatedUser currentUser
    );

    @Operation(summary = "Obtener una orden de trabajo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden de trabajo obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Orden de trabajo no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<WorkOrderResponse> findById(
            @Parameter(description = "Identificador de la orden de trabajo", example = "1") Long id,
            @Parameter(hidden = true) AuthenticatedUser currentUser
    );

    @Operation(
            summary = "Crear una orden de trabajo",
            description = "Creación manual, opcionalmente vinculada a un turno y/o un presupuesto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orden de trabajo creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos o relaciones inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Vehículo, turno, presupuesto o concepto no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<WorkOrderResponse> create(
            CreateWorkOrderRequest request,
            @Parameter(hidden = true) AuthenticatedUser currentUser,
            @Parameter(hidden = true) UriComponentsBuilder uriBuilder
    );

    @Operation(summary = "Actualizar una orden de trabajo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden de trabajo actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos o relaciones inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Orden de trabajo o referencia no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<WorkOrderResponse> update(
            @Parameter(description = "Identificador de la orden de trabajo", example = "1") Long id,
            UpdateWorkOrderRequest request,
            @Parameter(hidden = true) AuthenticatedUser currentUser
    );

    @Operation(summary = "Cambiar el estado de una orden de trabajo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Orden de trabajo no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<WorkOrderResponse> updateStatus(
            @Parameter(description = "Identificador de la orden de trabajo", example = "1") Long id,
            UpdateWorkOrderStatusRequest request,
            @Parameter(hidden = true) AuthenticatedUser currentUser
    );

    @Operation(
            summary = "Eliminar una orden de trabajo",
            description = "Baja definitiva: no queda registro en el historial del vehículo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Orden de trabajo eliminada correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Orden de trabajo no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "Identificador de la orden de trabajo", example = "1") Long id,
            @Parameter(hidden = true) AuthenticatedUser currentUser
    );
}

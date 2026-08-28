package com.mechanicmanager.vehicles.controller;

import com.mechanicmanager.shared.exception.ErrorResponse;
import com.mechanicmanager.shared.security.AuthenticatedUser;
import com.mechanicmanager.vehicles.dto.CreateVehicleRequest;
import com.mechanicmanager.vehicles.dto.UpdateVehicleRequest;
import com.mechanicmanager.vehicles.dto.VehicleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Tag(
        name = "Vehículos",
        description = "Gestión de los vehículos pertenecientes al taller del usuario autenticado."
)
public interface VehicleApi {

    @Operation(
            summary = "Listar o buscar vehículos",
            description = "Sin search lista los vehículos del taller. Con search busca por patente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El usuario no tiene un taller asociado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<List<VehicleResponse>> findAll(
            @Parameter(description = "Patente opcional de búsqueda") String search,
            @Parameter(hidden = true) AuthenticatedUser currentUser
    );

    @Operation(summary = "Registrar un vehículo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vehículo registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El taller o el cliente no existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La patente ya existe en el taller",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<VehicleResponse> create(
            CreateVehicleRequest request,
            @Parameter(hidden = true) AuthenticatedUser currentUser,
            @Parameter(hidden = true) UriComponentsBuilder uriBuilder
    );

    @Operation(
            summary = "Editar un vehículo",
            description = "Solo permite editar vehículos y asociar clientes del taller autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehículo actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El vehículo o el cliente no existe en el taller",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La patente ya existe en el taller",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<VehicleResponse> update(
            @Parameter(description = "Identificador del vehículo", example = "1") Long id,
            UpdateVehicleRequest request,
            @Parameter(hidden = true) AuthenticatedUser currentUser
    );
}

package com.mechanicmanager.quotes.controller;

import com.mechanicmanager.quotes.dto.CreateQuoteRequest;
import com.mechanicmanager.quotes.dto.QuotePageResponse;
import com.mechanicmanager.quotes.dto.QuoteResponse;
import com.mechanicmanager.quotes.dto.UpdateQuoteRequest;
import com.mechanicmanager.quotes.dto.UpdateQuoteStatusRequest;
import com.mechanicmanager.quotes.model.QuoteStatus;
import com.mechanicmanager.shared.exception.ErrorResponse;
import com.mechanicmanager.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(
        name = "Presupuestos",
        description = "Gestión de presupuestos del usuario autenticado"
)
public interface QuoteApi {

    @Operation(
            summary = "Buscar presupuestos",
            description = "Busca por número, cliente o patente y permite filtrar por estado."
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
    ResponseEntity<QuotePageResponse> findAll(
            @Parameter(description = "Número, nombre del cliente o patente") String search,
            @Parameter(description = "Estado opcional") QuoteStatus status,
            @Parameter(description = "Página basada en cero", example = "0") int page,
            @Parameter(description = "Resultados por página, máximo 50", example = "20") int size,
            @Parameter(hidden = true) AuthenticatedUser currentUser
    );

    @Operation(summary = "Obtener un presupuesto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presupuesto obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<QuoteResponse> findById(
            @Parameter(description = "Identificador del presupuesto", example = "1") Long id,
            @Parameter(hidden = true) AuthenticatedUser currentUser
    );

    @Operation(summary = "Crear un presupuesto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Presupuesto creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos o relaciones inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente, vehículo o concepto no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<QuoteResponse> create(
            CreateQuoteRequest request,
            @Parameter(hidden = true) AuthenticatedUser currentUser,
            @Parameter(hidden = true) UriComponentsBuilder uriBuilder
    );

    @Operation(
            summary = "Actualizar un presupuesto",
            description = "Permite corregir el presupuesto independientemente de su estado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presupuesto actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos o relaciones inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Presupuesto o referencia no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<QuoteResponse> update(
            @Parameter(description = "Identificador del presupuesto", example = "1") Long id,
            UpdateQuoteRequest request,
            @Parameter(hidden = true) AuthenticatedUser currentUser
    );

    @Operation(summary = "Cambiar el estado de un presupuesto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<QuoteResponse> updateStatus(
            @Parameter(description = "Identificador del presupuesto", example = "1") Long id,
            UpdateQuoteStatusRequest request,
            @Parameter(hidden = true) AuthenticatedUser currentUser
    );

    @Operation(
            summary = "Eliminar un presupuesto",
            description = "Elimina el presupuesto y conserva el turno y el trabajo realizado vinculados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Presupuesto eliminado correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "Identificador del presupuesto", example = "1") Long id,
            @Parameter(hidden = true) AuthenticatedUser currentUser
    );
}

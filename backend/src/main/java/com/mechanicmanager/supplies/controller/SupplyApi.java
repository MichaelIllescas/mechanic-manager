package com.mechanicmanager.supplies.controller;

import com.mechanicmanager.shared.exception.ErrorResponse;
import com.mechanicmanager.shared.security.AuthenticatedUser;
import com.mechanicmanager.supplies.dto.CreateSupplyRequest;
import com.mechanicmanager.supplies.dto.SupplyResponse;
import com.mechanicmanager.supplies.dto.UpdateSupplyRequest;
import com.mechanicmanager.supplies.dto.UpdateSupplyStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(
        name = "Catálogo de insumos",
        description = """
                Insumos y repuestos que utiliza el taller: aceite, filtros, pastillas, bujías.

                Es catálogo, no existencias. Cuántas unidades hay en el taller queda fuera
                del MVP: acá sólo vive el material y su precio de referencia.

                Los insumos no se eliminan: se desactivan, para no romper el historial
                de presupuestos y órdenes que los referencian.
                """
)
public interface SupplyApi {

    @Operation(
            summary = "Crear un insumo",
            description = "Agrega un insumo al catálogo del usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Insumo creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Alguno de los campos enviados tiene un formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un insumo con ese nombre",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SupplyResponse> create(CreateSupplyRequest request, AuthenticatedUser currentUser);

    @Operation(
            summary = "Listar el catálogo",
            description = """
                    Devuelve los insumos del usuario autenticado, ordenados por nombre.

                    Sin el parámetro `active` devuelve el catálogo completo. Con `active=true`
                    devuelve solo los disponibles para cargar en presupuestos y órdenes.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catálogo obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<List<SupplyResponse>> findAll(Boolean active, AuthenticatedUser currentUser);

    @Operation(
            summary = "Obtener un insumo",
            description = "Devuelve un insumo del catálogo del usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insumo obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontró el insumo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SupplyResponse> findById(Long id, AuthenticatedUser currentUser);

    @Operation(
            summary = "Actualizar un insumo",
            description = """
                    Actualiza el nombre y el precio.

                    Modificar el precio no altera los presupuestos ni las órdenes ya
                    registrados: esos conservan el valor que tenían al crearse.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insumo actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Alguno de los campos enviados tiene un formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontró el insumo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe otro insumo con ese nombre",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SupplyResponse> update(Long id, UpdateSupplyRequest request, AuthenticatedUser currentUser);

    @Operation(
            summary = "Activar o desactivar un insumo",
            description = """
                    Reemplaza al borrado. Un insumo desactivado deja de ofrecerse al cargar
                    presupuestos y órdenes, pero sigue disponible para el historial que lo usa.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "El estado enviado es inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontró el insumo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<SupplyResponse> updateStatus(Long id, UpdateSupplyStatusRequest request, AuthenticatedUser currentUser);
}

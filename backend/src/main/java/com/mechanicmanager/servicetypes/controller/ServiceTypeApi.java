package com.mechanicmanager.servicetypes.controller;

import com.mechanicmanager.servicetypes.dto.CreateServiceTypeRequest;
import com.mechanicmanager.servicetypes.dto.ServiceTypeResponse;
import com.mechanicmanager.servicetypes.dto.UpdateServiceTypeRequest;
import com.mechanicmanager.servicetypes.dto.UpdateServiceTypeStatusRequest;
import com.mechanicmanager.shared.exception.ErrorResponse;
import com.mechanicmanager.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(
        name = "Catálogo de servicios",
        description = """
                Servicios que ofrece el taller: cambio de aceite, alineación, cambio de pastillas.

                Es catálogo, no trabajo realizado. El trabajo efectivamente ejecutado sobre
                un vehículo se registra en las órdenes de trabajo.

                Los servicios no se eliminan: se desactivan, para no romper el historial
                de presupuestos y órdenes que los referencian.
                """
)
public interface ServiceTypeApi {

    @Operation(
            summary = "Crear un servicio",
            description = "Agrega un servicio al catálogo del usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Servicio creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Alguno de los campos enviados tiene un formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un servicio con ese nombre",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ServiceTypeResponse> create(CreateServiceTypeRequest request, AuthenticatedUser currentUser);

    @Operation(
            summary = "Listar el catálogo",
            description = """
                    Devuelve los servicios del usuario autenticado, ordenados por nombre.

                    Sin el parámetro `active` devuelve el catálogo completo. Con `active=true`
                    devuelve solo los disponibles para cargar en presupuestos y órdenes.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catálogo obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<List<ServiceTypeResponse>> findAll(Boolean active, AuthenticatedUser currentUser);

    @Operation(
            summary = "Obtener un servicio",
            description = "Devuelve un servicio del catálogo del usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Servicio obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontró el servicio",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ServiceTypeResponse> findById(Long id, AuthenticatedUser currentUser);

    @Operation(
            summary = "Actualizar un servicio",
            description = """
                    Actualiza el nombre, la descripción, el precio y la duración estimada.

                    Modificar el precio no altera los presupuestos ni las órdenes ya
                    registrados: esos conservan el valor que tenían al crearse.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Servicio actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Alguno de los campos enviados tiene un formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontró el servicio",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe otro servicio con ese nombre",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ServiceTypeResponse> update(Long id, UpdateServiceTypeRequest request, AuthenticatedUser currentUser);

    @Operation(
            summary = "Activar o desactivar un servicio",
            description = """
                    Reemplaza al borrado. Un servicio desactivado deja de ofrecerse al cargar
                    presupuestos y órdenes, pero sigue disponible para el historial que lo usa.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "El estado enviado es inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontró el servicio",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ServiceTypeResponse> updateStatus(Long id, UpdateServiceTypeStatusRequest request, AuthenticatedUser currentUser);
}

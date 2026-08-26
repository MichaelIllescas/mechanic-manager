package com.mechanicmanager.workshops.controller;

import com.mechanicmanager.shared.exception.ErrorResponse;
import com.mechanicmanager.shared.security.AuthenticatedUser;
import com.mechanicmanager.workshops.dto.UpdateWorkshopRequest;
import com.mechanicmanager.workshops.dto.WorkshopResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "Taller",
        description = """
                Datos del taller del usuario autenticado.
                
                El taller se crea vacío junto con la cuenta, por lo que todos sus
                campos pueden venir en null hasta que el usuario los complete.
                """
)
public interface WorkshopApi {
    
    @Operation(
            summary = "Obtener el taller propio",
            description = "Devuelve los datos del taller asociado al usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Taller obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El usuario no tiene un taller asociado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<WorkshopResponse> findMine(AuthenticatedUser currentUser);
    
    @Operation(
            summary = "Actualizar el taller propio",
            description = """
                    Actualiza los datos del taller del usuario autenticado.
                    
                    Todos los campos son opcionales: se puede guardar el taller
                    parcialmente cargado. Los campos enviados vacíos se guardan como null.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Taller actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Alguno de los campos enviados tiene un formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El usuario no tiene un taller asociado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<WorkshopResponse> update(UpdateWorkshopRequest request, AuthenticatedUser currentUser);
}

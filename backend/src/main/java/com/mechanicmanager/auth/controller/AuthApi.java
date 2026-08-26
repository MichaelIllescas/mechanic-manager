package com.mechanicmanager.auth.controller;

import com.mechanicmanager.auth.dto.LoginRequest;
import com.mechanicmanager.auth.dto.LoginResponse;
import com.mechanicmanager.shared.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "Autenticación",
        description = "Obtención del token de acceso a la API."
)
public interface AuthApi {
    
    @Operation(
            summary = "Iniciar sesión",
            description = """
                    Valida las credenciales y devuelve un token JWT.
                    
                    El token se envía en las siguientes peticiones mediante la cabecera
                    Authorization con el prefijo Bearer. El email no distingue mayúsculas.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación correcta"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Email o contraseña incorrectos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "La cuenta está deshabilitada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<LoginResponse> login(LoginRequest request);
}

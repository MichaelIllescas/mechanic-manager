package com.mechanicmanager.auth.controller;

import com.mechanicmanager.auth.dto.ChangePasswordRequest;
import com.mechanicmanager.auth.dto.ForgotPasswordRequest;
import com.mechanicmanager.auth.dto.LoginRequest;
import com.mechanicmanager.auth.dto.LoginResponse;
import com.mechanicmanager.auth.dto.ResetPasswordRequest;
import com.mechanicmanager.shared.exception.ErrorResponse;
import com.mechanicmanager.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Demasiados intentos fallidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<LoginResponse> login(LoginRequest request, HttpServletRequest httpRequest);

    @Operation(
            summary = "Solicitar recuperación de contraseña",
            description = """
                    Envía por email un enlace para elegir una contraseña nueva.
                    
                    El envío se encola y la respuesta no espera al servidor de correo.
                    
                    La respuesta es siempre 204, exista o no una cuenta con ese email:
                    el endpoint es público y no debe revelar qué direcciones están
                    registradas. Cada pedido invalida los enlaces anteriores.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pedido registrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Demasiados pedidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest);
    
    @Operation(
            summary = "Restablecer contraseña",
            description = """
                    Cambia la contraseña usando el token recibido por email.
                    
                    El token es de un solo uso y vence a los pocos minutos.
                    """
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contraseña actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o enlace vencido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Demasiados intentos con enlaces inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> resetPassword(ResetPasswordRequest request, HttpServletRequest httpRequest);
    
    @Operation(
            summary = "Cambiar la propia contraseña",
            description = """
                    Cambia la contraseña del usuario autenticado.
                    
                    Exige la contraseña actual: evita que alguien con la sesión abierta
                    en un equipo ajeno se apropie de la cuenta. La sesión en curso sigue
                    siendo válida después del cambio.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contraseña actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "La contraseña actual es incorrecta",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> changePassword(ChangePasswordRequest request, AuthenticatedUser currentUser);
}

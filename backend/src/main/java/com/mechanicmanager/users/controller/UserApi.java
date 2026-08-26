package com.mechanicmanager.users.controller;

import com.mechanicmanager.shared.exception.ErrorResponse;
import com.mechanicmanager.shared.security.AuthenticatedUser;
import com.mechanicmanager.users.dto.CreateUserRequest;
import com.mechanicmanager.users.dto.UpdateUserRequest;
import com.mechanicmanager.users.dto.UpdateUserStatusRequest;
import com.mechanicmanager.users.dto.UserResponse;
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
        name = "Usuarios",
        description = """
                Administración de las cuentas de usuario de la plataforma.
                
                Todos los endpoints requieren un usuario autenticado con rol SYSTEM_ADMIN.
                """
)
public interface UserApi {
    
    @Operation(
            summary = "Listar usuarios",
            description = "Devuelve todas las cuentas de la plataforma, habilitadas y deshabilitadas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no es SYSTEM_ADMIN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<List<UserResponse>> findAll();
    
    @Operation(
            summary = "Obtener un usuario",
            description = "Devuelve los datos de una cuenta a partir de su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no es SYSTEM_ADMIN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese id",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<UserResponse> findById(
            @Parameter(description = "Identificador del usuario", example = "1") Long id
    );
    
    @Operation(
            summary = "Crear un usuario",
            description = """
                    Crea una cuenta con la contraseña inicial definida por el administrador.
                    
                    La cuenta se crea habilitada y marcada para cambio de contraseña obligatorio
                    en el primer inicio de sesión. El email se normaliza a minúsculas.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado. La cabecera Location apunta al recurso."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no es SYSTEM_ADMIN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un usuario con ese email",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<UserResponse> create(CreateUserRequest request, UriComponentsBuilder uriBuilder);
    
    @Operation(
            summary = "Actualizar un usuario",
            description = """
                    Modifica el nombre, el email y el rol de una cuenta.
                    
                    No modifica la contraseña ni el estado. Cambiar el email invalida la sesión
                    activa del usuario, porque el token se emite contra su email.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no es SYSTEM_ADMIN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese id",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El email ya pertenece a otro usuario",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<UserResponse> update(
            @Parameter(description = "Identificador del usuario", example = "1") Long id,
            UpdateUserRequest request
    );
    
    @Operation(
            summary = "Habilitar o deshabilitar un usuario",
            description = """
                    Cambia el estado de la cuenta.
                    
                    Deshabilitar tiene efecto inmediato: el usuario pierde el acceso en su
                    siguiente petición aunque su token siga vigente. Un administrador no puede
                    deshabilitar su propia cuenta.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No es SYSTEM_ADMIN, o intenta deshabilitarse a sí mismo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese id",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<UserResponse> updateStatus(
            @Parameter(description = "Identificador del usuario", example = "1") Long id,
            UpdateUserStatusRequest request,
            @Parameter(hidden = true) AuthenticatedUser currentUser
    );
}

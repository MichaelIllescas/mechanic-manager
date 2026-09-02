package com.mechanicmanager.appointments.controller;

import com.mechanicmanager.appointments.dto.AppointmentResponse;
import com.mechanicmanager.appointments.dto.CreateAppointmentRequest;
import com.mechanicmanager.appointments.dto.UpdateAppointmentRequest;
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

import java.time.LocalDate;
import java.util.List;

@Tag(
        name = "Turnos",
        description = "Agenda de turnos del taller del usuario autenticado. "
                + "Un turno se agenda sobre un vehículo y el cliente se deduce de él."
)
public interface AppointmentApi {

    @Operation(
            summary = "Consultar la agenda de un día",
            description = "Devuelve los turnos de la fecha indicada, ordenados por horario. "
                    + "La fecha es obligatoria: la agenda siempre se consulta por día."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agenda obtenida correctamente"),
            @ApiResponse(responseCode = "400", description = "Fecha ausente o con formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<List<AppointmentResponse>> findByDate(
            @Parameter(description = "Día de la agenda", example = "2026-09-10") LocalDate date,
            @Parameter(hidden = true) AuthenticatedUser currentUser
    );

    @Operation(
            summary = "Agendar un turno",
            description = "El turno nace en estado SCHEDULED. La agenda no valida solapamiento: "
                    + "el taller puede atender varios vehículos en el mismo horario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Turno agendado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El vehículo no existe en el taller",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<AppointmentResponse> create(
            CreateAppointmentRequest request,
            @Parameter(hidden = true) AuthenticatedUser currentUser,
            @Parameter(hidden = true) UriComponentsBuilder uriBuilder
    );

    @Operation(
            summary = "Reprogramar un turno",
            description = "Modifica fecha y notas de un turno agendado. El vehículo no se cambia: "
                    + "si el turno se cargó sobre el auto equivocado se cancela y se crea otro."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turno actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El turno no existe en el taller",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El turno ya está completado o cancelado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<AppointmentResponse> update(
            @Parameter(description = "Identificador del turno", example = "1") Long id,
            UpdateAppointmentRequest request,
            @Parameter(hidden = true) AuthenticatedUser currentUser
    );

    @Operation(
            summary = "Completar un turno",
            description = "Marca que el vehículo ingresó al taller. A partir de ahí el estado del "
                    + "trabajo lo lleva la orden de trabajo, no el turno."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turno completado correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El turno no existe en el taller",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El turno no está agendado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<AppointmentResponse> complete(
            @Parameter(description = "Identificador del turno", example = "1") Long id,
            @Parameter(hidden = true) AuthenticatedUser currentUser
    );

    @Operation(
            summary = "Cancelar un turno",
            description = "El turno no se elimina: queda en el historial del vehículo como cancelado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turno cancelado correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol USER",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El turno no existe en el taller",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El turno no está agendado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<AppointmentResponse> cancel(
            @Parameter(description = "Identificador del turno", example = "1") Long id,
            @Parameter(hidden = true) AuthenticatedUser currentUser
    );
}

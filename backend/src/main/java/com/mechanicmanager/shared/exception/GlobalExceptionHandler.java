package com.mechanicmanager.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(ErrorResponse.of(e.getCode(), e.getMessage()));
    }

    /**
     * Rechazos de @PreAuthorize: llegan como excepción del método, no del filter
     * chain, así que no pasan por RestAccessDeniedHandler. Se responde igual que él.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("ACCESS_DENIED", "No tenés permisos para esta operación"));
    }

    /** Igual que el caso anterior, pero para un pedido sin autenticar que llegó hasta el método. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("UNAUTHENTICATED", "Se requiere autenticación"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("VALIDATION_ERROR", "Datos inválidos", details));
    }

    /** JSON mal formado, o un valor que no se puede convertir al tipo del campo. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException e) {
        log.debug("Cuerpo de la solicitud ilegible", e);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("INVALID_REQUEST_BODY",
                        "La solicitud no tiene un formato válido"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleParameterType(MethodArgumentTypeMismatchException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("INVALID_PARAMETER",
                        "El valor del parámetro '" + e.getName() + "' no es válido"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException e
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("INVALID_PARAMETER",
                        "Falta el parámetro '" + e.getParameterName() + "'"));
    }

    /**
     * La base rechazó la operación por una restricción (unicidad, clave foránea).
     * El detalle nombra tablas y restricciones: va al log, no al cliente.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("Violación de restricción de la base: {}", e.getMostSpecificCause().getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("DATA_CONFLICT",
                        "La operación entra en conflicto con datos existentes"));
    }

    /**
     * Red de seguridad: todo lo que nadie previó. Las excepciones del propio
     * framework web (ruta inexistente, método no permitido, tipo de contenido
     * no soportado) ya traen su status y se respetan; el resto es un 500 con
     * mensaje genérico y el detalle solo en el log.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        if (e instanceof org.springframework.web.ErrorResponse frameworkError
                && frameworkError.getStatusCode().is4xxClientError()) {
            HttpStatusCode status = frameworkError.getStatusCode();

            return ResponseEntity
                    .status(status)
                    .body(ErrorResponse.of("REQUEST_ERROR", frameworkMessage(status)));
        }

        log.error("Error no controlado", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", "Ocurrió un error inesperado"));
    }

    private String frameworkMessage(HttpStatusCode status) {
        return switch (status.value()) {
            case 404 -> "No se encontró el recurso solicitado";
            case 405 -> "Método no permitido para este recurso";
            case 415 -> "Tipo de contenido no soportado";
            default -> "La solicitud no es válida";
        };
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}

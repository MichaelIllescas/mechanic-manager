# Manejo de excepciones

Estrategia de errores de la aplicación. Todo error que llega al cliente sale
por acá, con un formato único.

## Principio

Los servicios **lanzan excepciones**, no devuelven códigos de error ni `null`
para señalar fallas. Nadie captura excepciones para transformarlas en
respuestas HTTP: de eso se encarga `GlobalExceptionHandler`, una sola vez,
para toda la aplicación.

Esto mantiene los controllers delgados, como pide la sección 4.1 del
[Technical Design](../../../../../../../../docs/03-architecture/01-technical-design.md).

## Piezas

| Clase | Rol |
|---|---|
| `AppException` | Base abstracta de todas las excepciones propias. Lleva su `code` y su `HttpStatus` |
| `ErrorResponse` | Contrato JSON de error. Coincide con el schema `ErrorResponse` del `openapi.yaml` |
| `GlobalExceptionHandler` | `@RestControllerAdvice`. Traduce excepciones a respuestas HTTP |
| `ResourceNotFoundException` | Genérica, 404 |
| `AccessDeniedAppException` | Genérica, 403. Para acciones que el usuario autenticado no puede hacer sobre su propia cuenta (por ejemplo, deshabilitarse a sí mismo). Un recurso de otro usuario **no** la usa: responde 404 |

## Cómo agregar una excepción nueva

Heredá de `AppException` y definí en el constructor el código y el status.
La excepción sabe qué error representa; el handler no necesita enterarse.

```java
public class LicensePlateAlreadyExistsException extends AppException {

    public LicensePlateAlreadyExistsException(String licensePlate) {
        super("LICENSE_PLATE_ALREADY_EXISTS", HttpStatus.CONFLICT,
                "Ya existe un vehículo con la patente " + licensePlate);
    }
}
```

No hay que tocar `GlobalExceptionHandler`: el `@ExceptionHandler(AppException.class)`
ya cubre toda la jerarquía.

## Dónde vive cada excepción

- **Genéricas y reutilizables por varios módulos** → `shared/exception`.
- **Propias de un módulo** → `<modulo>/exception`.
  Ejemplo: `auth/exception/InvalidCredentialsException`.

Si una excepción de módulo empieza a usarse desde otros dos módulos, es señal
de que hay que moverla a `shared`.

## Convenciones

**El `code` es para el frontend, el `message` para la persona.**
El código es estable y en `SCREAMING_SNAKE_CASE`; sirve para que el cliente
decida qué hacer sin parsear texto. El mensaje está en español, redactado para
que un usuario lo entienda, y puede cambiar sin romper nada.

**Los mensajes no filtran información sensible.**
`InvalidCredentialsException` dice "Email o contraseña incorrectos" y nunca
cuál de los dos falló: distinguirlos le confirma a un atacante qué emails
existen en el sistema.

**Nunca exponer excepciones del framework.**
`BadCredentialsException`, `DataIntegrityViolationException` y similares son
detalles de implementación. El módulo que las produce las traduce a una
`AppException` propia. Ver `AuthService.authenticate()` como ejemplo.

**El status vive en la excepción.**
`AppException` conoce `HttpStatus`, o sea que el dominio conoce HTTP. Es una
concesión deliberada: la alternativa es un `switch` de código a status en el
handler que crece con cada excepción nueva. Para este MVP, la simplicidad gana
(sección 12 del Technical Design: KISS).

## Errores que no pasan por el handler

`@RestControllerAdvice` solo captura lo que ocurre **dentro** de un controller.
Los rechazos de Spring Security suceden antes, en el filter chain, y por eso
tienen sus propios responsables en `shared/security`:

| Situación | Componente | Respuesta |
|---|---|---|
| Falta el token o es inválido | `RestAuthenticationEntryPoint` | 401 `UNAUTHENTICATED` |
| Autenticado pero sin permisos | `RestAccessDeniedHandler` | 403 `ACCESS_DENIED` |

Ambos escriben el mismo `ErrorResponse`, así que el cliente ve un formato
uniforme sin importar dónde se haya frenado el request.

## Códigos en uso

| `code` | Status | Origen |
|---|---|---|
| `VALIDATION_ERROR` | 400 | Bean Validation (`@Valid`). `details` lista `campo: mensaje` |
| `INVALID_REQUEST_BODY` | 400 | JSON mal formado o valor de tipo incorrecto |
| `INVALID_PARAMETER` | 400 | Parámetro con valor inválido o ausente |
| `INVALID_PASSWORD_RESET_TOKEN` | 400 | `auth` |
| `PASSWORD_NOT_CHANGED` | 400 | `auth` |
| `INVALID_QUOTE` | 400 | `quotes` |
| `INVALID_WORK_ORDER` | 400 | `workorders` |
| `INVALID_APPOINTMENT_QUOTE` | 400 | `appointments` |
| `INVALID_DATE_RANGE` | 400 | `appointments` |
| `INVALID_CREDENTIALS` | 401 | `auth` |
| `INVALID_CURRENT_PASSWORD` | 401 | `auth` |
| `UNAUTHENTICATED` | 401 | Filter chain, o un pedido sin autenticar que llegó al método |
| `ACCOUNT_DISABLED` | 403 | `auth`. Solo se informa si la contraseña es correcta |
| `ACCESS_DENIED` | 403 | Filter chain, `@PreAuthorize` o acción propia no permitida |
| `PASSWORD_CHANGE_REQUIRED` | 403 | `PasswordChangeRequiredFilter`: la contraseña es temporal |
| `RESOURCE_NOT_FOUND` | 404 | Genérico. También un recurso de otro usuario |
| `REQUEST_ERROR` | 404 / 405 / 415 | Errores 4xx del propio framework (ruta, método, tipo de contenido) |
| `EMAIL_ALREADY_EXISTS` | 409 | `users` |
| `LICENSE_PLATE_ALREADY_EXISTS` | 409 | `vehicles` |
| `SERVICE_TYPE_NAME_ALREADY_EXISTS` | 409 | `servicetypes` |
| `SUPPLY_NAME_ALREADY_EXISTS` | 409 | `supplies` |
| `WORKSHOP_INCOMPLETE` | 409 | `workshops` |
| `APPOINTMENT_NOT_EDITABLE` | 409 | `appointments` |
| `INVALID_APPOINTMENT_STATUS_TRANSITION` | 409 | `appointments` |
| `WORK_ORDER_ALREADY_EXISTS` | 409 | `workorders` |
| `QUOTE_HAS_WORK_ORDER` | 409 | `quotes` |
| `DATA_CONFLICT` | 409 | Restricción de la base no prevista por el servicio |
| `TOO_MANY_ATTEMPTS` | 429 | `auth`: límite de intentos |
| `INTERNAL_ERROR` | 500 | Excepción no prevista; el detalle queda solo en el log |
| `EMAIL_DELIVERY_FAILED` | 503 | `shared/email`. El envío es asíncrono: se registra en el log y no llega al cliente |

Mantener esta tabla al día al agregar excepciones nuevas.

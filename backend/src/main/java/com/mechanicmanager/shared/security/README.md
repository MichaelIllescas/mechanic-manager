# Seguridad

Autenticación y autorización de la aplicación. Implementa los requisitos de la
sección 8 del [Technical Design](../../../../../../../../docs/03-architecture/01-technical-design.md).

## Modelo

Autenticación **stateless** con JWT. No hay sesión en el servidor: cada request
llega con su token en el header `Authorization` y se autentica por sí solo.

```text
POST /api/v1/auth/login
        │
        ▼
AuthenticationManager  ──►  AppUserDetailsService  ──►  users (BD)
        │                          │
        │                   BCryptPasswordEncoder
        ▼
   JwtService.generateToken()
        │
        ▼
   token al cliente


Request siguiente con Authorization: Bearer <token>
        │
        ▼
JwtAuthenticationFilter
        ├── JwtService.extractEmail()      verifica firma y expiración
        ├── AppUserDetailsService          recarga el usuario desde la BD
        ├── verifica que siga habilitado
        └── SecurityContextHolder.setAuthentication()
        │
        ▼
PasswordChangeRequiredFilter   corta con 403 si la contraseña es temporal
        │
        ▼
SecurityFilterChain  ──►  Controller
```

## Piezas

| Clase | Rol |
|---|---|
| `SecurityConfig` | Filter chain, reglas de acceso por ruta, `PasswordEncoder`, `AuthenticationManager` |
| `JwtService` | Firma y verificación de tokens HS256 |
| `JwtAuthenticationFilter` | Lee el token de cada request y puebla el `SecurityContext` |
| `PasswordChangeRequiredFilter` | Mientras la cuenta tenga contraseña temporal, rechaza con 403 todo salvo `POST /auth/change-password` |
| `AttemptLimiter` | Cuenta eventos por clave en una ventana deslizante, en memoria. Lo usa `auth` para limitar intentos |
| `AuthenticatedUser` | El `UserDetails` de la aplicación. Es el principal autenticado |
| `AppUserDetailsService` | Carga el usuario por email para Spring Security |
| `RestAuthenticationEntryPoint` | Responde 401 en JSON cuando falta autenticación |
| `RestAccessDeniedHandler` | Responde 403 en JSON cuando faltan permisos |

El endpoint de login y su service viven en el módulo `auth`, no acá. Este
paquete provee el mecanismo; `auth` expone el caso de uso.

## Decisiones y por qué

**El filtro recarga el usuario desde la base en cada request.**
Podría confiar en los claims del token y ahorrarse la consulta, pero entonces
un usuario deshabilitado seguiría entrando hasta que su token expire. Con la
recarga, un `SYSTEM_ADMIN` que deshabilita una cuenta la deja afuera en el
request siguiente. Los claims `uid` y `role` del token son informativos para el
frontend; **la autorización nunca se apoya en ellos**.

**El filtro nunca corta la cadena.**
Si el token falta o es inválido, no autentica y sigue. Quién decide si eso es
un 401 es el `SecurityFilterChain`. Separar "identificar" de "autorizar"
mantiene el filtro simple y evita duplicar reglas de acceso.

**`anyRequest().authenticated()` — lista blanca, no lista negra.**
Todo endpoint nuevo nace protegido. Para abrirlo hay que agregarlo
explícitamente en `SecurityConfig`. Un olvido deja un endpoint inaccesible,
nunca uno expuesto.

**CSRF deshabilitado.**
CSRF protege sesiones basadas en cookies, donde el navegador adjunta la
credencial automáticamente. Con un JWT en el header `Authorization` eso no
ocurre y el ataque no aplica. **No copiar esta decisión a un proyecto con
sesiones.**

**Sin logout ni refresh token.**
Con JWT stateless el logout no existe del lado del servidor: el token vale
hasta que expira. El logout real es el frontend descartando el token.
Implementarlo "de verdad" exigiría una blacklist en base o Redis, o sea
reintroducir el estado que la arquitectura eligió evitar. El refresh token
tampoco está en el alcance del MVP. Ambos se revisarán si la validación con
clientes lo justifica, y quedarán registrados en un ADR.

**Cambiar la contraseña no invalida los tokens ya emitidos (riesgo aceptado).**
Un token robado sigue válido hasta que vence (60 minutos por defecto, ver
`JWT_EXPIRATION_MINUTES`), aunque el dueño cambie su contraseña. La ventana es
corta y exige que el token ya haya sido robado. Si la validación con clientes lo
justifica, la salida más barata es incluir en el token una huella de la
contraseña vigente y compararla en `JwtAuthenticationFilter`: no requiere
migración ni consultas extra.

**Una contraseña temporal solo permite cambiarla.**
Cuando un administrador crea una cuenta, esta queda con `mustChangePassword`.
`PasswordChangeRequiredFilter` responde 403 `PASSWORD_CHANGE_REQUIRED` a todo
salvo `POST /auth/change-password`. Se hace cumplir en el servidor y no solo en
la interfaz, porque quien llama a la API directamente no pasa por sus pantallas.

**Los intentos de login y de recuperación están limitados.**
`AuthService` y los casos de uso de recuperación consultan `AttemptLimiter`:
5 fallos por email y 20 por IP en el login, con ventana de 15 minutos; después
responden 429 `TOO_MANY_ATTEMPTS`. Es en memoria: se pierde al reiniciar y no
se comparte entre instancias. La IP sale de `request.getRemoteAddr()`; detrás
de un proxy, en `prod` se activa `server.forward-headers-strategy=native`.

**El login no revela qué cuentas están deshabilitadas.**
Spring rechaza a una cuenta deshabilitada antes de mirar la contraseña.
`AuthService` verifica la contraseña en ese caso, y solo con la correcta
responde `ACCOUNT_DISABLED`; si no, responde credenciales inválidas.

**El email es el `username`.**
En Spring Security "username" es solo el identificador de login. El nuestro es
el email, y se normaliza a minúsculas **en el service** antes de consultar,
porque la búsqueda del repositorio es exacta.

**El prefijo `ROLE_` es obligatorio.**
`AuthenticatedUser.getAuthorities()` devuelve `ROLE_USER` / `ROLE_SYSTEM_ADMIN`
porque es lo que espera `hasRole()`. Sin el prefijo, las reglas de autorización
no matchean nunca y fallan en silencio.

## Configuración

| Propiedad | Origen | Descripción |
|---|---|---|
| `security.jwt.secret` | `JWT_SECRET` | Clave HMAC en base64. Mínimo 32 bytes |
| `security.jwt.expiration-minutes` | `JWT_EXPIRATION_MINUTES` | Validez del token. Default 60 |
| `security.rate-limit.window-minutes` | (sin variable) | Ventana del límite de intentos. Default 15 |

Las variables se cargan desde `backend/.env`, que **no se commitea**. Ver
`.env.example` para la plantilla y el comando de generación del secreto.

El secreto no tiene valor por defecto a propósito: si falta, la aplicación no
arranca, con un mensaje que explica qué variable definir. Es preferible a que un
entorno quede firmando con una clave conocida. El perfil `dev` es el que se
activa si nadie define `SPRING_PROFILES_ACTIVE`, y una clave por defecto
permitiría fabricar tokens en un despliegue mal configurado.

## Reglas de acceso vigentes

| Ruta | Acceso |
|---|---|
| `POST /api/v1/auth/login`, `/auth/forgot-password`, `/auth/reset-password` | Público |
| `/v3/api-docs/**`, `/swagger-ui/**` | Público en `dev`; deshabilitados en `prod` |
| `POST /api/v1/auth/change-password` | Autenticado. Única ruta permitida con contraseña temporal |
| `/api/v1/admin/**` | `SYSTEM_ADMIN`, con `@PreAuthorize` en el controller |
| Endpoints operativos (clientes, vehículos, turnos, presupuestos, servicios, insumos, trabajos, taller, dashboard) | `USER`, con `@PreAuthorize("hasRole('USER')")` en cada controller |
| Todo lo demás | Autenticado |

La autorización por rol vive en cada controller y no en `SecurityConfig`: un
controller nuevo sin `@PreAuthorize` queda accesible para cualquier usuario
autenticado, así que la anotación es parte de crearlo.

## Aislamiento de datos (ownership)

La autorización por rol no alcanza: cada usuario debe operar **solamente** sobre
sus propios datos (ADR-003).

1. el controller recibe el `userId` del principal autenticado
   (`@AuthenticationPrincipal AuthenticatedUser`), nunca del cliente;
2. cada caso de uso filtra por `user_id` (`findByIdAndUserId` y similares);
3. un identificador que pertenece a otro usuario responde **404**
   (`ResourceNotFoundException`), no 403, para no revelar que existe;
4. en `vehicles` y `appointments` la base lo refuerza con claves foráneas
   compuestas `(id, user_id)`.

`AccessDeniedAppException` no se usa para esto.

Regla crítica: `SYSTEM_ADMIN` y `USER` son contextos distintos. Ser
administrador de la plataforma **no** habilita el acceso a los datos operativos
de ningún taller: los endpoints operativos exigen el rol `USER`.

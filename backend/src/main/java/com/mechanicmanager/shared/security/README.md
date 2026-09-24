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
SecurityFilterChain  ──►  Controller
```

## Piezas

| Clase | Rol |
|---|---|
| `SecurityConfig` | Filter chain, reglas de acceso por ruta, `PasswordEncoder`, `AuthenticationManager` |
| `JwtService` | Firma y verificación de tokens HS256 |
| `JwtAuthenticationFilter` | Lee el token de cada request y puebla el `SecurityContext` |
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

Las variables se cargan desde `backend/.env`, que **no se commitea**. Ver
`.env.example` para la plantilla y el comando de generación del secreto.

El secreto no tiene valor por defecto a propósito: si falta, la aplicación no
arranca. Es preferible a que un entorno quede firmando con una clave conocida.

## Reglas de acceso vigentes

| Ruta | Acceso |
|---|---|
| `POST /api/v1/auth/login` | Público |
| `/v3/api-docs/**`, `/swagger-ui/**` | Público |
| `/api/v1/admin/**` | `SYSTEM_ADMIN` |
| Todo lo demás | Autenticado |

## Pendiente: ownership de taller

La autorización por rol no alcanza. La sección 6 del Technical Design exige
además que cada usuario opere **solamente** dentro de su propio taller:

1. el usuario autenticado determina su único `workshopId`;
2. las consultas se restringen a ese taller;
3. un `workshopId` enviado por el frontend **nunca** autoriza por sí solo.

Esa validación se aplica en los **services** de cada módulo de negocio, no en
este paquete, y usa `AccessDeniedAppException` cuando se viola. Está pendiente
de implementación junto con el módulo `workshops`.

Regla crítica de la sección 8: `SYSTEM_ADMIN` y `USER` son contextos distintos.
Ser administrador de la plataforma **no** habilita el acceso a los datos
operativos de ningún taller.

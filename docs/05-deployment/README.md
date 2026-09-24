# Deployment / Infraestructura

**Versión:** 1.1
**Estado:** Entorno local documentado. La infraestructura de despliegue está pendiente.

---

Este documento describe lo que existe hoy y lo que falta antes del primer despliegue. El proveedor de hosting no está definido: es una decisión que se toma al preparar el primer entorno remoto, considerando costo y necesidades reales del MVP.

## Qué existe hoy

**Desarrollo local**

- **Base de datos:** `compose.yaml`, en la raíz del repositorio, levanta MySQL 8.4 (puerto `3307`) y phpMyAdmin (puerto `8082`). Sólo sirve para desarrollo: sus credenciales son de desarrollo y phpMyAdmin no debe exponerse en un servidor.
- **Backend:** Java 21 y Spring Boot, con Maven. Desde `backend/`, `./mvnw spring-boot:run` levanta el perfil `dev`, que arranca `compose.yaml` por su cuenta. Escucha en el puerto `8080`.
- **Frontend:** Angular. Desde `frontend-panel/`, `npm start` sirve la aplicación en el puerto `4200`.
- **Migraciones:** Flyway las aplica al arrancar el backend.
- **Tests:** `./mvnw test` levanta una base MySQL descartable con Testcontainers, así que requiere Docker en ejecución.

**Lo que no existe todavía:** `Dockerfile` del backend o del frontend, un compose de producción, integración continua, health check, backups ni entornos remotos.

## Configuración del backend

El perfil se elige con `SPRING_PROFILES_ACTIVE`. Si no se define, se activa `dev`; **en un servidor hay que definirlo como `prod`**.

En `dev`, los valores se leen del archivo `backend/.env` (no se commitea; la plantilla es `backend/.env.example`). En `prod` no existen valores por defecto a propósito: si falta una variable, la aplicación no arranca en vez de arrancar mal configurada.

| Variable | Dev | Prod | Descripción |
|---|---|---|---|
| `JWT_SECRET` | obligatoria | obligatoria | Clave HMAC en base64, mínimo 32 bytes. Nunca tiene valor por defecto |
| `JWT_EXPIRATION_MINUTES` | 60 | 60 | Validez del token de acceso |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | valores de `compose.yaml` | obligatorias | Conexión a MySQL |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | obligatoria | Orígenes del frontend, separados por coma |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM_NAME` | opcionales | obligatorias | SMTP para el email de recuperación de contraseña |
| `FRONTEND_BASE_URL`, `FRONTEND_RESET_PASSWORD_PATH` | `http://localhost:4200`, `/reset-password` | obligatorias | Enlace que llega en el email de recuperación |
| `PASSWORD_RESET_EXPIRATION_MINUTES` | 30 | 30 | Validez del enlace de recuperación |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | no se usan | obligatorias | Administrador inicial (ver abajo) |
| `APP_TIME_ZONE` | `America/Argentina/Buenos_Aires` | `America/Argentina/Buenos_Aires` | Zona horaria de la JVM y de la sesión de MySQL |

### Administrador inicial

La migración `V2` crea un `SYSTEM_ADMIN` de desarrollo (`admin@mechanicmanager.local`) con una contraseña que es pública en el repositorio. En el perfil `prod`, al arrancar, el backend crea el administrador real a partir de `ADMIN_EMAIL` y `ADMIN_PASSWORD` si no existe, y **deshabilita** la cuenta sembrada. No hace falta ninguna acción manual, pero sí definir esas dos variables.

### Comportamiento propio del perfil `prod`

- Swagger UI y `/v3/api-docs` están deshabilitados.
- Spring Docker Compose está deshabilitado: la base ya debe existir.
- `server.forward-headers-strategy=native`: el backend toma la IP real del cliente de `X-Forwarded-For`, y el límite de intentos de login la usa. Sólo confía en ese encabezado si el proxy tiene una IP privada o de loopback; si no, todos los usuarios se verían con la IP del proxy.

## Configuración del frontend

`ng build` genera archivos estáticos en `frontend-panel/dist/`. El build de producción reemplaza `environment.ts` por `environment.prod.ts`, que apunta a `https://apimechanicmanager.imperial-net.com/api/v1`. La URL del backend queda fijada en el momento del build.

El dominio desde el que se sirva el frontend tiene que estar en `CORS_ALLOWED_ORIGINS` del backend.

## Antes del primer despliegue

Pendiente, sin orden de prioridad todavía:

- decidir cómo se empaqueta y se ejecuta el backend (jar, contenedor u otro);
- definir dónde se sirve el frontend estático;
- HTTPS y dominios;
- backups de la base y prueba de restauración;
- health check (hoy no hay Actuator) y monitoreo;
- logging: hoy es mínimo y va a la salida estándar;
- integración y entrega continuas;
- estrategia de rollback y recuperación ante fallos;
- cabeceras de seguridad HTTP en el servidor que sirva el frontend (`Content-Security-Policy`, `X-Content-Type-Options`, `Referrer-Policy`);
- limitador de intentos de login: es en memoria y por instancia; con más de una instancia habría que compartirlo.

## Ambientes previstos

```text
local
development
production
```

Hoy sólo existe `local`.

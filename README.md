<p align="center">
  <img src="docs/assets/Logo-mechanic.jpeg" alt="Logo de Mechanic Manager" width="240">
</p>

# Mechanic Manager

Aplicación web para la gestión de talleres mecánicos pequeños y medianos. Reemplaza agendas en papel, planillas y mensajes sueltos por un único panel donde se administra la operación diaria del taller: clientes, vehículos, turnos, presupuestos, trabajos realizados e historial técnico de cada vehículo.

## Funcionalidades

- **Clientes y vehículos**, con identificación por patente y, cuando se conocen, número de chasis y motor.
- **Agenda de turnos** por día, con estados.
- **Presupuestos** con ítems de servicios, insumos y descuentos manuales.
- **Catálogos propios** de tipos de servicio e insumos, con precios de referencia.
- **Trabajos realizados** (órdenes de trabajo), que pueden nacer de un turno completado, de un presupuesto aprobado o cargarse a mano.
- **Historial técnico** completo de cada vehículo.
- **Panel de inicio** con el resumen operativo del taller.
- **Cuentas y seguridad:** autenticación con JWT, recuperación de contraseña por email, cambio obligatorio de contraseña en el primer ingreso y un administrador del sistema que gestiona los usuarios.

Cada usuario administra su propio taller y sólo ve su información.

## Stack

| Capa | Tecnologías |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Security, Spring Data JPA, JWT, springdoc-openapi |
| Base de datos | MySQL 8.4, migraciones con Flyway |
| Frontend | Angular 21, TypeScript |
| Tests | JUnit, Testcontainers (backend) · tests unitarios de Angular (frontend) |
| Desarrollo local | Docker Compose (MySQL y phpMyAdmin) |

## Estructura del repositorio

```
mechanic-manager/
├── backend/          API REST en Spring Boot
├── frontend-panel/   panel web en Angular
├── docs/             producto, dominio, arquitectura, API y despliegue
└── compose.yaml      base de datos para desarrollo
```

## Arquitectura

El backend es un **monolito modular con arquitectura en capas**. Cada módulo de negocio (`customers`, `vehicles`, `appointments`, `quotes`, `workorders`, `servicetypes`, `supplies`, `workshops`, `users`, `auth`, `dashboard`) vive en su propio paquete y expone su API REST bajo `/api/v1`.

El contrato de la API está definido en OpenAPI y las decisiones de diseño están registradas como ADRs.

## Documentación

| Documento | Contenido |
|---|---|
| [Producto](docs/01-product/README.md) | Visión, alcance del MVP, roadmap y backlog |
| [Dominio](docs/02-domain/README.md) | Modelo de dominio |
| [Arquitectura](docs/03-architecture/README.md) | Diseño técnico y decisiones de arquitectura (ADR-001 a ADR-011) |
| [API](docs/04-api/README.md) | Contrato OpenAPI |
| [Despliegue](docs/05-deployment/README.md) | Configuración y variables de entorno |

## Cómo ejecutarlo

### Requisitos

- JDK 21
- Node.js 20 o superior
- Docker (para la base de datos y los tests del backend)

### Backend

```bash
cd backend
cp .env.example .env
```

Completar `JWT_SECRET` en `.env` (clave en base64 de al menos 32 bytes). Después:

```bash
./mvnw spring-boot:run
```

El perfil `dev` levanta MySQL con Docker Compose automáticamente y aplica las migraciones. La API queda en `http://localhost:8080` y la documentación interactiva en `http://localhost:8080/swagger-ui.html`.

### Frontend

```bash
cd frontend-panel
npm install
npm start
```

El panel queda en `http://localhost:4200`.

### Tests

```bash
cd backend && ./mvnw test
cd frontend-panel && npm test
```

Los tests del backend usan una base MySQL descartable con Testcontainers, por lo que necesitan Docker en ejecución.

## Licencia

Distribuido bajo licencia MIT. Ver [LICENSE](LICENSE).

## Autor

Jonathan Illescas · [GitHub](https://github.com/MichaelIllescas)

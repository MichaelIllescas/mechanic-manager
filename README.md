<p align="center">
  <img src="docs/assets/Logo-mechanic.jpeg" alt="Logo de Mechanic Manager" width="300">
</p>

<h1 align="center">Mechanic Manager</h1>

<p align="center">
  <strong>Micro SaaS para la gestión integral de talleres mecánicos.</strong>
</p>

---

## Descripción

**Mechanic Manager** es un Micro SaaS orientado a centralizar la gestión operativa de talleres mecánicos.

La plataforma permitirá administrar clientes, vehículos, agenda de turnos, presupuestos, servicios realizados e historial técnico, manteniendo la información de cada taller aislada dentro de una arquitectura multi-tenant.

El objetivo es reemplazar registros manuales, planillas y documentación dispersa por una solución organizada, mantenible y preparada para evolucionar.

---

## Funcionalidades del MVP

El alcance definido para la primera versión contempla:

- Gestión de talleres.
- Gestión de usuarios.
- Autenticación y autorización.
- Gestión de clientes.
- Gestión de vehículos.
- Registro de patente, número de chasis y número de motor.
- Agenda de turnos.
- Gestión de presupuestos.
- Registro y seguimiento de servicios mecánicos.
- Registro de diagnósticos.
- Registro de trabajos realizados.
- Historial técnico por vehículo.
- Búsqueda de clientes y vehículos.
- Aislamiento de información entre talleres.

---

## Roles

### Administrador del sistema

Responsable de la administración global de la plataforma.

Funciones previstas:

- administrar talleres;
- crear usuarios;
- habilitar o deshabilitar usuarios;
- administrar accesos;
- supervisar el estado básico de las cuentas.

### Mecánico

Único rol operativo del MVP.

Tendrá acceso a:

- clientes;
- vehículos;
- agenda;
- presupuestos;
- servicios;
- historial.

Durante el MVP no se diferenciarán roles internos como recepción, encargado o técnico.

---

## Flujo principal

```text
Agenda
  ↓
Cliente / Vehículo
  ↓
Presupuesto
  ↓
Servicio
  ↓
Diagnóstico / Trabajo
  ↓
Finalización / Entrega
  ↓
Historial
```

---

## Stack tecnológico

### Backend

- **Java 21**
- **Spring Boot 3.3**
- **Spring Web**
- **Spring Security**
- **JWT**
- **Spring Data JPA**
- **Hibernate**
- **Maven**

### Frontend

- **Angular 21**
- **TypeScript**
- **RxJS**

### Base de datos

- **MySQL 8**
- **Flyway** para versionado y migraciones de base de datos.

### API

- **REST API**
- **OpenAPI**
- **Swagger UI**

La especificación de la API se mantendrá en:

```text
docs/api/openapi.yaml
```

### Testing

- **JUnit 5**
- **Mockito**

### Infraestructura y despliegue

- **Docker**
- **Docker Compose**

---

## Arquitectura

Mechanic Manager se diseñará utilizando:

- **Monolito Modular**
- **Arquitectura Hexagonal — Ports & Adapters**
- **Domain-Driven Design (DDD)**
- **Principios SOLID**
- **Arquitectura Multi-Tenant**
- **Persistencia relacional**

La aplicación se mantendrá como una única unidad de despliegue, pero internamente estará dividida en módulos de negocio claramente delimitados.

### Módulos principales

```text
Identity & Tenancy
Customers
Vehicles
Appointments
Quotes
Services
```

El historial técnico de un vehículo se obtendrá a partir de los servicios registrados.

---

## Multi-Tenancy

Cada taller representa una organización independiente dentro de Mechanic Manager.

```text
Mechanic Manager
│
├── Taller A
│   ├── Usuarios
│   ├── Clientes
│   ├── Vehículos
│   ├── Turnos
│   ├── Presupuestos
│   └── Servicios
│
├── Taller B
│   ├── Usuarios
│   ├── Clientes
│   ├── Vehículos
│   ├── Turnos
│   ├── Presupuestos
│   └── Servicios
│
└── ...
```

Los datos pertenecientes a un taller deberán permanecer aislados de los demás talleres.

Una misma patente podrá existir en talleres diferentes, pero no podrá duplicarse dentro de un mismo taller.

---

## Documentación

La documentación funcional y técnica del proyecto se encuentra dentro de:

```text
docs/
```

Estructura:

```text
docs/
├── product/
├── domain/
├── architecture/
│   └── adr/
├── api/
└── deployment/
```

### Producto

- Product Vision.
- MVP Scope.
- Product Roadmap.
- Product Backlog.

### Dominio

- Modelo de dominio.
- Agregados.
- Entidades.
- Reglas de negocio.
- Lenguaje ubicuo.

### Arquitectura

- Technical Design.
- Arquitectura Hexagonal.
- DDD.
- SOLID.
- Multi-Tenancy.
- Architecture Decision Records (ADRs).

### API

La documentación de endpoints se realizará mediante **OpenAPI**.

```text
docs/api/openapi.yaml
```

### Deployment

Contendrá la documentación relacionada con:

- Docker.
- Docker Compose.
- ambientes.
- variables de entorno.
- base de datos.
- migraciones.
- CI/CD.
- HTTPS.
- backups.
- logging.
- despliegue.
- rollback.

---

## Repositorio

```text
mechanic-manager
```

### Ramas principales

```text
main
develop
```

- `main`: versiones estables.
- `develop`: rama principal de integración.

Las nuevas funcionalidades se desarrollarán mediante ramas `feature/*`.

Ejemplos:

```text
feature/customer-management
feature/vehicle-management
feature/appointments
feature/quotes
feature/services
```

---

<p align="center">
  <strong>Mechanic Manager — Gestión integral para talleres mecánicos.</strong>
</p>

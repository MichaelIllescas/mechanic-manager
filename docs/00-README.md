# Documentación — Mechanic Manager

**Versión:** 1.0  
**Estado:** Baseline MVP simplificada para validación

## Objetivo de esta documentación

Documentar únicamente lo necesario para construir y validar una primera versión usable de Mechanic Manager con talleres reales.

La prioridad del MVP es **reducir complejidad, salir rápido y aprender con usuarios reales**. Las capacidades que no sean necesarias para validar el flujo principal quedan fuera hasta contar con retroalimentación que justifique incorporarlas.

## Estructura

```text
docs/
├── 00-README.md
├── product/
├── domain/
├── architecture/
│   └── adr/
├── api/
└── deployment/
```

## Índice

### Producto
- [Product Vision](product/01-product-vision.md)
- [MVP Scope](product/02-mvp-scope.md)
- [Product Roadmap](product/03-product-roadmap.md)
- [Product Backlog](product/04-product-backlog.md)

### Dominio
- [Modelo de Dominio](domain/01-modelo-de-dominio.md)

### Arquitectura
- [Technical Design](architecture/01-technical-design.md)
- [Architecture Decision Records](architecture/adr/README.md)

### API
- [API Documentation](api/README.md)
- [OpenAPI Contract](api/openapi.yaml)

### Deployment
- [Deployment / Infraestructura](deployment/README.md)

## Arquitectura base

**Monolito Modular + Arquitectura en Capas + SOLID con criterio + KISS/YAGNI**

Se utilizará un modelo de negocio simple y explícito, agregando patrones únicamente cuando una necesidad real lo justifique.

## Stack tecnológico definido

### Backend

- **Java 21**
- **Spring Boot 3.3.13**
- **Maven**
- **Spring Web**
- **Spring Security**
- **JWT** para autenticación
- **Spring Data JPA + Hibernate**
- **Bean Validation**
- **Flyway** para migraciones

### Frontend

- **Angular 21**
- **TypeScript**
- **RxJS**

### Persistencia

- **MySQL 8**

### API y documentación

- **REST**
- **OpenAPI**
- **Swagger UI**
- API versionada bajo `/api/v1`

### Testing

- **JUnit 5**
- **Mockito**
- **Testcontainers**

### Infraestructura

- **Docker**
- **Docker Compose**

## Modelo de acceso del MVP

```text
SYSTEM_ADMIN
    └── administra usuarios de la plataforma

USER
    └── gestiona 1 Taller
            └── clientes, vehículos, agenda, presupuestos y servicios
```

En el MVP:

- un usuario operativo tiene como máximo un taller;
- un taller tiene un único usuario operativo;
- el administrador de la plataforma no administra talleres ni su operación;
- múltiples talleres por usuario y múltiples usuarios por taller quedan fuera del alcance inicial.

## API

La API será REST y su contrato se documentará mediante **OpenAPI**.

El archivo:

```text
docs/api/openapi.yaml
```

será versionado junto con el código y deberá evolucionar al mismo tiempo que los endpoints.

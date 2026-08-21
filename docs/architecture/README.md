# Architecture Documentation

Documentación técnica y arquitectónica del sistema.

## Arquitectura adoptada para el MVP

- Monolito Modular.
- Arquitectura en Capas.
- Organización por funcionalidad.
- Principios SOLID aplicados con criterio.
- KISS y YAGNI.
- API REST.
- Ownership simple `USER -> Workshop`.
- Java 21 + Spring Boot 4.1.0
- Maven.
- Spring Web + Spring Security + JWT.
- Spring Data JPA + Hibernate.
- MySQL 8 + Flyway.
- OpenAPI + Swagger UI.
- JUnit 5 + Mockito + Testcontainers.
- Frontend Angular 21 + TypeScript + RxJS.
- Docker + Docker Compose.

No se adopta Arquitectura Hexagonal ni una infraestructura genérica de multi-tenancy para el MVP.

## Contenido

- [Technical Design](01-technical-design.md)
- [Architecture Decision Records](adr/README.md)

Los cambios arquitectónicos relevantes deben quedar registrados mediante ADRs.

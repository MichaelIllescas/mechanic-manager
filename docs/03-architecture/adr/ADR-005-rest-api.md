# ADR-005 — API REST Versionada

**Estado:** Accepted
**Fecha:** Agosto 2026

## Contexto

El frontend necesita comunicarse con el backend mediante un contrato simple, estándar y fácil de documentar.

## Decisión

Utilizar una API REST versionada, implementada con **Spring Web**.

Base inicial:

```text
/api/v1
```

## Consecuencias

- amplia compatibilidad;
- facilidad de testing;
- documentación mediante **OpenAPI** y **Swagger UI**;
- contratos HTTP claros.

Los endpoints concretos se definirán durante la implementación.

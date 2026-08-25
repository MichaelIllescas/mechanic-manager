# ADR-008 — OpenAPI como Contrato de Documentación de la API

**Estado:** Accepted
**Fecha:** Agosto 2026

## Contexto

La aplicación expondrá una API REST que crecerá progresivamente con la implementación de los módulos del MVP.

Es necesario mantener una documentación técnica precisa, versionada y sincronizada con los endpoints reales.

## Decisión

Utilizar **OpenAPI** como contrato HTTP de la aplicación y **Swagger UI** para su consulta durante desarrollo e integración.

El contrato se mantendrá dentro del repositorio en:

```text
docs/04-api/openapi.yaml
```

La documentación deberá actualizarse junto con los cambios de endpoints.

## Contenido del contrato

OpenAPI deberá documentar, según corresponda:

- paths;
- métodos HTTP;
- parámetros;
- request bodies;
- response bodies;
- schemas;
- códigos HTTP;
- autenticación;
- errores;
- ejemplos relevantes.

## Regla de evolución

Un endpoint nuevo o modificado no se considerará completamente documentado si el contrato OpenAPI correspondiente no fue actualizado.

## Consecuencias positivas

- contrato versionado junto con el código;
- documentación consistente;
- posibilidad de generar Swagger UI u otra interfaz;
- facilita testing e integración con frontend;
- facilita generación futura de clientes o mocks.

## Integración con el stack

El contrato OpenAPI forma parte del stack definido del backend Spring Boot. El archivo `docs/04-api/openapi.yaml` permanece versionado como referencia del contrato y Swagger UI se utilizará para inspeccionar la API durante desarrollo.

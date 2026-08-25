# API Documentation

**Estado:** Preparada para implementación

---

## Estrategia de documentación

La API del backend **Spring Boot 4.1.0** se implementará con **Spring Web** y se documentará utilizando **OpenAPI**, con **Swagger UI** para su inspección.

El contrato principal se encuentra en:

```text
openapi.yaml
```

y se mantendrá versionado dentro del repositorio.

## Convención base

```text
/api/v1
```

## Documentación por endpoint

Cada endpoint deberá incluir como mínimo:

- método HTTP;
- path;
- descripción;
- autenticación;
- autorización;
- parámetros;
- request body;
- response body;
- códigos HTTP;
- errores;
- schemas;
- ejemplos cuando aporten valor.

## Regla de mantenimiento

Cuando un endpoint sea agregado, eliminado o modificado, el contrato OpenAPI deberá actualizarse en el mismo cambio.

## Organización futura

Si la API crece considerablemente, este directorio podrá dividir el contrato en componentes reutilizables, por ejemplo:

```text
api/
├── README.md
├── openapi.yaml
├── paths/
├── schemas/
├── examples/
└── errors/
```

No se dividirá prematuramente mientras un único `openapi.yaml` siga siendo manejable.

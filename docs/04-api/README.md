# API Documentation

**Estado:** Implementada. El contrato cubre todos los endpoints del MVP.

---

## Estrategia de documentación

La API del backend **Spring Boot 4.1.0** se implementa con **Spring Web** y se documenta con **OpenAPI**, con **Swagger UI** para su inspección.

El contrato versionado está en:

```text
docs/04-api/openapi.yaml
```

**No se edita a mano.** Se genera desde el propio código: las anotaciones de cada `*Api` (controller) y los DTO son la fuente, y `openapi.yaml` es su reflejo. Así el contrato no puede quedar desactualizado sin que se note.

## Convención base

```text
/api/v1
```

## Mantener el contrato al día

Un test (`OpenApiContractTest`) compara el `openapi.yaml` versionado con lo que la aplicación expone en `/v3/api-docs.yaml`. Si un endpoint, un DTO o una anotación de documentación cambian y el archivo no, el test falla y explica cómo resolverlo.

Para regenerar el contrato:

```bash
cd backend
./mvnw test -Dtest=OpenApiContractTest -Dopenapi.update=true
```

Después se revisa el diff de `openapi.yaml` y se commitea junto con el cambio que lo originó. Requiere Docker en ejecución, como el resto de los tests que levantan la aplicación.

## Inspección interactiva

Con el backend corriendo en el perfil `dev`, Swagger UI está en:

```text
http://localhost:8080/swagger-ui.html
```

En producción ni Swagger UI ni `/v3/api-docs` están habilitados.

## Documentación por endpoint

Cada endpoint se documenta en su interfaz `*Api` con, como mínimo:

- método HTTP y path;
- descripción;
- autenticación y autorización;
- parámetros y request body;
- response body;
- códigos HTTP y errores (formato `ErrorResponse`, ver `shared/exception/README.md`);
- ejemplos cuando aporten valor.

Un endpoint nuevo o modificado no se considera terminado hasta que su anotación esté completa y el contrato regenerado (ADR-008).

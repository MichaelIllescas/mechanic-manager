# Módulo de vehículos

Gestiona los vehículos atendidos por el taller del usuario autenticado.
Implementa el registro, listado, búsqueda por patente, asociación con clientes
y edición definidos para el MVP.

## Alcance

El módulo permite:

- registrar un vehículo;
- asociarlo a un cliente del mismo taller;
- listar los vehículos del taller autenticado;
- buscar por coincidencia parcial de patente;
- editar sus datos y cambiar el cliente asociado.

No incluye eliminación, consulta de historial ni órdenes de trabajo. Esas
funciones pertenecen a otros alcances del MVP.

## Propiedad y autorización

El frontend nunca envía un `workshopId`. Cada operación recibe el identificador
del usuario autenticado y resuelve su taller mediante `WorkshopRepository`.

Las consultas siempre incluyen el taller en el criterio. Un identificador de
vehículo o cliente perteneciente a otro taller se responde como recurso no
encontrado, sin revelar información ajena.

Un vehículo sólo puede asociarse a un cliente perteneciente al mismo taller.

## Modelo

`Vehicle` contiene:

- `workshop` y `customer`;
- `licensePlate`;
- `brand` y `model`;
- `year`, opcional;
- `chassisNumber` y `engineNumber`, opcionales;
- `mileage`, opcional;
- `vehicleType`;
- `notes`, opcional;
- fechas de creación y modificación.

Tipos admitidos:

- `CAR`;
- `SUV`;
- `PICKUP`;
- `VAN`;
- `TRUCK`;
- `MOTORCYCLE`;
- `OTHER`.

La patente es única dentro de cada taller. Patente, número de chasis y número
de motor se guardan en mayúsculas cuando están informados. Los textos
obligatorios se guardan sin espacios exteriores y los campos opcionales vacíos
se convierten en `null`.

## Endpoints

Todos requieren autenticación y rol `USER`.

| Método | Ruta | Función |
|---|---|---|
| `GET` | `/api/v1/vehicles` | Lista los vehículos del taller |
| `GET` | `/api/v1/vehicles?search=AB123` | Busca por coincidencia parcial de patente |
| `POST` | `/api/v1/vehicles` | Registra y asocia un vehículo |
| `PUT` | `/api/v1/vehicles/{id}` | Edita un vehículo |

El contrato detallado se encuentra en `docs/04-api/openapi.yaml`.

## Estructura

- `controller`: rutas, contrato OpenAPI y adaptación HTTP;
- `dto`: contratos de entrada y salida;
- `exception`: errores propios del módulo;
- `mapper`: conversión de entidad a respuesta;
- `model`: entidad y enum de tipos;
- `repository`: repositorio Spring Data y especificación de búsqueda;
- `service`: fachada y casos de uso individuales.

Los repositorios usan métodos derivados de Spring Data y `Specification`; no
contienen SQL manual.

## Errores

El módulo utiliza el manejo global de excepciones:

| Código | HTTP | Situación |
|---|---:|---|
| `VALIDATION_ERROR` | 400 | El request no cumple las validaciones |
| `UNAUTHENTICATED` | 401 | Falta autenticación válida |
| `ACCESS_DENIED` | 403 | El usuario no posee el rol requerido |
| `RESOURCE_NOT_FOUND` | 404 | No existe el taller, vehículo o cliente dentro del alcance |
| `LICENSE_PLATE_ALREADY_EXISTS` | 409 | La patente ya existe en el taller |

## Base de datos

La tabla se crea mediante `V10__create_vehicles.sql`.

La migración usa `DATETIME(6)`, claves foráneas restrictivas hacia `workshops`
y `customers`, unicidad de patente por taller e índices para las consultas del
módulo. `year`, `chassisNumber`, `engineNumber`, `mileage` y `notes` aceptan
`NULL`.

## Pruebas

Las pruebas unitarias cubren:

- registro y normalización;
- patente duplicada;
- asociación y aislamiento por taller;
- listado y búsqueda;
- edición y reasignación de cliente;
- delegación de la fachada.

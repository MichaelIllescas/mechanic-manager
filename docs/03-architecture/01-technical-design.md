# Architecture / Technical Design

**Versión:** 1.0  
**Estado:** Decisión técnica para MVP  
**Estilo:** Monolito Modular + Arquitectura en Capas + KISS/YAGNI

---

## 1. Objetivos arquitectónicos

La arquitectura debe permitir:

- desarrollar el MVP rápidamente;
- mantener bajo el costo operativo;
- mantener el código entendible;
- separar responsabilidades sin crear abstracciones innecesarias;
- facilitar testing de las reglas importantes;
- proteger los datos de cada taller;
- permitir cambios rápidos según la validación con clientes.

La prioridad es construir una base **suficientemente ordenada para evolucionar**, no una arquitectura preparada de antemano para todos los escenarios futuros.

---

## 2. Estilo arquitectónico

Se adopta un **Monolito Modular con Arquitectura en Capas**.

La aplicación se desplegará como una única unidad. El código se organizará por capacidades funcionales y, dentro de ellas, por responsabilidades simples.

No se utilizará Arquitectura Hexagonal en el MVP.

No se exigirá DDD táctico, ports/adapters, CQRS, eventos de dominio ni otras estructuras mientras no exista una necesidad concreta.

```text
HTTP Request
    │
    ▼
Controller
    │
    ▼
Service
    │
    ▼
Repository
    │
    ▼
Database
```

Responsabilidades transversales como seguridad, validación, mapeo y manejo de errores se implementarán de manera explícita, evitando crear capas sin una función real.

### Stack de implementación

El stack del MVP queda definido de la siguiente manera:

| Área | Tecnología |
|---|---|
| Backend | Java 21 + Spring Boot 4.1.0 |
| Build | Maven |
| HTTP | Spring Web |
| Seguridad | Spring Security + JWT |
| Persistencia | Spring Data JPA + Hibernate |
| Validación | Bean Validation |
| Base de datos | MySQL 8 |
| Migraciones | Flyway |
| API | REST `/api/v1` |
| Documentación API | OpenAPI + Swagger UI |
| Testing | JUnit 5 + Mockito + Testcontainers |
| Frontend | Angular 21 + TypeScript + RxJS |
| Contenedores | Docker + Docker Compose |

---

## 3. Organización del código

Se prefiere organizar por funcionalidad para evitar carpetas globales gigantes.

Ejemplo conceptual:

```text
com.mechanicmanager
├── auth
│   ├── controller
│   ├── service
│   ├── repository
│   ├── model
│   └── dto
├── users
│   ├── controller
│   ├── service
│   ├── repository
│   ├── model
│   └── dto
├── workshops
├── customers
├── vehicles
├── appointments
├── quotes
├── servicetypes
├── workorders
└── shared
    ├── security
    ├── exception
    └── config
```

El backend se implementará con **Java 21 y Spring Boot 4.1.0**, utilizando **Maven** como herramienta de build. La organización anterior es la estructura base del proyecto y se mantendrá alineada con las responsabilidades de cada módulo.

---

## 4. Capas y responsabilidades

### 4.1 Controller

Responsabilidades:

- recibir requests HTTP;
- validar formato de entrada;
- obtener la identidad autenticada;
- delegar la operación al servicio correspondiente;
- mapear resultados y errores a respuestas HTTP.

No debe contener reglas de negocio ni acceso directo a base de datos.

### 4.2 Service

Responsabilidades:

- implementar y coordinar los casos de uso;
- aplicar reglas de negocio;
- verificar ownership/acceso al taller cuando corresponda;
- coordinar repositorios;
- manejar transacciones cuando sea necesario.

Para el MVP no se separarán artificialmente `application services`, `domain services`, commands o use cases si un servicio simple resuelve correctamente la necesidad.

### 4.3 Repository

Responsabilidades:

- encapsular acceso a datos;
- realizar búsquedas y persistencia;
- aplicar consultas restringidas por taller cuando corresponda.

Los controllers nunca accederán directamente a repositories.

### 4.4 Model / Entity

Representa los datos y reglas esenciales del negocio.

Las reglas simples pueden permanecer en services o entidades según dónde resulten más claras. No se fuerza un modelo de dominio rico si no aporta valor al MVP.

### 4.5 DTO

Los contratos HTTP no deben reutilizar directamente entidades de persistencia cuando eso acople innecesariamente la API a la base de datos.

Se utilizarán DTOs de request/response donde aporten claridad y control del contrato.

---

## 5. Modelo de usuarios y talleres

El MVP utiliza dos roles:

- `SYSTEM_ADMIN`
- `USER`

### `SYSTEM_ADMIN`

- administra cuentas de usuario de la plataforma;
- puede crear, consultar, habilitar y deshabilitar usuarios;
- no posee taller;
- no crea ni administra talleres;
- no participa en la operación de los talleres.

### `USER`

- es el usuario operativo del negocio;
- puede crear y editar su propio taller;
- tiene acceso completo a las funciones de ese taller;
- puede tener como máximo un taller en el MVP.

### Relación inicial

```text
User 1 ───────── 0..1 Workshop
```

El `0..1` permite que una cuenta exista antes de completar la configuración inicial del taller.

La base de datos deberá impedir que un usuario operativo tenga dos talleres.

No existe en el MVP una relación `Workshop 1 -> N Users`.

---

## 6. Aislamiento de datos

La plataforma tendrá múltiples talleres, pero el MVP no implementará una infraestructura genérica de multi-tenancy.

Se utilizará una regla simple de ownership:

1. el usuario autenticado identifica su cuenta;
2. su cuenta determina su único `workshopId`;
3. las operaciones del negocio se ejecutan únicamente dentro de ese taller;
4. un `workshopId` enviado por frontend nunca autoriza por sí mismo una operación.

```text
Authenticated USER
      │
      ▼
User.workshop
      │
      ▼
workshopId confiable
      │
      ▼
Service / Repository
      │
      ▼
WHERE workshop_id = :authenticatedWorkshopId
```

Los endpoints operativos deberían evitar pedir `workshopId` cuando el taller puede obtenerse directamente desde la identidad autenticada.

---

## 7. Persistencia

Se utilizará **MySQL 8** como base de datos relacional.

Motivos:

- integridad referencial;
- relaciones claras;
- transacciones;
- presupuestos con ítems;
- restricciones de unicidad;
- consultas históricas.

### Restricciones importantes

Conceptualmente:

```text
UNIQUE(workshop.user_id)
UNIQUE(workshop_id, license_plate)
```

Las entidades operativas podrán almacenar `workshop_id` para facilitar el aislamiento y las consultas. La persistencia se implementará con **Spring Data JPA + Hibernate** y las migraciones de esquema se gestionarán con **Flyway**.

No se agregará una entidad abstracta `Tenant` en el MVP.

---

## 8. Seguridad

Requerimientos mínimos:

- hash seguro de contraseñas;
- autenticación mediante **JWT**;
- autorización por rol;
- cuenta habilitada para iniciar sesión;
- resolución del taller desde el usuario autenticado;
- validación de ownership en operaciones sensibles;
- manejo centralizado de errores;
- protección de endpoints administrativos.

### Regla crítica

`SYSTEM_ADMIN` y `USER` son contextos distintos.

Ser administrador de la plataforma no implica ser usuario operativo de un taller ni habilita automáticamente el acceso a datos de clientes, vehículos, presupuestos u órdenes de trabajo.

---

## 9. Flujo de una operación

Ejemplo: registrar vehículo.

```text
HTTP Request
    │
    ▼
VehicleController
    │
    ▼
VehicleService
    ├── obtiene usuario autenticado
    ├── obtiene su workshopId
    ├── valida cliente del mismo taller
    ├── valida patente
    └── registra vehículo
    │
    ▼
VehicleRepository
    │
    ▼
Database
```

El frontend no decide a qué taller pertenece el vehículo.

---

## 10. API

La aplicación expondrá una API REST versionada mediante **Spring Web**. El contrato se documentará con **OpenAPI** y estará disponible para inspección mediante **Swagger UI**.

Base prevista:

```text
/api/v1
```

El contrato evolucionará junto con la implementación mediante OpenAPI.

Preferencia del MVP:

- endpoints simples;
- recursos claros;
- sin versionados o capas adicionales innecesarias;
- evitar comandos o abstracciones si CRUD/casos de uso directos son suficientes.

---

## 11. Testing

El stack de testing será **JUnit 5 + Mockito + Testcontainers**. Se priorizarán pruebas con mayor valor para el MVP.

### Unit tests

Para reglas y servicios relevantes:

- cálculo de presupuestos;
- transiciones de estado;
- validaciones importantes;
- ownership cuando corresponda.

### Integration tests

Prioridad especial para:

- autenticación;
- autorización;
- acceso restringido al taller autenticado;
- constraints de base de datos;
- persistencia de flujos críticos.

### Controller/API tests

Para contratos y errores relevantes de endpoints principales.

No se crearán suites de tests de arquitectura sólo para sostener abstracciones que el MVP no necesita.

---

## 12. Principios de diseño

- KISS.
- YAGNI.
- SOLID con criterio, no como ceremonia.
- DRY con criterio; duplicación pequeña puede ser preferible a una abstracción prematura.
- Controllers delgados.
- Services con casos de uso claros.
- Repositories enfocados en persistencia.
- No microservicios en el MVP.
- No CQRS completo en el MVP.
- No eventos distribuidos en el MVP.
- No infraestructura genérica de multi-tenancy.
- No permisos internos del taller hasta que la validación los requiera.

---

## 13. Evolución futura

Si la validación demuestra la necesidad de:

- varios usuarios por taller;
- varios talleres por usuario;
- roles internos;
- permisos granulares;
- separación más fuerte de módulos;
- integraciones complejas;

se revisará la arquitectura y se documentará la nueva decisión mediante ADR.

No se implementarán esas capacidades antes de necesitarlas.

---

## 14. Resultado esperado

La combinación:

**Monolito Modular + Arquitectura en Capas + KISS/YAGNI**

busca permitir construir y cambiar el MVP rápido, manteniendo suficiente orden para que el código no se convierta en un monolito desorganizado.

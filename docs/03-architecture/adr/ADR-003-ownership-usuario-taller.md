# ADR-003 — Ownership Usuario-Taller para el MVP

**Estado:** Accepted  
**Fecha:** Agosto 2026

## Contexto

La plataforma tendrá varios usuarios y talleres, pero el MVP no necesita soportar organizaciones complejas.

Permitir desde el inicio múltiples talleres por usuario, múltiples usuarios dentro de cada taller, invitaciones y permisos internos aumentaría considerablemente el modelo de datos, la autorización y la interfaz sin haber validado esa necesidad.

## Decisión

Adoptar para el MVP una relación simple:

```text
USER 1 -> 0..1 Workshop
Workshop 1 -> 1 USER
```

Reglas:

- cada usuario operativo puede gestionar como máximo un taller;
- cada taller pertenece a un único usuario operativo;
- el taller es creado y administrado por ese usuario;
- `SYSTEM_ADMIN` administra usuarios de plataforma, no talleres;
- los datos operativos se restringen usando el `userId` del usuario autenticado;
- no se confiará en ningún identificador de propietario enviado por el frontend para autorizar acceso.

## Columna de propiedad

Toda tabla de datos operativos lleva `user_id` como columna de propiedad, con
clave foránea hacia `users`. No lleva `workshop_id`.

El taller no es la unidad de aislamiento del MVP: es un conjunto de datos de
encabezado que cuelga del usuario. Como `workshops.user_id` es único, taller y
usuario identifican exactamente el mismo alcance, y `user_id` lo hace sin
depender de que el taller exista ni obligar a resolverlo en cada request.

En consecuencia:

- el criterio de toda consulta de datos operativos es `user_id`;
- las claves únicas de negocio se definen por usuario, por ejemplo
  `UNIQUE (user_id, name)`;
- un identificador que pertenece a otro usuario se responde como recurso no
  encontrado;
- `WorkshopRepository` se usa para leer los datos del taller, no para autorizar.

## Consecuencias positivas

- modelo de datos simple;
- autorización simple;
- onboarding simple;
- menor superficie de errores;
- una consulta menos por request al no resolver el taller para autorizar;
- salida más rápida del MVP.

## Trade-off

Si un cliente necesita varios empleados o varios talleres, el modelo deberá evolucionar.

Ese cambio se hará después de validar la necesidad y podrá requerir una relación de membresías, nuevos roles y una estrategia de aislamiento más general. En ese momento `user_id` pasa a `workshop_id` en las tablas operativas, con un backfill directo a través de `workshops.user_id`.

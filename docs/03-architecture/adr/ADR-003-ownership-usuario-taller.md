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
- los datos operativos se restringen usando el `workshopId` obtenido desde el usuario autenticado;
- no se confiará en un `workshopId` enviado por frontend para autorizar acceso.

## Consecuencias positivas

- modelo de datos simple;
- autorización simple;
- onboarding simple;
- menor superficie de errores;
- salida más rápida del MVP.

## Trade-off

Si un cliente necesita varios empleados o varios talleres, el modelo deberá evolucionar.

Ese cambio se hará después de validar la necesidad y podrá requerir una relación de membresías, nuevos roles y una estrategia de aislamiento más general.

# ADR-004 — Modelo Inicial de Roles

**Estado:** Accepted  
**Fecha:** Agosto 2026

## Contexto

El MVP necesita distinguir únicamente entre quien administra la plataforma y quien usa el producto para operar su taller.

Separar propietario, recepción, mecánico, encargado u otros roles internos agregaría complejidad de permisos antes de validar esa necesidad.

## Decisión

El MVP tendrá dos roles:

- `SYSTEM_ADMIN`
- `USER`

### `SYSTEM_ADMIN`

- administra cuentas de usuario de Mechanic Manager;
- puede crear, consultar, habilitar o deshabilitar usuarios;
- no administra talleres;
- no participa de la operación de los talleres.

### `USER`

- es el único rol operativo;
- administra su propio taller;
- tiene acceso funcional completo a clientes, vehículos, agenda, presupuestos, servicios e historial de su taller.

## Consecuencia

La autorización inicial permanece simple.

Los roles internos del taller sólo se incorporarán si la validación con clientes demuestra que son necesarios.

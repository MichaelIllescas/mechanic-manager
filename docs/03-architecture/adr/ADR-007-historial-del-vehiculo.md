# ADR-007 — Historial Derivado de Órdenes de Trabajo

**Estado:** Accepted
**Fecha:** Agosto 2026

## Contexto

El historial de un vehículo representa los trabajos registrados a lo largo del tiempo.

A partir de ADR-009, esos trabajos se registran como órdenes de trabajo con sus ítems, no como una entidad `Service`.

Crear un almacenamiento independiente para historial duplicaría información.

## Decisión

No crear inicialmente una entidad o agregado `VehicleHistory`.

El historial será una consulta cronológica de los trabajos realizados (`WorkOrder`, ver ADR-011) asociados al vehículo o a un cliente, con sus ítems.

Los ítems conservan la descripción y el precio con los que fueron registrados, de modo que el historial no cambia cuando cambia el catálogo de servicios.

## Consecuencias positivas

- evita duplicación;
- evita sincronización;
- reduce inconsistencias;
- simplifica el modelo.

Si en el futuro aparecen necesidades de auditoría o proyecciones especializadas, se evaluará una nueva decisión arquitectónica.

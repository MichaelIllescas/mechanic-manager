# ADR-007 — Historial Derivado de Servicios

**Estado:** Accepted
**Fecha:** Agosto 2026

## Contexto

El historial de un vehículo representa los trabajos registrados a lo largo del tiempo.

Crear un almacenamiento independiente para historial duplicaría información.

## Decisión

No crear inicialmente una entidad o agregado `VehicleHistory`.

El historial será una consulta cronológica de los servicios asociados al vehículo.

## Consecuencias positivas

- evita duplicación;
- evita sincronización;
- reduce inconsistencias;
- simplifica el modelo.

Si en el futuro aparecen necesidades de auditoría o proyecciones especializadas, se evaluará una nueva decisión arquitectónica.

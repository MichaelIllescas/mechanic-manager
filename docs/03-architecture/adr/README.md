# Architecture Decision Records

Cada decisión arquitectónica relevante se documenta en un ADR independiente.

## ADRs

- [ADR-001 — Monolito Modular con Arquitectura en Capas](ADR-001-monolito-modular-arquitectura-en-capas.md)
- [ADR-002 — Modelado Simple del Dominio para el MVP](ADR-002-modelado-simple-del-dominio.md)
- [ADR-003 — Ownership Usuario-Taller para el MVP](ADR-003-ownership-usuario-taller.md)
- [ADR-004 — Modelo Inicial de Roles](ADR-004-modelo-de-roles.md)
- [ADR-005 — API REST Versionada](ADR-005-rest-api.md)
- [ADR-006 — Base de Datos Relacional](ADR-006-base-de-datos-relacional.md)
- [ADR-007 — Historial Derivado de Órdenes de Trabajo](ADR-007-historial-del-vehiculo.md)
- [ADR-008 — OpenAPI como Contrato de Documentación de la API](ADR-008-openapi-contract.md)
- [ADR-009 — Separación entre Catálogo de Servicios y Orden de Trabajo](ADR-009-servicio-vs-orden-de-trabajo.md)
- [ADR-010 — Catálogo de Insumos separado del Control de Stock](ADR-010-catalogo-de-insumos.md)
- [ADR-011 — Trabajo Realizado en lugar de Orden de Trabajo con Ciclo de Vida](ADR-011-trabajo-realizado.md)

## Principio del MVP

Las decisiones deben favorecer simplicidad y validación temprana. Una arquitectura más compleja sólo se incorpora cuando exista una necesidad demostrada.

## Convención

Cada ADR debe incluir como mínimo:

- Estado.
- Contexto.
- Decisión.
- Consecuencias.
- Riesgos o trade-offs cuando correspondan.

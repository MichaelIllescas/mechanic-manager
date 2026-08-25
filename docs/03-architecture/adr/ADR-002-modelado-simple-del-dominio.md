# ADR-002 — Modelado Simple del Dominio para el MVP

**Estado:** Accepted  
**Fecha:** Agosto 2026

## Contexto

El sistema contiene conceptos de negocio como talleres, clientes, vehículos, turnos, presupuestos y servicios. Sin embargo, el objetivo actual es validar el producto rápidamente.

Adoptar DDD táctico completo desde el inicio podría introducir agregados, value objects, domain services, eventos y otras estructuras antes de saber si el producto necesita esa complejidad.

## Decisión

No utilizar **DDD táctico como requisito arquitectónico del MVP**.

Se mantendrán únicamente prácticas simples que aporten claridad:

- nombres alineados con el negocio;
- entidades y relaciones explícitas;
- reglas de negocio ubicadas donde resulten comprensibles;
- módulos alineados con funcionalidades principales;
- separación entre contrato HTTP, lógica y persistencia cuando aporte valor.

## Consecuencias

- menor curva de complejidad;
- implementación más rápida;
- menos clases y patrones ceremoniales;
- mayor libertad para refactorizar cuando exista evidencia real.

## Evolución

Si el dominio crece y aparecen invariantes o procesos complejos, se podrá incorporar DDD de manera selectiva mediante una nueva decisión arquitectónica.

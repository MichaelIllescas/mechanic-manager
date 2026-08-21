# ADR-001 — Monolito Modular con Arquitectura en Capas

**Estado:** Accepted  
**Fecha:** Agosto 2026

## Contexto

Mechanic Manager comienza como un MVP cuyo objetivo principal es validar la utilidad del producto con talleres reales.

La arquitectura debe favorecer velocidad de desarrollo, bajo costo operativo y facilidad de cambio. Una estructura Hexagonal con ports, adapters y abstracciones adicionales agrega trabajo que todavía no está justificado por las necesidades del MVP.

## Decisión

Implementar el backend como un **Monolito Modular con Arquitectura en Capas**, sobre **Java 21 y Spring Boot 4.1.0**, utilizando **Maven**.

Las responsabilidades principales serán:

```text
Controller -> Service -> Repository -> MySQL 8
```

El código se organizará preferentemente por funcionalidad, manteniendo dentro de cada módulo las capas que realmente necesite.

No se utilizará Arquitectura Hexagonal en el MVP.

## Consecuencias positivas

- menor cantidad de abstracciones;
- desarrollo más rápido;
- estructura familiar y fácil de entender;
- testing directo de services y repositories;
- despliegue simple;
- posibilidad de reorganizar la arquitectura si el producto valida y crece.

## Riesgos

- controllers o services demasiado grandes;
- acoplamiento entre funcionalidades si no se mantienen límites razonables;
- crecimiento desordenado del monolito.

## Mitigación

- controllers delgados;
- services enfocados en casos de uso;
- repositories sin lógica de negocio;
- organización por feature;
- revisiones periódicas según crecimiento real del producto.

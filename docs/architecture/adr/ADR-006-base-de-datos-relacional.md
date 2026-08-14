# ADR-006 — Base de Datos Relacional

**Estado:** Accepted
**Fecha:** Agosto 2026

## Contexto

El dominio contiene relaciones fuertes, presupuestos con ítems, restricciones de unicidad, transacciones e historial.

## Decisión

Utilizar **MySQL 8** como base de datos relacional del MVP.

El acceso a datos se realizará con **Spring Data JPA + Hibernate** y la evolución del esquema se gestionará mediante **Flyway**.

## Motivos

- integridad referencial;
- soporte transaccional;
- restricciones;
- consultas relacionales;
- consistencia.

Restricción inicial relevante:

```text
UNIQUE(workshop_id, license_plate)
```


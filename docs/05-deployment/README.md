# Deployment / Infraestructura

**Versión:** 1.0  
**Estado:** Baseline de infraestructura del MVP

---

El MVP se empaquetará y ejecutará mediante **Docker** y **Docker Compose**. El backend será una aplicación **Java 21 / Spring Boot 4.1.0** y la persistencia utilizará **MySQL 8**, con migraciones gestionadas mediante **Flyway**.

El proveedor de hosting no se fija en esta versión porque todavía no es una decisión necesaria para implementar y validar el producto.

## Aspectos a documentar

- plataforma de despliegue;
- ambientes;
- contenedores Docker;
- variables de entorno;
- secretos;
- MySQL 8;
- migraciones con Flyway;
- backups;
- dominios;
- HTTPS;
- logging;
- observabilidad;
- CI/CD;
- health checks;
- rollback;
- recuperación ante fallos.

## Ambientes previstos

```text
local
development
production
```

La elección del proveedor de infraestructura queda fuera de la v1.0 y se realizará al preparar el primer entorno remoto, considerando costo y necesidades reales del MVP.

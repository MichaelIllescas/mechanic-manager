# Product Roadmap

**Versión:** 1.1  
**Estado:** Inicial — orientado a validación

---

## Objetivo

Organizar el desarrollo en entregas pequeñas que permitan llegar al flujo usable lo antes posible.

El roadmap no representa un compromiso de fechas y debe poder cambiar según la retroalimentación de usuarios reales.

---

## Fase 0 — Base mínima

### Objetivo

Tener la base suficiente para empezar a construir funcionalidades sin introducir infraestructura o abstracciones prematuras.

### Incluye

- estructura inicial del proyecto;
- monolito modular;
- arquitectura en capas;
- persistencia y migraciones;
- autenticación;
- autorización básica con `SYSTEM_ADMIN` y `USER`;
- administración de usuarios de plataforma;
- creación y edición del taller propio;
- asociación `USER` 1:1 con `Workshop`;
- aislamiento de datos a partir del taller del usuario autenticado;
- manejo centralizado de errores;
- logging básico;
- tests esenciales.

### No incluye

- Arquitectura Hexagonal;
- DDD táctico como requisito;
- infraestructura genérica de multi-tenancy;
- múltiples talleres por usuario;
- múltiples usuarios por taller;
- permisos granulares.

---

## Fase 1 — Core del Taller

### Objetivo

Permitir registrar la información fundamental del negocio.

### Incluye

- clientes;
- vehículos;
- asociación cliente-vehículo;
- patente;
- número de chasis opcional;
- número de motor opcional;
- kilometraje opcional;
- búsqueda por patente;
- consulta;
- edición.

---

## Fase 2 — Agenda y Presupuestos

### Objetivo

Cubrir el flujo previo al ingreso del vehículo al taller.

### Agenda

- crear turno;
- consultar agenda por fecha;
- reprogramar;
- confirmar;
- atender;
- cancelar.

### Presupuestos

- crear presupuesto;
- agregar ítems;
- cantidades;
- precios;
- subtotal;
- total;
- estados;
- consulta por cliente;
- consulta por vehículo.

---

## Fase 3 — Catálogos, Órdenes de Trabajo e Historial

### Objetivo

Administrar el trabajo realizado sobre los vehículos y cerrar el flujo principal del MVP.

### Incluye

- catálogo de servicios del taller con precio de referencia;
- catálogo de insumos del taller con precio de referencia;
- ingreso del vehículo;
- creación de la orden de trabajo;
- vínculo con presupuesto aprobado;
- diagnóstico;
- ítems de servicios y repuestos realizados;
- observaciones;
- total;
- estados;
- finalización;
- entrega;
- historial técnico.

Los catálogos se construyen antes que las órdenes: los ítems de una orden los toman como origen.

---

## Fase 4 — Validación con clientes

### Objetivo

Usar el producto con talleres reales y registrar problemas, necesidades y patrones de uso antes de ampliar el alcance.

### Resultado esperado

Priorizar cambios basados en evidencia y no en supuestos.

---

## Fase 5 — Post-MVP

Sólo después de validación.

Posibles capacidades:

- más de un usuario por taller;
- roles y permisos internos;
- más de un taller por usuario;
- stock y existencias;
- proveedores;
- órdenes de compra;
- facturación;
- notificaciones;
- reportes;
- dashboards;
- planes comerciales;
- suscripciones;
- aplicación móvil.

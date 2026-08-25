# Product Backlog

**Versión:** 1.0  
**Método de priorización:** MoSCoW

---

## Prioridades

- **Must:** indispensable para considerar funcional el MVP.
- **Should:** importante, pero puede simplificarse inicialmente.
- **Could:** queda para una evolución posterior.
- **Won't:** explícitamente fuera del MVP actual.

---

| ID | Épica | Ítem | Prioridad | Release |
|---|---|---|---|---|
| PB-01 | Seguridad | Crear/administrar usuarios de plataforma como `SYSTEM_ADMIN` | Must | MVP |
| PB-02 | Seguridad | Inicio de sesión | Must | MVP |
| PB-03 | Seguridad | Autorizar por rol `SYSTEM_ADMIN` / `USER` | Must | MVP |
| PB-04 | Taller | Crear taller propio | Must | MVP |
| PB-05 | Taller | Consultar y editar taller propio | Must | MVP |
| PB-06 | Seguridad | Restringir datos al taller del usuario autenticado | Must | MVP |
| PB-07 | Clientes | Registrar cliente | Must | MVP |
| PB-08 | Clientes | Listar clientes | Must | MVP |
| PB-09 | Clientes | Buscar cliente | Must | MVP |
| PB-10 | Clientes | Editar cliente | Must | MVP |
| PB-11 | Vehículos | Registrar vehículo con patente, chasis y motor | Must | MVP |
| PB-12 | Vehículos | Asociar vehículo a cliente | Must | MVP |
| PB-13 | Vehículos | Buscar vehículo por patente | Must | MVP |
| PB-14 | Vehículos | Editar vehículo | Must | MVP |
| PB-15 | Agenda | Crear turno | Must | MVP |
| PB-16 | Agenda | Consultar agenda por fecha | Must | MVP |
| PB-17 | Agenda | Reprogramar turno | Must | MVP |
| PB-18 | Agenda | Cancelar turno | Must | MVP |
| PB-19 | Agenda | Actualizar estado del turno | Must | MVP |
| PB-20 | Presupuestos | Crear presupuesto | Must | MVP |
| PB-21 | Presupuestos | Agregar ítems | Must | MVP |
| PB-22 | Presupuestos | Calcular subtotal y total | Must | MVP |
| PB-23 | Presupuestos | Gestionar estado | Must | MVP |
| PB-24 | Presupuestos | Consultar presupuestos por cliente/vehículo | Must | MVP |
| PB-25 | Servicios | Crear servicio / registrar ingreso | Must | MVP |
| PB-26 | Servicios | Vincular presupuesto aprobado | Should | MVP |
| PB-27 | Servicios | Registrar diagnóstico | Must | MVP |
| PB-28 | Servicios | Registrar trabajo realizado | Must | MVP |
| PB-29 | Servicios | Agregar observaciones | Should | MVP |
| PB-30 | Servicios | Actualizar estado | Must | MVP |
| PB-31 | Servicios | Consultar servicios abiertos | Must | MVP |
| PB-32 | Historial | Consultar historial del vehículo | Must | MVP |
| PB-33 | Organización | Varios usuarios por taller | Won't | Post-MVP |
| PB-34 | Organización | Varios talleres por usuario | Won't | Post-MVP |
| PB-35 | Seguridad | Roles y permisos internos del taller | Won't | Post-MVP |
| PB-36 | Inventario | Gestionar repuestos y stock | Could | Post-MVP |
| PB-37 | Proveedores | Gestionar proveedores | Could | Post-MVP |
| PB-38 | Comunicación | Notificar al cliente | Could | Post-MVP |
| PB-39 | Facturación | Facturación electrónica | Could | Post-MVP |
| PB-40 | Analítica | Reportes y estadísticas | Could | Post-MVP |

---

## Regla de evolución

Los ítems Post-MVP no deben incorporarse al desarrollo inicial hasta validar el flujo central del producto con clientes reales, salvo que una necesidad observada obligue a cambiar el alcance.

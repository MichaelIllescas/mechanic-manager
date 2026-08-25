# Product Scope / MVP Scope

**Versión:** 1.0  
**Estado:** MVP simplificado para validación

---

## 1. Objetivo del alcance

Definir únicamente las capacidades necesarias para poner Mechanic Manager en manos de talleres reales y validar si resuelve el flujo operativo principal.

El criterio de alcance es:

> Si una funcionalidad no es necesaria para validar el flujo central del taller, no entra al MVP.

---

## 2. Modelo operativo del MVP

```text
SYSTEM_ADMIN -> administra usuarios de la plataforma

USER -> administra 1 Taller

Taller -> posee clientes, vehículos, turnos, presupuestos y servicios
```

Reglas iniciales:

- un `USER` puede tener como máximo un taller;
- un taller pertenece a un único `USER`;
- `SYSTEM_ADMIN` no administra talleres;
- no existen colaboradores ni múltiples usuarios dentro de un taller;
- no existen roles internos del taller.

---

## 3. Incluido en el MVP

### 3.1 Plataforma y seguridad

- Administración de cuentas de usuario por `SYSTEM_ADMIN`.
- Inicio de sesión.
- Autorización básica por rol.
- Roles iniciales:
  - `SYSTEM_ADMIN`
  - `USER`
- Creación y edición del taller propio por parte de `USER`.
- Asociación uno a uno entre usuario operativo y taller.
- Separación de los datos operativos según el taller del usuario autenticado.

### 3.2 Taller

Funciones:

- crear los datos iniciales del taller;
- consultar el taller propio;
- editar sus datos básicos.

Datos mínimos sugeridos:

- nombre;
- teléfono;
- dirección opcional;
- email opcional;
- observaciones opcionales.

### 3.3 Clientes

Funciones:

- registrar;
- consultar;
- buscar;
- editar;
- visualizar vehículos asociados.

Datos mínimos:

- nombre;
- apellido;
- teléfono;
- DNI opcional;
- email opcional;
- observaciones opcionales.

### 3.4 Vehículos

Funciones:

- registrar;
- editar;
- asociar a cliente;
- buscar;
- consultar;
- acceder a historial.

Datos mínimos:

- patente;
- marca;
- modelo;
- año;
- número de chasis;
- número de motor;
- kilometraje;
- tipo de vehículo;
- observaciones opcionales.

### 3.5 Agenda

Funciones:

- crear turno;
- consultar agenda por fecha;
- reprogramar;
- confirmar;
- marcar como atendido;
- cancelar.

Estados iniciales:

- `SCHEDULED`
- `CONFIRMED`
- `ATTENDED`
- `CANCELLED`

### 3.6 Presupuestos

Funciones:

- crear presupuesto;
- asociar cliente y vehículo;
- agregar ítems;
- registrar cantidades;
- registrar precios;
- calcular subtotales y total;
- consultar presupuestos;
- gestionar estado;
- vincular presupuesto aprobado a un servicio.

Estados iniciales:

- `DRAFT`
- `SENT`
- `APPROVED`
- `REJECTED`

### 3.7 Servicios

Funciones:

- registrar ingreso;
- registrar kilometraje;
- registrar motivo;
- registrar diagnóstico;
- registrar trabajo realizado;
- registrar observaciones;
- actualizar estado;
- consultar servicios abiertos;
- vincular presupuesto aprobado cuando corresponda.

Estados iniciales:

- `PENDING`
- `IN_PROGRESS`
- `FINISHED`
- `DELIVERED`

### 3.8 Historial

Funciones:

- consultar servicios anteriores de un vehículo;
- ordenarlos cronológicamente;
- visualizar información técnica relevante.

Información mínima:

- fecha;
- kilometraje;
- motivo;
- diagnóstico;
- trabajo realizado;
- estado;
- presupuesto asociado cuando exista.

---

## 4. Fuera del alcance del MVP

Quedan explícitamente fuera de la primera versión:

### Organización y usuarios

- múltiples talleres por usuario;
- múltiples usuarios dentro de un taller;
- colaboradores;
- invitaciones;
- roles internos diferenciados;
- permisos granulares;
- transferencia de propiedad de un taller;
- administración de talleres por `SYSTEM_ADMIN`.

### Funcionalidad comercial y operativa

- stock;
- inventario de repuestos;
- proveedores;
- compras;
- órdenes de compra;
- facturación electrónica;
- contabilidad;
- caja;
- conciliaciones;
- integración automática con WhatsApp;
- integración automática con email;
- reportes analíticos avanzados;
- aplicación móvil nativa;
- billing automatizado del SaaS;
- planes comerciales automatizados.

Estas capacidades podrán evaluarse después de validar el núcleo funcional con clientes reales.

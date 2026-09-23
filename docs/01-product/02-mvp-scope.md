# Product Scope / MVP Scope

**Versión:** 1.3  
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

USER -> posee clientes, vehículos, turnos, presupuestos,
        catálogo de servicios y órdenes de trabajo
```

Reglas iniciales:

- los datos operativos pertenecen al `USER`; el taller guarda los datos del negocio;
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
- Separación de los datos operativos según el usuario autenticado.

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
- dirección opcional;
- observaciones opcionales.

### 3.4 Vehículos

Funciones:

- registrar;
- editar;
- asociar a cliente;
- buscar;
- consultar;
- acceder a historial.

Datos mínimos obligatorios:

- patente;
- marca;
- modelo;
- tipo de vehículo.

Datos técnicos opcionales:

- año;
- número de chasis;
- número de motor;
- kilometraje;
- observaciones opcionales.

### 3.5 Agenda

Funciones:

- crear turno sobre un vehículo;
- consultar agenda por fecha;
- reprogramar;
- completar;
- cancelar.

Estados iniciales:

- `SCHEDULED`
- `COMPLETED`
- `CANCELLED`

No incluye:

- validación de solapamiento de horarios;
- confirmación previa del cliente ni recordatorios;
- detalle de los trabajos a realizar: el motivo va como nota libre y el detalle surge del presupuesto o de la orden de trabajo.

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
- tomar ítems del catálogo de servicios o describirlos libremente;
- vincular presupuesto aprobado a una orden de trabajo.

Estados iniciales:

- `DRAFT`
- `SENT`
- `APPROVED`
- `REJECTED`

### 3.7 Catálogo de servicios

Un servicio es lo que el taller ofrece: cambio de aceite, alineación, cambio de pastillas. Es catálogo, no un trabajo ocurrido.

Funciones:

- crear servicio;
- registrar nombre y descripción;
- registrar precio de referencia;
- activar y desactivar;
- consultar servicios activos.

Reglas iniciales:

- un servicio utilizado no se elimina, se desactiva;
- cambiar el precio no modifica presupuestos ni órdenes ya registrados.

### 3.8 Catálogo de insumos

Un insumo es un material o repuesto que el taller utiliza en sus trabajos: aceite, filtros, pastillas, bujías. Es catálogo, no un consumo ocurrido.

Funciones:

- crear insumo;
- registrar nombre;
- registrar precio de referencia;
- activar y desactivar;
- consultar insumos activos.

Reglas iniciales:

- un insumo utilizado no se elimina, se desactiva;
- cambiar el precio no modifica presupuestos ni órdenes ya registrados.

El MVP incluye el catálogo con sus precios, no el control de existencias. Cuántas unidades hay en el taller queda fuera del alcance.

### 3.9 Órdenes de trabajo

Una orden de trabajo es el ingreso concreto de un vehículo al taller.

Funciones:

- registrar ingreso;
- registrar kilometraje;
- registrar motivo;
- registrar diagnóstico;
- registrar los servicios y repuestos realizados como ítems;
- registrar observaciones;
- calcular total;
- actualizar estado;
- consultar órdenes abiertas;
- vincular presupuesto aprobado cuando corresponda.

Estados iniciales:

- `PENDING`
- `IN_PROGRESS`
- `FINISHED`
- `DELIVERED`

### 3.10 Historial

Funciones:

- consultar órdenes de trabajo anteriores de un vehículo;
- ordenarlas cronológicamente;
- visualizar información técnica relevante.

Información mínima:

- fecha;
- kilometraje;
- motivo;
- diagnóstico;
- servicios y repuestos realizados;
- total;
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
- movimientos de existencias;
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

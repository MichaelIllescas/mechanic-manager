# Product Scope / MVP Scope

**Versión:** 1.4  
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
        catálogos de servicios e insumos y trabajos realizados
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
- Recuperación de contraseña por email y cambio de la propia contraseña.
- Contraseña temporal en las cuentas nuevas: hay que cambiarla en el primer ingreso.
- Límite de intentos en el inicio de sesión y en la recuperación de contraseña.
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
- vincular un presupuesto aprobado del mismo vehículo al crear el turno;
- completar, lo que registra automáticamente el trabajo realizado (sección 3.9);
- cancelar;
- eliminar: el presupuesto y el trabajo vinculados se conservan, sin turno.

Estados iniciales:

- `SCHEDULED`
- `COMPLETED`
- `CANCELLED`

No incluye:

- validación de solapamiento de horarios;
- confirmación previa del cliente ni recordatorios;
- detalle de los trabajos a realizar: el motivo va como nota libre y el detalle surge del presupuesto o del trabajo realizado.

### 3.6 Presupuestos

Funciones:

- crear presupuesto;
- asociar cliente y vehículo;
- agregar ítems tomándolos del catálogo de servicios o del de insumos, o describiéndolos libremente;
- registrar cantidades y precios;
- registrar descuentos como ítems manuales con precio negativo;
- calcular subtotales y total;
- consultar y buscar presupuestos, con filtro por estado y paginación;
- editar y eliminar;
- gestionar estado;
- vincular un presupuesto aprobado a un turno;
- descargar un comprobante del presupuesto.

Estados:

- `PENDING` (inicial)
- `APPROVED`
- `REJECTED`

Reglas:

- cada presupuesto recibe un número correlativo propio de su usuario, que empieza en 1;
- un ítem tiene como origen un servicio, un insumo o es manual; nunca dos a la vez (ADR-011);
- el precio de un ítem de catálogo no puede ser negativo; sólo un ítem manual puede serlo, para representar un descuento;
- el total no puede ser negativo ni superar el importe máximo que admite la base;
- un presupuesto puede pasar de cualquier estado a cualquier otro y editarse en cualquier estado;
- excepción: un presupuesto que ya tiene un trabajo realizado no puede cambiar de estado;
- al crearlo se puede fijar una vigencia que no puede ser anterior a hoy; al editarlo no se vuelve a exigir, para poder corregir un presupuesto vencido.

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

### 3.9 Trabajos realizados

Un trabajo realizado registra lo que efectivamente se hizo sobre un vehículo. En el código es `WorkOrder` (ver ADR-011). No tiene ciclo de vida: nace completado.

Funciones:

- registrar un trabajo realizado sobre un vehículo, con fecha y kilometraje opcional;
- registrar los servicios y repuestos realizados como ítems, con la misma forma que los de un presupuesto;
- registrar observaciones;
- calcular el total a partir de los ítems;
- vincularlo a un turno y/o a un presupuesto aprobado del mismo vehículo;
- consultar y buscar, con filtros por estado, vehículo y rango de fechas, y paginación;
- editar y eliminar;
- marcarlo como cancelado o volver a marcarlo como completado;
- descargar un comprobante del trabajo.

Estados:

- `COMPLETED` (inicial)
- `CANCELLED`

Reglas:

- puede crearse a mano o de forma automática al completar un turno (sección 3.5);
- al completar un turno, si tiene un presupuesto aprobado que todavía no originó otro trabajo, sus ítems se copian al trabajo; si no, el trabajo nace sin ítems para completarlo a mano;
- un turno y un presupuesto originan como máximo un trabajo realizado;
- los ítems se copian y no se referencian: el presupuesto queda intacto como referencia histórica aunque lo realizado difiera.

### 3.10 Historial

Funciones:

- consultar los trabajos realizados de un vehículo o de un cliente;
- ordenarlos cronológicamente, del más reciente al más antiguo;
- descargar el comprobante de cada trabajo.

Información mínima:

- fecha;
- kilometraje;
- servicios y repuestos realizados;
- total;
- estado;
- presupuesto asociado cuando exista.

### 3.11 Panel de inicio

Resumen operativo del usuario al ingresar:

- turnos del día y su agenda;
- presupuestos pendientes y cuántos de ellos están vencidos;
- trabajos completados en el mes;
- cantidad de vehículos registrados.

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
- planes comerciales automatizados;
- ciclo de vida de la orden de trabajo (en curso, finalizada, entregada);
- motivo de ingreso y diagnóstico estructurados.

Estas capacidades podrán evaluarse después de validar el núcleo funcional con clientes reales.

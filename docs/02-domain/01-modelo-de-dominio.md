# Modelo de Dominio

**Versión:** 1.3  
**Enfoque:** Modelo simple orientado al MVP

---

## 1. Objetivo

Describir los conceptos, relaciones y reglas mínimas necesarias para implementar y validar Mechanic Manager sin introducir estructuras de dominio prematuras.

El modelo debe ser fácil de entender y modificar a medida que aparezca retroalimentación de talleres reales.

---

## 2. Términos principales

| Término | Significado |
|---|---|
| Usuario | Persona que utiliza la plataforma para gestionar su taller |
| Administrador del sistema | Miembro del equipo de Mechanic Manager que administra cuentas de usuario |
| Taller | Negocio mecánico administrado por un único usuario operativo en el MVP |
| Cliente | Persona responsable o propietaria de uno o más vehículos |
| Vehículo | Unidad atendida por el taller |
| Turno | Reserva de fecha y hora para atención |
| Presupuesto | Estimación económica previa a un trabajo |
| Ítem de presupuesto | Concepto individual de trabajo o repuesto |
| Servicio | Prestación que el taller ofrece, con precio de referencia. Es catálogo, no un hecho ocurrido |
| Insumo | Material o repuesto que el taller utiliza en sus trabajos, con precio de referencia. Es catálogo, no un hecho ocurrido |
| Orden de trabajo | Ingreso concreto de un vehículo al taller, con su diagnóstico, estado y trabajos ejecutados |
| Ítem de orden de trabajo | Servicio o repuesto efectivamente realizado dentro de una orden |
| Historial | Vista cronológica de las órdenes de trabajo de un vehículo |

---

## 3. Áreas funcionales

```text
Auth / Users
Workshops
Customers
Vehicles
Appointments
Quotes
ServiceTypes
Supplies
WorkOrders
```

`History` será inicialmente una consulta derivada de las órdenes de trabajo registradas.

Estas áreas son límites organizativos del código, no bounded contexts formales del MVP.

---

## 4. Entidades principales

### 4.1 User

Atributos iniciales:

- `id`
- `name`
- `email`
- `passwordHash`
- `role`
- `status`
- `createdAt`
- `updatedAt`

Roles:

- `SYSTEM_ADMIN`
- `USER`

Reglas:

- un usuario deshabilitado no puede autenticarse;
- `SYSTEM_ADMIN` no necesita taller;
- un `USER` puede asociarse como máximo a un taller;
- `SYSTEM_ADMIN` administra cuentas de usuario, no talleres.

---

### 4.2 Workshop

Atributos iniciales:

- `id`
- `userId`
- `name`
- `phone`
- `email` opcional
- `address` opcional
- `notes` opcional
- `createdAt`
- `updatedAt`

Reglas:

- pertenece a un único `USER`;
- `userId` debe ser único en `Workshop`;
- el usuario propietario puede consultar y editar su propio taller;
- no existe administración de talleres por `SYSTEM_ADMIN` en el MVP.

---

### 4.3 Customer

Atributos:

- `id`
- `userId`
- `firstName`
- `lastName`
- `documentNumber`
- `phone`
- `email`
- `notes`

Reglas:

- pertenece a un usuario;
- sólo puede consultarse o modificarse desde ese usuario.

---

### 4.4 Vehicle

Atributos:

- `id`
- `userId`
- `customerId`
- `licensePlate`
- `brand`
- `model`
- `year`
- `chassisNumber`
- `engineNumber`
- `mileage`
- `vehicleType`
- `notes`

Reglas:

- pertenece a un cliente;
- pertenece a un usuario;
- la patente es única dentro de un usuario;
- cliente y vehículo deben pertenecer al mismo usuario;
- su historial no se elimina al modificar datos descriptivos.

---

### 4.5 Appointment

Atributos:

- `id`
- `userId`
- `customerId`
- `vehicleId`
- `scheduledAt`
- `reason`
- `notes`
- `status`

Estados:

- `SCHEDULED`
- `CONFIRMED`
- `ATTENDED`
- `CANCELLED`

Reglas:

- pertenece a un usuario;
- cliente y vehículo deben pertenecer al mismo usuario;
- puede reprogramarse mientras la regla de negocio lo permita.

---

### 4.6 Quote

Atributos de `Quote`:

- `id`
- `userId`
- `customerId`
- `vehicleId`
- `number`
- `createdAt`
- `validUntil`
- `status`
- `total`
- `notes`

Atributos de `QuoteItem`:

- `id`
- `quoteId`
- `serviceTypeId` opcional
- `description`
- `quantity`
- `unitPrice`
- `subtotal`

Estados:

- `DRAFT`
- `SENT`
- `APPROVED`
- `REJECTED`

Reglas:

- el total surge de sus ítems;
- cliente y vehículo deben pertenecer al usuario autenticado;
- un ítem puede originarse en un `ServiceType` del catálogo o describirse libremente;
- `description` y `unitPrice` se copian al crear el ítem y no siguen cambiando con el catálogo;
- un presupuesto aprobado puede vincularse a una orden de trabajo;
- un presupuesto aprobado no debe modificarse silenciosamente.

---

### 4.7 ServiceType

Representa una prestación que el taller ofrece. Es catálogo: no tiene fecha, estado ni vehículo asociado.

Atributos:

- `id`
- `userId`
- `name`
- `description`
- `basePrice`
- `estimatedMinutes` opcional
- `active`
- `createdAt`
- `updatedAt`

Reglas:

- pertenece a un usuario;
- `name` es único dentro de un usuario;
- `basePrice` es un precio de referencia y puede cambiar en el tiempo;
- un cambio de precio no altera presupuestos ni órdenes ya registrados;
- un servicio no se elimina si fue utilizado: se marca `active = false`.

---

### 4.8 Supply

Representa un insumo o repuesto que el taller utiliza en sus trabajos. Es catálogo: no tiene stock, fecha ni vehículo asociado.

Atributos:

- `id`
- `userId`
- `name`
- `price`
- `active`
- `createdAt`
- `updatedAt`

Reglas:

- pertenece a un usuario;
- `name` es único dentro de un usuario;
- `price` es un precio de referencia y puede cambiar en el tiempo;
- un cambio de precio no altera presupuestos ni órdenes ya registrados;
- un insumo no se elimina nunca: se marca `active = false` (baja lógica).

No forma parte del MVP el control de stock, existencias ni movimientos de inventario (`PB-36`).

---

### 4.9 WorkOrder

Representa el ingreso concreto de un vehículo al taller. Es el hecho ocurrido.

Atributos:

- `id`
- `userId`
- `vehicleId`
- `quoteId` opcional
- `entryDate`
- `entryMileage`
- `reason`
- `diagnosis`
- `notes`
- `status`
- `total`
- `finishedAt`
- `deliveredAt`

Estados:

- `PENDING`
- `IN_PROGRESS`
- `FINISHED`
- `DELIVERED`

Reglas:

- pertenece a un vehículo del mismo usuario;
- puede vincular un presupuesto aprobado del mismo usuario;
- el trabajo ejecutado se registra en sus ítems, no en un campo de texto;
- `total` surge de sus ítems;
- las órdenes finalizadas o entregadas permanecen disponibles como historial.

---

### 4.10 WorkOrderItem

Representa un servicio o repuesto efectivamente realizado dentro de una orden.

Atributos:

- `id`
- `workOrderId`
- `serviceTypeId` opcional
- `description`
- `quantity`
- `unitPrice`
- `subtotal`
- `notes` opcional

Reglas:

- pertenece a una única orden de trabajo;
- puede originarse en un `ServiceType` del mismo usuario o describirse libremente para trabajos no catalogados y repuestos;
- `description` y `unitPrice` se copian del catálogo al crear el ítem y quedan congelados;
- `subtotal` surge de `quantity` por `unitPrice`.

---

## 5. Relaciones

```text
User 1 ─────────── 0..1 Workshop
User 1 ──────────── N Customer
Customer 1 ─────── N Vehicle
Vehicle 1 ───────── N Appointment
Vehicle 1 ───────── N Quote
Quote 1 ─────────── N QuoteItem
User 1 ──────────── N ServiceType
User 1 ──────────── N Supply
Vehicle 1 ───────── N WorkOrder
Quote 1 ─────────── 0..1 WorkOrder
WorkOrder 1 ─────── N WorkOrderItem
ServiceType 1 ───── N QuoteItem
ServiceType 1 ───── N WorkOrderItem
```

`ServiceType` participa como origen opcional de los ítems: un ítem puede existir sin referencia al catálogo.

No existe en el MVP:

```text
User 1 -> N Workshop
Workshop 1 -> N User
```

---

## 6. Reglas transversales

1. Todo dato operativo pertenece a un `USER`.
2. Un `USER` sólo opera sobre sus propios datos.
3. El `USER` se obtiene desde la identidad autenticada, no desde un identificador confiado al frontend.
4. Las referencias entre cliente, vehículo, turno, presupuesto, servicio y orden de trabajo deben corresponder al mismo `USER`.
5. La patente debe ser única por `USER`.
6. El historial se deriva de las órdenes de trabajo registradas.
7. Los importes de un presupuesto o de una orden se congelan al registrarse y no siguen al catálogo.
8. Los cambios de estado deben respetar transiciones válidas.
9. `SYSTEM_ADMIN` administra usuarios de plataforma y no la operación de los talleres.

El dueño de los datos es el `USER`, no el `Workshop`. En el MVP un usuario tiene un único taller, así que el `userId` alcanza para aislar la información y evita tener que resolver el taller en cada consulta.

`Workshop` guarda los datos del negocio (nombre, teléfono, dirección) y no es el propietario de clientes, vehículos, turnos, presupuestos, servicios ni órdenes.

Si en algún momento un taller necesita más de una persona (PB-33), estas referencias pasan a `workshopId`. Es un cambio de alcance conocido, no un descuido.

---

## 7. Evolución fuera del MVP

Si los clientes requieren colaboración o estructuras más grandes, podrá evaluarse:

- varios usuarios por taller;
- membresías de usuarios en talleres;
- roles internos;
- varios talleres por usuario;
- permisos granulares.

No se modelarán estas relaciones antes de validar que son necesarias.

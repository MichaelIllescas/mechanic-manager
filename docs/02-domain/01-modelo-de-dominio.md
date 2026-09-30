# Modelo de Dominio

**Versión:** 1.5  
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
| Trabajo realizado (`WorkOrder`) | Registro de lo que se hizo sobre un vehículo, con sus ítems, total y estado. No tiene ciclo de vida (ADR-011) |
| Ítem de trabajo realizado | Servicio o repuesto efectivamente realizado dentro de un trabajo |
| Historial | Vista cronológica de los trabajos realizados de un vehículo o de un cliente |

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

`History` es una consulta derivada de los trabajos realizados registrados. `Dashboard` es un resumen de lectura sobre turnos, presupuestos, trabajos y vehículos; no tiene entidades propias.

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
- `mustChangePassword`
- `createdAt`
- `updatedAt`

Roles:

- `SYSTEM_ADMIN`
- `USER`

Reglas:

- un usuario deshabilitado no puede autenticarse;
- una cuenta con `mustChangePassword` (contraseña temporal puesta por el administrador) sólo puede cambiar su contraseña hasta hacerlo;
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
- `address`
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
- `year` opcional
- `chassisNumber` opcional
- `engineNumber` opcional
- `mileage` opcional
- `vehicleType`
- `notes` opcional

Reglas:

- pertenece a un cliente;
- pertenece a un usuario;
- la patente es única dentro de un usuario;
- patente, marca, modelo y tipo de vehículo son obligatorios para registrar la ficha;
- chasis y motor son datos técnicos que pueden completarse cuando estén disponibles;
- cliente y vehículo deben pertenecer al mismo usuario;
- su historial no se elimina al modificar datos descriptivos.

---

### 4.5 Appointment

Atributos:

- `id`
- `userId`
- `vehicleId`
- `quoteId` opcional
- `scheduledAt`
- `status`
- `notes` opcional

Estados:

- `SCHEDULED`
- `COMPLETED`
- `CANCELLED`

Reglas:

- pertenece a un usuario;
- se agenda contra un vehículo; el cliente se deduce del vehículo y no se almacena en el turno;
- vehículo y turno deben pertenecer al mismo usuario;
- `SCHEDULED` es el estado inicial; `COMPLETED` y `CANCELLED` son terminales;
- reprogramar es modificar `scheduledAt` de un turno en `SCHEDULED`, no un estado propio;
- el turno se completa cuando el vehículo ingresa al taller; a partir de ahí el trabajo queda registrado en el trabajo realizado;
- la agenda no valida solapamiento de horarios: un taller puede atender varios vehículos en simultáneo;
- el motivo del turno se registra como texto libre en `notes`; el detalle estructurado del trabajo surge del presupuesto o del trabajo realizado;
- al crearlo puede vincularse un presupuesto *disponible* del mismo vehículo: aprobado, sin trabajo realizado y sin otro turno activo (`SCHEDULED` o `COMPLETED`); la referencia vive en el turno (`Appointment.quoteId`);
- un turno cancelado conserva su presupuesto como historial, pero no lo bloquea: el presupuesto puede vincularse a un turno nuevo;
- al completarse se crea automáticamente su trabajo realizado (ver `WorkOrder`);
- al eliminarlo, el presupuesto vinculado no se modifica y el trabajo vinculado se conserva sin turno.

---

### 4.6 Quote

Atributos de `Quote`:

- `id`
- `userId`
- `customerId`
- `vehicleId`
- `number`
- `createdAt`
- `updatedAt`
- `validUntil` opcional
- `status`
- `total`
- `notes`

Atributos de `QuoteItem`:

- `id`
- `quoteId`
- `serviceTypeId` opcional
- `supplyId` opcional
- `description`
- `quantity`
- `unitPrice`
- `subtotal`

Estados:

- `PENDING` (inicial)
- `APPROVED`
- `REJECTED`

Reglas:

- el total surge de sus ítems y no puede ser negativo ni superar el importe máximo de la base;
- `number` es correlativo dentro del usuario y empieza en 1;
- puede originarse en un turno o crearse sin turno previo;
- cliente y vehículo deben pertenecer al usuario autenticado, y el vehículo debe ser del cliente indicado;
- un ítem puede originarse en un `ServiceType`, en un `Supply` o describirse libremente, pero nunca en los dos catálogos a la vez;
- `description` y `unitPrice` se copian al crear el ítem y no siguen cambiando con el catálogo;
- el precio de un ítem de catálogo no puede ser negativo; un ítem manual sí, para representar un descuento;
- cambia entre los tres estados sin restricción y se edita en cualquier estado, salvo que ya tenga un trabajo realizado: entonces su estado no puede cambiar;
- la vigencia (`validUntil`) no puede ser anterior a hoy al crearlo, pero no se vuelve a exigir al editarlo;
- un presupuesto aprobado puede originar un trabajo realizado.

---

### 4.7 ServiceType

Representa una prestación que el taller ofrece. Es catálogo: no tiene fecha, estado ni vehículo asociado.

Atributos:

- `id`
- `userId`
- `name`
- `description`
- `basePrice`
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

### 4.9 WorkOrder (trabajo realizado)

Representa lo que efectivamente se hizo sobre un vehículo. Es el hecho ocurrido y no tiene ciclo de vida: nace completado (ADR-011).

Atributos:

- `id`
- `userId`
- `vehicleId`
- `appointmentId` opcional
- `quoteId` opcional
- `performedAt`
- `mileage` opcional
- `status`
- `total`
- `notes`
- `createdAt`
- `updatedAt`

Estados:

- `COMPLETED` (inicial)
- `CANCELLED`

Reglas:

- pertenece a un vehículo del mismo usuario;
- puede vincular un turno y/o un presupuesto del mismo usuario y del mismo vehículo;
- un turno y un presupuesto originan como máximo un trabajo realizado;
- puede crearse a mano o automáticamente al completar un turno; en ese caso copia los ítems del presupuesto aprobado del turno, si existe y no fue usado por otro trabajo, y si no nace sin ítems;
- el trabajo ejecutado se registra en sus ítems, no en un campo de texto;
- `total` surge de sus ítems y no puede ser negativo ni superar el importe máximo de la base;
- puede pasar entre `COMPLETED` y `CANCELLED` y editarse en cualquiera de los dos estados;
- los trabajos permanecen disponibles como historial.

---

### 4.10 WorkOrderItem

Representa un servicio o repuesto efectivamente realizado dentro de un trabajo.

Atributos:

- `id`
- `workOrderId`
- `serviceTypeId` opcional
- `supplyId` opcional
- `description`
- `quantity`
- `unitPrice`
- `subtotal`

Reglas:

- pertenece a un único trabajo realizado;
- puede originarse en un `ServiceType` o en un `Supply` del mismo usuario, o describirse libremente para trabajos no catalogados; nunca en los dos catálogos a la vez;
- `description` y `unitPrice` se copian del catálogo al crear el ítem y quedan congelados;
- el precio de un ítem de catálogo no puede ser negativo; un ítem manual sí, para representar un descuento;
- `subtotal` surge de `quantity` por `unitPrice`.

---

## 5. Relaciones

```text
User 1 ─────────── 0..1 Workshop
User 1 ──────────── N Customer
Customer 1 ─────── N Vehicle
Vehicle 1 ───────── N Appointment
Vehicle 1 ───────── N Quote
Appointment 1 ───── 0..1 Quote
Quote 1 ─────────── N QuoteItem
User 1 ──────────── N ServiceType
User 1 ──────────── N Supply
Vehicle 1 ───────── N WorkOrder
Quote 1 ─────────── 0..1 WorkOrder
Appointment 1 ───── 0..1 WorkOrder
WorkOrder 1 ─────── N WorkOrderItem
ServiceType 1 ───── N QuoteItem
ServiceType 1 ───── N WorkOrderItem
Supply 1 ────────── N QuoteItem
Supply 1 ────────── N WorkOrderItem
```

`ServiceType` y `Supply` participan como origen opcional de los ítems: un ítem referencia como máximo uno de los dos, o ninguno si es manual.

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
4. Las referencias entre cliente, vehículo, turno, presupuesto, servicio, insumo y trabajo realizado deben corresponder al mismo `USER`.
5. La patente debe ser única por `USER`.
6. El historial se deriva de los trabajos realizados registrados.
7. Los importes de un presupuesto o de una orden se congelan al registrarse y no siguen al catálogo.
8. Los cambios de estado respetan las reglas de cada entidad. En el MVP el turno restringe sus transiciones (`SCHEDULED` pasa a `COMPLETED` o `CANCELLED` y no vuelve); presupuestos y trabajos cambian libremente, con la excepción de un presupuesto que ya tiene trabajo realizado.
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

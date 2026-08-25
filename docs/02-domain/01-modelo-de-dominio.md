# Modelo de Dominio

**Versión:** 1.0  
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
| Servicio | Trabajo o intervención registrada sobre un vehículo |
| Historial | Vista cronológica de los servicios realizados sobre un vehículo |

---

## 3. Áreas funcionales

```text
Auth / Users
Workshops
Customers
Vehicles
Appointments
Quotes
Services
```

`History` será inicialmente una consulta derivada de los servicios realizados.

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
- `workshopId`
- `firstName`
- `lastName`
- `documentNumber`
- `phone`
- `email`
- `notes`

Reglas:

- pertenece a un taller;
- sólo puede consultarse o modificarse desde el usuario propietario de ese taller.

---

### 4.4 Vehicle

Atributos:

- `id`
- `workshopId`
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
- pertenece a un taller;
- la patente es única dentro de un taller;
- cliente y vehículo deben pertenecer al mismo taller;
- su historial no se elimina al modificar datos descriptivos.

---

### 4.5 Appointment

Atributos:

- `id`
- `workshopId`
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

- pertenece a un taller;
- cliente y vehículo deben pertenecer al mismo taller;
- puede reprogramarse mientras la regla de negocio lo permita.

---

### 4.6 Quote

Atributos de `Quote`:

- `id`
- `workshopId`
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
- cliente y vehículo deben pertenecer al taller autenticado;
- un presupuesto aprobado puede vincularse a un servicio;
- un presupuesto aprobado no debe modificarse silenciosamente.

---

### 4.7 Service

Atributos:

- `id`
- `workshopId`
- `vehicleId`
- `quoteId` opcional
- `entryDate`
- `entryMileage`
- `reason`
- `diagnosis`
- `workPerformed`
- `notes`
- `status`
- `finishedAt`
- `deliveredAt`

Estados:

- `PENDING`
- `IN_PROGRESS`
- `FINISHED`
- `DELIVERED`

Reglas:

- pertenece a un vehículo del mismo taller;
- puede vincular un presupuesto aprobado del mismo taller;
- los servicios finalizados o entregados permanecen disponibles como historial.

---

## 5. Relaciones

```text
User 1 ─────────── 0..1 Workshop
Workshop 1 ─────── N Customer
Customer 1 ─────── N Vehicle
Vehicle 1 ───────── N Appointment
Vehicle 1 ───────── N Quote
Quote 1 ─────────── N QuoteItem
Vehicle 1 ───────── N Service
Quote 0..1 ───────── 1 Service
```

No existe en el MVP:

```text
User 1 -> N Workshop
Workshop 1 -> N User
```

---

## 6. Reglas transversales

1. Todo dato operativo pertenece a un taller.
2. Un `USER` sólo opera sobre su propio taller.
3. El taller se obtiene desde la identidad autenticada, no desde un identificador confiado al frontend.
4. Las referencias entre cliente, vehículo, turno, presupuesto y servicio deben corresponder al mismo taller.
5. La patente debe ser única por taller.
6. El historial se deriva de servicios registrados.
7. Los cambios de estado deben respetar transiciones válidas.
8. `SYSTEM_ADMIN` administra usuarios de plataforma y no la operación de los talleres.

---

## 7. Evolución fuera del MVP

Si los clientes requieren colaboración o estructuras más grandes, podrá evaluarse:

- varios usuarios por taller;
- membresías de usuarios en talleres;
- roles internos;
- varios talleres por usuario;
- permisos granulares.

No se modelarán estas relaciones antes de validar que son necesarias.

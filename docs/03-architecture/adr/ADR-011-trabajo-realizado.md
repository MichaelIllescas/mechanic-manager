# ADR-011 — Trabajo Realizado en lugar de Orden de Trabajo con Ciclo de Vida

**Estado:** Accepted
**Fecha:** Septiembre 2026

## Contexto

ADR-009 separó el catálogo de servicios del hecho ocurrido y modeló ese hecho como `WorkOrder`: el ingreso de un vehículo al taller, con `entryDate`, `entryMileage`, `reason`, `diagnosis`, `finishedAt`, `deliveredAt` y un ciclo de estados `PENDING → IN_PROGRESS → FINISHED → DELIVERED`.

Al construir el módulo, el taller que lo va a usar no necesita seguir el trabajo mientras se hace. Lo que necesita es dejar registrado lo que se hizo sobre un vehículo, con sus ítems y su total, para consultarlo después como historial y entregar un comprobante.

Un ciclo de vida completo agrega estados, transiciones, pantallas para avanzar el estado, una vista de "órdenes abiertas" y campos de texto que nadie iba a completar, sin aportar al flujo que el MVP quiere validar.

El mismo módulo dejó abierto lo que ADR-010 declaró pendiente: cómo un ítem referencia un insumo.

## Decisión

### 1. La orden de trabajo pasa a ser un trabajo realizado

`WorkOrder` se conserva como nombre técnico, pero el concepto es un **trabajo realizado**: un registro de lo que efectivamente se hizo, sin ciclo de vida.

Atributos: `vehicleId`, `appointmentId` y `quoteId` opcionales, `performedAt` (fecha), `mileage` opcional, `status`, `total`, `notes` y sus ítems.

Estados: `COMPLETED` (inicial) y `CANCELLED`. Se puede pasar de uno a otro y editar el trabajo en cualquiera de los dos.

Se descartan, a propósito y no como pendiente:

- los estados `PENDING`, `IN_PROGRESS`, `FINISHED` y `DELIVERED`;
- los campos `reason`, `diagnosis`, `finishedAt` y `deliveredAt`;
- la consulta de "órdenes abiertas".

### 2. Flujo entre turno, presupuesto y trabajo

- Un turno puede vincular un presupuesto aprobado del mismo vehículo.
- Al completar un turno se crea automáticamente su trabajo realizado. Si el turno tiene un presupuesto aprobado que todavía no originó otro trabajo, sus ítems se **copian** al trabajo; si no, el trabajo nace sin ítems.
- Un trabajo también puede crearse a mano, vinculado o no a un turno y a un presupuesto.
- Un turno y un presupuesto originan como máximo un trabajo (restricción `UNIQUE` sobre `work_orders.appointment_id` y `work_orders.quote_id`).
- Un presupuesto que ya tiene un trabajo realizado no puede cambiar de estado. Editarlo, o editar el trabajo, sigue permitido: es una decisión de producto, no un olvido.

### 3. Origen de los ítems (resuelve el pendiente de ADR-010)

Un ítem de presupuesto o de trabajo realizado referencia como máximo un origen de catálogo: un `ServiceType` o un `Supply`, nunca los dos (`CHECK` en la base). Si no referencia ninguno, es un ítem manual.

Un ítem de catálogo no puede tener precio negativo. Un ítem manual sí, y así se representan los descuentos. El total de un presupuesto o de un trabajo no puede ser negativo.

Con esto un presupuesto puede distinguir mano de obra (ítems de servicio) de materiales (ítems de insumo) si alguna vez hace falta mostrar los importes por separado. Los ítems manuales no se clasifican.

## Consecuencias positivas

- el modelo coincide con lo que el taller hace y dice: "trabajo realizado";
- menos estados, menos transiciones y menos pantallas que mantener;
- el trabajo nace de un turno completado sin carga adicional;
- el presupuesto queda intacto como referencia histórica, porque los ítems se copian;
- el historial (ADR-007) sigue siendo una consulta derivada, ahora por vehículo o por cliente.

## Trade-offs

- no hay forma de registrar que un vehículo está en el taller en proceso: el sistema sólo conoce lo ya hecho;
- si un taller pide seguimiento del avance, motivo de ingreso o diagnóstico, hay que volver a incorporarlos con una nueva decisión. La migración sería aditiva: nuevas columnas y estados, sin tocar los datos existentes;
- cambiar libremente el estado de un presupuesto o de un trabajo simplifica la operación, pero permite estados poco coherentes (por ejemplo, un presupuesto rechazado con un trabajo). Sólo se bloquea el caso de un presupuesto con trabajo realizado.

## Relación con otros ADRs

- **ADR-009:** la separación entre catálogo y hecho ocurrido sigue vigente. Este ADR reemplaza el modelo del hecho ocurrido: pasa de una orden con ciclo de vida a un trabajo realizado.
- **ADR-010:** resuelve el punto que quedó pendiente sobre cómo un ítem referencia un insumo.
- **ADR-007:** el historial sigue derivado de los trabajos registrados.

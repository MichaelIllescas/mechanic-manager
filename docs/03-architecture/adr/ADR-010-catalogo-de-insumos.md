# ADR-010 — Catálogo de Insumos separado del Control de Stock

**Estado:** Accepted
**Fecha:** Agosto 2026

## Contexto

ADR-009 resolvió el catálogo de prestaciones del taller (`ServiceType`), pero dejó los materiales fuera: un repuesto se cargaba como ítem de texto libre, con nombre y precio escritos a mano en cada presupuesto y en cada orden.

Es el mismo problema que ADR-009 corrigió para la mano de obra, sin corregir para los materiales. Un taller que presupuesta un service completo retipea "Filtro de aceite Mann W712" y su precio cada vez, con el riesgo de escribirlo distinto y cobrarlo distinto.

El alcance original del MVP (`02-mvp-scope.md`) excluía tanto el catálogo de repuestos como el stock, tratándolos como una sola cosa. Son dos capacidades distintas con costos muy distintos:

- un **catálogo** es una tabla con un CRUD, idéntico en forma al de servicios;
- el **stock** exige movimientos de existencias reversibles, decidir en qué momento del ciclo de una orden se descuenta, qué ocurre al anularla, si se admite existencia negativa, y arrastra compras y proveedores detrás.

## Decisión

Incorporar `Supply` al MVP como catálogo, y mantener el control de existencias fuera.

`Supply` — catálogo de materiales del taller: `name`, `price`, `active`.

Reglas asociadas:

- pertenece a un `USER`, como todo dato operativo (ADR-003);
- `name` es único dentro de un usuario;
- `price` es precio de referencia y puede cambiar en el tiempo;
- un cambio de precio no altera presupuestos ni órdenes ya registrados;
- un insumo no se elimina nunca: se marca `active = false`.

Es deliberadamente el mismo contrato que `ServiceType`. `Supply` no tiene cantidad en existencia.

El control de stock permanece Post-MVP (`PB-36`).

## Consecuencias positivas

- el taller deja de retipear materiales y precios al presupuestar;
- el precio de un repuesto pasa a tener una fuente única y editable en un solo lugar;
- la simetría con `ServiceType` permite reutilizar la forma del módulo ya construido;
- la separación entre catálogo y existencias queda explícita, y la ausencia de stock se lee como decisión y no como olvido.

## Trade-offs

- una tabla y un módulo más en el MVP;
- se agrega un segundo catálogo en lugar de unificar ambos en una entidad genérica. La unificación se descartó: el stock, cuando llegue, colgará únicamente de `Supply`;
- un catálogo sin existencias puede leerse como funcionalidad incompleta por parte del usuario del taller. Es el recorte consciente que separa esta decisión de `PB-36`.

## Pendiente (resuelto en ADR-011)

Cómo un `QuoteItem` o un `WorkOrderItem` referencia a un `Supply` **no forma parte de esta decisión** y no está resuelto.

En particular queda abierto si el ítem debe distinguir explícitamente mano de obra de materiales, lo que condiciona la posibilidad de discriminar ambos importes en un presupuesto. Se registrará en un ADR posterior.

Hasta entonces, `ServiceType` sigue siendo el único origen de catálogo que un ítem puede referenciar.

**Resuelto:** ADR-011 define que un ítem referencia como máximo un origen de catálogo, un servicio o un insumo, y que un ítem sin origen es manual.

## Relación con ADR-002 y ADR-009

ADR-002 evita complejidad prematura. Esta decisión la respeta: incorpora la mitad barata del problema y posterga la cara.

ADR-009 estableció que el catálogo se separa del hecho ocurrido. ADR-010 aplica ese mismo criterio a los materiales: `Supply` es catálogo, el consumo real seguirá viviendo en los ítems.

Su regla de que un ítem puede describir "un repuesto suelto" sin referencia al catálogo sigue vigente: el catálogo de insumos es un origen posible, no obligatorio.

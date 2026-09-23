# ADR-009 — Separación entre Catálogo de Servicios y Orden de Trabajo

**Estado:** Accepted
**Fecha:** Agosto 2026

## Contexto

El modelo de dominio inicial definía una única entidad `Service` con los atributos `entryDate`, `entryMileage`, `reason`, `diagnosis`, `workPerformed` y un ciclo de estados `PENDING → IN_PROGRESS → FINISHED → DELIVERED`.

Esa entidad no describe un servicio. Describe el ingreso de un vehículo al taller: un hecho ocurrido, con fecha, kilometraje y estado.

En el lenguaje del taller, un servicio es otra cosa: "cambio de aceite", "alineación", "cambio de pastillas". Es una prestación ofrecida, con un precio de referencia, sin fecha y sin estado.

La confusión entre ambos conceptos tenía consecuencias concretas:

- `QuoteItem.description` era texto libre, por lo que cada presupuesto retipeaba las mismas prestaciones con precios cargados a mano;
- el trabajo ejecutado quedaba en `workPerformed`, un párrafo de texto, lo que impedía cualquier consulta agregada;
- no existía lista de precios del taller;
- el nombre `Service` colisionaba con el estereotipo `@Service` y con la convención de paquetes del proyecto.

## Decisión

Separar el concepto en tres entidades:

- **`ServiceType`** — catálogo de prestaciones del taller: `name`, `basePrice`, `active`.
- **`WorkOrder`** — el ingreso concreto del vehículo. Es la entidad antes llamada `Service`, sin `workPerformed` y con `total`.
- **`WorkOrderItem`** — cada servicio o repuesto efectivamente realizado dentro de una orden.

Reglas asociadas:

- la referencia de un ítem a `ServiceType` es **opcional**: un ítem puede describir trabajo no catalogado o un repuesto suelto;
- `description` y `unitPrice` se **copian** al crear el ítem y quedan congelados;
- un `ServiceType` utilizado no se elimina: se marca `active = false`;
- `QuoteItem` recibe el mismo tratamiento que `WorkOrderItem`.

## Consecuencias positivas

- el vocabulario del modelo coincide con el del taller;
- el taller tiene lista de precios y carga presupuestos sin retipear;
- el trabajo realizado queda como datos consultables y no como texto libre;
- el total de una orden se calcula en lugar de estimarse;
- el historial es estable frente a cambios de precio.

## Trade-offs

- una entidad más y una tabla de catálogo adicional respecto del modelo anterior;
- el precio duplicado entre catálogo e ítem es **deliberado**: son valores distintos, uno es precio vigente y el otro precio histórico. No es desnormalización accidental.

## Relación con ADR-002

ADR-002 evita complejidad prematura. Esta decisión no la contradice.

El renombre de `Service` a `WorkOrder` corrige un concepto mal nombrado y su costo crece con cada línea de código, endpoint y pantalla que se escriba encima. El catálogo es una tabla con un CRUD, no una estructura táctica de DDD, y habilita una capacidad central del producto.

Se toma esta decisión antes de escribir el módulo, que es el momento de menor costo.

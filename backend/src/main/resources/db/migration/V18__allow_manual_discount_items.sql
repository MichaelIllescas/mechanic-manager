-- Los importes negativos representan descuentos cargados como ítems manuales.
-- Los servicios e insumos del catálogo conservan la restricción de precio positivo.
ALTER TABLE quote_items
    DROP CHECK ck_quote_items_unit_price,
    DROP CHECK ck_quote_items_subtotal,
    ADD CONSTRAINT ck_quote_items_unit_price
        CHECK (service_type_id IS NULL AND supply_id IS NULL OR unit_price >= 0),
    ADD CONSTRAINT ck_quote_items_subtotal
        CHECK (service_type_id IS NULL AND supply_id IS NULL OR subtotal >= 0);

-- Al completar un turno, los ítems del presupuesto se copian al trabajo realizado.
ALTER TABLE work_order_items
    DROP CHECK ck_work_order_items_unit_price,
    DROP CHECK ck_work_order_items_subtotal,
    ADD CONSTRAINT ck_work_order_items_unit_price
        CHECK (service_type_id IS NULL AND supply_id IS NULL OR unit_price >= 0),
    ADD CONSTRAINT ck_work_order_items_subtotal
        CHECK (service_type_id IS NULL AND supply_id IS NULL OR subtotal >= 0);

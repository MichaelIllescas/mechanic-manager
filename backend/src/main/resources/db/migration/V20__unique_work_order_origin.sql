-- Un turno y un presupuesto originan como máximo una orden de trabajo.
--
-- Hasta ahora nada lo impedía: una orden creada a mano con el turno de otra,
-- o el turno completado después de cargar una orden manual, dejaba dos filas
-- con el mismo origen. Los casos de uso de borrado buscan "la" orden asociada
-- y fallaban con más de una.
--
-- MySQL permite varios NULL en una restricción UNIQUE, así que las órdenes
-- creadas sin turno o sin presupuesto no se ven afectadas.

ALTER TABLE work_orders
    ADD CONSTRAINT uq_work_orders_appointment UNIQUE (appointment_id),
    ADD CONSTRAINT uq_work_orders_quote UNIQUE (quote_id);

-- Redundantes: la restricción única ya provee el índice.
DROP INDEX idx_work_orders_appointment ON work_orders;
DROP INDEX idx_work_orders_quote ON work_orders;

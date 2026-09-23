-- Órdenes de trabajo: lo que realmente se hizo en el taller.
--
-- A diferencia del presupuesto (lo que se cotizó) o el turno (cuándo se citó
-- al vehículo), la orden de trabajo registra el trabajo efectivamente
-- realizado y es la base del historial de servicios del vehículo. Puede
-- nacer de un presupuesto aprobado (quote_id) y/o de un turno completado
-- (appointment_id), o crearse manualmente sin ninguno de los dos.
--
-- Los ítems no reutilizan quote_items: se copian a work_order_items para que
-- el presupuesto original quede intacto como referencia histórica aunque lo
-- realmente hecho difiera (otro repuesto, mano de obra extra, etc).

-- Vínculo turno-presupuesto: un turno puede tener como máximo un presupuesto
-- asociado. Se navega en ambos sentidos desde el código (quote.appointment,
-- y una consulta por appointment_id desde el lado de Appointment) sin
-- duplicar la referencia en la otra tabla.
ALTER TABLE quotes
    ADD COLUMN appointment_id BIGINT NULL AFTER vehicle_id,
    ADD CONSTRAINT uq_quotes_appointment UNIQUE (appointment_id),
    ADD CONSTRAINT fk_quotes_appointment
        FOREIGN KEY (appointment_id)
            REFERENCES appointments (id)
            ON DELETE SET NULL;

CREATE TABLE work_orders
(
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    user_id        BIGINT         NOT NULL,
    vehicle_id     BIGINT         NOT NULL,
    appointment_id BIGINT         NULL,
    quote_id       BIGINT         NULL,
    performed_at   DATE           NOT NULL,
    mileage        INT            NULL,
    status         VARCHAR(20)    NOT NULL DEFAULT 'COMPLETED',
    total          DECIMAL(14, 2) NOT NULL DEFAULT 0,
    notes          TEXT           NULL,
    created_at     DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                              ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_work_orders PRIMARY KEY (id),
    CONSTRAINT fk_work_orders_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_work_orders_vehicle
        FOREIGN KEY (vehicle_id)
            REFERENCES vehicles (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_work_orders_appointment
        FOREIGN KEY (appointment_id)
            REFERENCES appointments (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_work_orders_quote
        FOREIGN KEY (quote_id)
            REFERENCES quotes (id)
            ON DELETE SET NULL,
    CONSTRAINT ck_work_orders_status
        CHECK (status IN ('COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_work_orders_total CHECK (total >= 0),

    INDEX idx_work_orders_user_performed_at (user_id, performed_at),
    INDEX idx_work_orders_vehicle (vehicle_id),
    INDEX idx_work_orders_appointment (appointment_id),
    INDEX idx_work_orders_quote (quote_id)
);

CREATE TABLE work_order_items
(
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    work_order_id   BIGINT         NOT NULL,
    service_type_id BIGINT         NULL,
    supply_id       BIGINT         NULL,
    description     VARCHAR(500)   NOT NULL,
    quantity        DECIMAL(12, 3) NOT NULL,
    unit_price      DECIMAL(12, 2) NOT NULL,
    subtotal        DECIMAL(14, 2) NOT NULL,

    CONSTRAINT pk_work_order_items PRIMARY KEY (id),
    CONSTRAINT fk_work_order_items_work_order
        FOREIGN KEY (work_order_id)
            REFERENCES work_orders (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_work_order_items_service_type
        FOREIGN KEY (service_type_id)
            REFERENCES service_types (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_work_order_items_supply
        FOREIGN KEY (supply_id)
            REFERENCES supplies (id)
            ON DELETE RESTRICT,
    CONSTRAINT ck_work_order_items_single_source
        CHECK (service_type_id IS NULL OR supply_id IS NULL),
    CONSTRAINT ck_work_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT ck_work_order_items_unit_price CHECK (unit_price >= 0),
    CONSTRAINT ck_work_order_items_subtotal CHECK (subtotal >= 0),

    INDEX idx_work_order_items_work_order (work_order_id),
    INDEX idx_work_order_items_service_type (service_type_id),
    INDEX idx_work_order_items_supply (supply_id)
);

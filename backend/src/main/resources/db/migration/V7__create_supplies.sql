-- Catálogo de insumos y repuestos que utiliza el taller (ADR-010).
-- No representa existencias: cuántas unidades hay en el taller queda
-- fuera del MVP (PB-36). Acá sólo vive el material y su precio.
--
-- El catálogo pertenece al usuario y no al taller, igual que service_types:
-- en el MVP un usuario tiene un único taller, así que alcanza para aislar
-- los datos. Si en algún momento un taller tiene más de una persona,
-- esto pasa a workshop_id.
CREATE TABLE supplies
(
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    user_id    BIGINT         NOT NULL,
    name       VARCHAR(120)   NOT NULL,
    price      DECIMAL(12, 2) NOT NULL,
    active     BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                       ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_supplies PRIMARY KEY (id),
    CONSTRAINT uq_supplies_user_name UNIQUE (user_id, name),
    CONSTRAINT fk_supplies_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,
    CONSTRAINT ck_supplies_price CHECK (price >= 0)
);

-- Listado del catálogo activo: es la consulta por defecto de la pantalla
-- de insumos y la que alimenta el alta de ítems en presupuestos y órdenes.
CREATE INDEX ix_supplies_user_active
    ON supplies (user_id, active);

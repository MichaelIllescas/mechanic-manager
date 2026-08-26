-- Catálogo de servicios que ofrece el taller (ADR-009).
-- No representa trabajo realizado: eso son las órdenes de trabajo,
-- que se crean cuando existan las tablas vehicles y quotes.
--
-- El catálogo pertenece al usuario y no al taller: en el MVP un usuario
-- tiene un único taller, así que alcanza para aislar los datos. Si en algún
-- momento un taller tiene más de una persona, esto pasa a workshop_id.
CREATE TABLE service_types
(
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    user_id           BIGINT         NOT NULL,
    name              VARCHAR(120)   NOT NULL,
    description       VARCHAR(500)   NULL,
    base_price        DECIMAL(12, 2) NOT NULL,
    estimated_minutes INT            NULL,
    active            BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                             ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_service_types PRIMARY KEY (id),
    CONSTRAINT uq_service_types_user_name UNIQUE (user_id, name),
    CONSTRAINT fk_service_types_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,
    CONSTRAINT ck_service_types_base_price CHECK (base_price >= 0),
    CONSTRAINT ck_service_types_estimated_minutes CHECK (estimated_minutes IS NULL OR estimated_minutes > 0)
);

-- Listado del catálogo activo: es la consulta por defecto de la pantalla
-- de servicios y la que alimenta el alta de ítems en presupuestos y órdenes.
CREATE INDEX ix_service_types_user_active
    ON service_types (user_id, active);

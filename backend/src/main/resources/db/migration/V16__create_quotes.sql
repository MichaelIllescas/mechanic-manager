CREATE TABLE quotes
(
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    user_id     BIGINT         NOT NULL,
    customer_id BIGINT         NOT NULL,
    vehicle_id  BIGINT         NOT NULL,
    number      BIGINT         NOT NULL,
    valid_until DATE           NULL,
    status      VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    total       DECIMAL(14, 2) NOT NULL DEFAULT 0,
    notes       TEXT           NULL,
    created_at  DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                           ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_quotes PRIMARY KEY (id),
    CONSTRAINT uq_quotes_user_number UNIQUE (user_id, number),
    CONSTRAINT fk_quotes_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_quotes_customer
        FOREIGN KEY (customer_id)
            REFERENCES customers (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_quotes_vehicle
        FOREIGN KEY (vehicle_id)
            REFERENCES vehicles (id)
            ON DELETE RESTRICT,
    CONSTRAINT ck_quotes_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT ck_quotes_total CHECK (total >= 0),

    INDEX idx_quotes_user_created_at (user_id, created_at),
    INDEX idx_quotes_user_status (user_id, status),
    INDEX idx_quotes_customer (customer_id),
    INDEX idx_quotes_vehicle (vehicle_id)
);

CREATE TABLE quote_items
(
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    quote_id        BIGINT         NOT NULL,
    service_type_id BIGINT         NULL,
    supply_id       BIGINT         NULL,
    description     VARCHAR(500)   NOT NULL,
    quantity        DECIMAL(12, 3) NOT NULL,
    unit_price      DECIMAL(12, 2) NOT NULL,
    subtotal        DECIMAL(14, 2) NOT NULL,

    CONSTRAINT pk_quote_items PRIMARY KEY (id),
    CONSTRAINT fk_quote_items_quote
        FOREIGN KEY (quote_id)
            REFERENCES quotes (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_quote_items_service_type
        FOREIGN KEY (service_type_id)
            REFERENCES service_types (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_quote_items_supply
        FOREIGN KEY (supply_id)
            REFERENCES supplies (id)
            ON DELETE RESTRICT,
    CONSTRAINT ck_quote_items_single_source
        CHECK (service_type_id IS NULL OR supply_id IS NULL),
    CONSTRAINT ck_quote_items_quantity CHECK (quantity > 0),
    CONSTRAINT ck_quote_items_unit_price CHECK (unit_price >= 0),
    CONSTRAINT ck_quote_items_subtotal CHECK (subtotal >= 0),

    INDEX idx_quote_items_quote (quote_id),
    INDEX idx_quote_items_service_type (service_type_id),
    INDEX idx_quote_items_supply (supply_id)
);

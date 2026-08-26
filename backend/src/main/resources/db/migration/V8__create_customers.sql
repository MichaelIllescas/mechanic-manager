CREATE TABLE customers
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    workshop_id     BIGINT       NOT NULL,
    first_name      VARCHAR(120) NOT NULL,
    last_name       VARCHAR(120) NOT NULL,
    document_number VARCHAR(30)  NULL,
    phone           VARCHAR(30)  NOT NULL,
    email           VARCHAR(255) NULL,
    notes           TEXT         NULL,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                         ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT fk_customers_workshop
        FOREIGN KEY (workshop_id)
            REFERENCES workshops (id)
            ON DELETE RESTRICT,

    INDEX idx_customers_workshop (workshop_id),
    INDEX idx_customers_workshop_name (workshop_id, last_name, first_name),
    INDEX idx_customers_workshop_document (workshop_id, document_number)
);

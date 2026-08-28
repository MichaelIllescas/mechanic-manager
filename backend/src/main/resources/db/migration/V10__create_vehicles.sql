CREATE TABLE vehicles
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    workshop_id     BIGINT       NOT NULL,
    customer_id     BIGINT       NOT NULL,
    license_plate   VARCHAR(20)  NOT NULL,
    brand           VARCHAR(80)  NOT NULL,
    model           VARCHAR(120) NOT NULL,
    year            INT          NULL,
    chassis_number  VARCHAR(100) NOT NULL,
    engine_number   VARCHAR(100) NOT NULL,
    mileage         INT          NULL,
    vehicle_type    VARCHAR(40)  NOT NULL,
    notes           TEXT         NULL,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                         ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_vehicles PRIMARY KEY (id),
    CONSTRAINT uq_vehicles_workshop_license_plate
        UNIQUE (workshop_id, license_plate),
    CONSTRAINT fk_vehicles_workshop
        FOREIGN KEY (workshop_id)
            REFERENCES workshops (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_vehicles_customer
        FOREIGN KEY (customer_id)
            REFERENCES customers (id)
            ON DELETE RESTRICT,
    CONSTRAINT ck_vehicles_year CHECK (year IS NULL OR year BETWEEN 1886 AND 9999),
    CONSTRAINT ck_vehicles_mileage CHECK (mileage IS NULL OR mileage >= 0),

    INDEX idx_vehicles_workshop (workshop_id),
    INDEX idx_vehicles_customer (customer_id),
    INDEX idx_vehicles_workshop_brand_model (workshop_id, brand, model)
);

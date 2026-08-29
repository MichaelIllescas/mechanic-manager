-- ADR-003: los datos operativos se restringen por el usuario autenticado.
-- Continúa la migración iniciada en V11: vehicles pasa de workshop_id a user_id.
-- La unicidad de patente pasa a definirse por usuario. No puede fallar por
-- duplicados: workshops.user_id es único, así que la unicidad por taller que
-- ya regía garantiza la unicidad por usuario.

ALTER TABLE vehicles
    ADD COLUMN user_id BIGINT NULL AFTER id;

UPDATE vehicles v
    JOIN workshops w ON w.id = v.workshop_id
SET v.user_id = w.user_id;

ALTER TABLE vehicles
    MODIFY COLUMN user_id BIGINT NOT NULL;

ALTER TABLE vehicles
    ADD CONSTRAINT fk_vehicles_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT;

ALTER TABLE vehicles
    ADD CONSTRAINT uq_vehicles_user_license_plate
        UNIQUE (user_id, license_plate);

-- La foránea hacia customers pasa a ser compuesta: la base garantiza que el
-- vehículo y su cliente pertenezcan al mismo usuario, sin depender de que cada
-- camino de escritura recuerde filtrar por dueño.
ALTER TABLE vehicles
    DROP FOREIGN KEY fk_vehicles_customer;

CREATE INDEX idx_vehicles_customer_user ON vehicles (customer_id, user_id);

ALTER TABLE vehicles
    ADD CONSTRAINT fk_vehicles_customer_user
        FOREIGN KEY (customer_id, user_id)
            REFERENCES customers (id, user_id)
            ON DELETE RESTRICT;

-- Redundante: customer_id es prefijo izquierdo de idx_vehicles_customer_user.
DROP INDEX idx_vehicles_customer ON vehicles;

CREATE INDEX idx_vehicles_user ON vehicles (user_id);
CREATE INDEX idx_vehicles_user_brand_model ON vehicles (user_id, brand, model);

ALTER TABLE vehicles
    DROP FOREIGN KEY fk_vehicles_workshop;

ALTER TABLE vehicles
    DROP INDEX uq_vehicles_workshop_license_plate;

DROP INDEX idx_vehicles_workshop ON vehicles;
DROP INDEX idx_vehicles_workshop_brand_model ON vehicles;

ALTER TABLE vehicles
    DROP COLUMN workshop_id;

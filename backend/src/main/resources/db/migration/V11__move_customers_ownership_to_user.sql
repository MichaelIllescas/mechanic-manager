-- ADR-003: los datos operativos se restringen por el usuario autenticado.
-- customers había quedado colgado del taller; se lo pasa a user_id.
-- workshops.user_id es único y NOT NULL, así que el backfill es unívoco
-- y no puede dejar filas sin dueño.

ALTER TABLE customers
    ADD COLUMN user_id BIGINT NULL AFTER id;

UPDATE customers c
    JOIN workshops w ON w.id = c.workshop_id
SET c.user_id = w.user_id;

ALTER TABLE customers
    MODIFY COLUMN user_id BIGINT NOT NULL;

ALTER TABLE customers
    ADD CONSTRAINT fk_customers_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT;

-- Clave candidata que habilita la foránea compuesta de vehicles (V12):
-- permite exigir que un vehículo y su cliente pertenezcan al mismo usuario.
ALTER TABLE customers
    ADD CONSTRAINT uq_customers_id_user
        UNIQUE (id, user_id);

CREATE INDEX idx_customers_user ON customers (user_id);
CREATE INDEX idx_customers_user_name ON customers (user_id, last_name, first_name);
CREATE INDEX idx_customers_user_document ON customers (user_id, document_number);

ALTER TABLE customers
    DROP FOREIGN KEY fk_customers_workshop;

DROP INDEX idx_customers_workshop ON customers;
DROP INDEX idx_customers_workshop_name ON customers;
DROP INDEX idx_customers_workshop_document ON customers;

ALTER TABLE customers
    DROP COLUMN workshop_id;

-- El vínculo turno-presupuesto pasa a guardarse en el turno.
--
-- Antes lo guardaba el presupuesto (quotes.appointment_id, único), así que un
-- presupuesto solo podía apuntar a un turno. Al cancelar el turno el
-- presupuesto quedaba bloqueado, o había que perder el vínculo para
-- reutilizarlo. Con la referencia del lado del turno, varios turnos pueden
-- apuntar al mismo presupuesto: los cancelados lo conservan como historial y
-- solo puede haber uno activo (regla validada en el código).
ALTER TABLE appointments
    ADD COLUMN quote_id BIGINT NULL AFTER vehicle_id,
    ADD CONSTRAINT fk_appointments_quote
        FOREIGN KEY (quote_id)
            REFERENCES quotes (id)
            ON DELETE SET NULL,
    ADD INDEX idx_appointments_quote (quote_id);

-- updated_at se reasigna a sí mismo para que el ON UPDATE no lo pise.
UPDATE appointments a
    JOIN quotes q ON q.appointment_id = a.id
SET a.quote_id   = q.id,
    a.updated_at = a.updated_at;

ALTER TABLE quotes
    DROP FOREIGN KEY fk_quotes_appointment,
    DROP INDEX uq_quotes_appointment,
    DROP COLUMN appointment_id;

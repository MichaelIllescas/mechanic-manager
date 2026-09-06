-- Agenda de turnos del taller (PB-15 a PB-19).
--
-- El turno se agenda contra un vehículo: el cliente se deduce del vehículo y
-- no se guarda acá. Guardar ambos permitiría que un turno referencie un
-- vehículo y un cliente que no son entre sí, un estado inconsistente que el
-- modelo no necesita.
--
-- La agenda no valida solapamiento de horarios: un taller atiende varios
-- vehículos en simultáneo y el MVP no modela empleados, así que no hay
-- criterio real para decidir qué es un conflicto.
--
-- El turno no describe el trabajo a realizar. Al agendar sólo se conoce el
-- motivo que menciona el cliente, que va como texto libre en notes; el
-- detalle estructurado surge del presupuesto o de la orden de trabajo.

-- Necesaria para la foránea compuesta de appointments: MySQL exige un índice
-- sobre las columnas referenciadas. customers ya la tiene desde V11.
ALTER TABLE vehicles
    ADD CONSTRAINT uq_vehicles_id_user
        UNIQUE (id, user_id);

CREATE TABLE appointments
(
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    user_id      BIGINT      NOT NULL,
    vehicle_id   BIGINT      NOT NULL,
    scheduled_at DATETIME(6) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    notes        TEXT        NULL,
    created_at   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                      ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_appointments PRIMARY KEY (id),
    CONSTRAINT fk_appointments_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    -- Compuesta como en V12: la base garantiza que el turno y su vehículo
    -- pertenezcan al mismo usuario, sin depender de que cada camino de
    -- escritura recuerde filtrar por dueño.
    CONSTRAINT fk_appointments_vehicle_user
        FOREIGN KEY (vehicle_id, user_id)
            REFERENCES vehicles (id, user_id)
            ON DELETE RESTRICT,

    -- SCHEDULED es el estado inicial; COMPLETED y CANCELLED son terminales.
    -- COMPLETED significa que el vehículo ingresó al taller: a partir de ahí
    -- el estado del trabajo lo lleva la orden de trabajo, no el turno.
    CONSTRAINT ck_appointments_status
        CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED'))
);

-- Consulta central del módulo: la agenda de un día para el usuario autenticado.
CREATE INDEX idx_appointments_user_scheduled_at
    ON appointments (user_id, scheduled_at);

-- Historial de turnos de un vehículo, desde su ficha.
CREATE INDEX idx_appointments_vehicle
    ON appointments (vehicle_id);

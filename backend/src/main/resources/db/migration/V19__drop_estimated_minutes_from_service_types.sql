-- La duración estimada nunca se usó en ningún flujo del MVP.
ALTER TABLE service_types DROP CONSTRAINT ck_service_types_estimated_minutes;
ALTER TABLE service_types DROP COLUMN estimated_minutes;

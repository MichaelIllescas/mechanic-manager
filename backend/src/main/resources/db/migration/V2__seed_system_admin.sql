-- Cuenta SYSTEM_ADMIN inicial para desarrollo.
--
-- Credenciales: admin@mechanicmanager.local / Admin123!
--
-- ATENCION: el hash corresponde a una contrasena conocida y publica en este
-- repositorio. Antes de desplegar a produccion, cambiar la contrasena de esta
-- cuenta o deshabilitarla.
INSERT INTO users (name, email, password_hash, role, status)
VALUES ('Administrador',
        'admin@mechanicmanager.local',
        '$2a$10$yA0DNHsvGO53WlYWydm2oekeLSbD80/PpidOjUpj9VuAWcAkhO9KG',
        'SYSTEM_ADMIN',
        'ENABLED');

-- El taller se crea vacío junto con el usuario y se completa después,
-- por lo que name y phone dejan de ser obligatorios a nivel de tabla.
ALTER TABLE workshops
    MODIFY COLUMN name VARCHAR(120) NULL,
    MODIFY COLUMN phone VARCHAR(30) NULL;

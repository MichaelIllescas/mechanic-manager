-- Los usuarios creados antes de que el alta generara el taller automáticamente
-- se quedaron sin uno. Solo aplica al rol USER: un SYSTEM_ADMIN no tiene taller.
INSERT INTO workshops (user_id)
SELECT u.id
FROM users u
         LEFT JOIN workshops w ON w.user_id = u.id
WHERE w.id IS NULL
  AND u.role = 'USER';

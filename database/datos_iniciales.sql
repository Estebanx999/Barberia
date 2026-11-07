-- =============================================================================
-- Datos iniciales del MVP: cuentas del dueño y de dos barberos.
--
-- El registro de barberos (HU-09 y HU-10) está fuera del MVP, así que sus
-- cuentas se crean con este script. Los clientes se registran desde la app (HU-21).
--
-- Contraseña inicial de las tres cuentas: Barberia2026
-- Cámbienla antes de usar el sistema con datos reales.
--
-- Uso (después de barberia.sql):
--   psql -d barberia -f datos_iniciales.sql
-- =============================================================================

BEGIN;

INSERT INTO usuario (tipo, nombre, correo, telefono, contrasena_hash) VALUES
    ('DUENO',   'Dueño de la barbería', 'dueno@barberia.com',    '3000000001', '$2b$10$X0ha6s8lCnxfHwRUq1p2luRWqYg9h.CrFu58ztHiNwQd6JJm3Y8qG'),
    ('BARBERO', 'Barbero Uno',          'barbero1@barberia.com', '3000000002', '$2b$10$X0ha6s8lCnxfHwRUq1p2luRWqYg9h.CrFu58ztHiNwQd6JJm3Y8qG'),
    ('BARBERO', 'Barbero Dos',          'barbero2@barberia.com', '3000000003', '$2b$10$X0ha6s8lCnxfHwRUq1p2luRWqYg9h.CrFu58ztHiNwQd6JJm3Y8qG');

COMMIT;

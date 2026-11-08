-- =============================================================================
-- Actualiza una base creada antes de agregar el servicio de la cita y el
-- perfil del barbero. Se puede ejecutar varias veces sin efecto adicional.
--
-- Uso (como el usuario barberia):
--   psql -d barberia -f actualizar_servicio_y_perfil.sql
-- =============================================================================

BEGIN;

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS descripcion VARCHAR(300);
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS foto_url    VARCHAR(500);

-- Las citas anteriores eran de un turno de 30 minutos: quedan como corte.
ALTER TABLE cita ADD COLUMN IF NOT EXISTS servicio VARCHAR(20) NOT NULL DEFAULT 'CORTE';
ALTER TABLE cita ALTER COLUMN servicio DROP DEFAULT;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_cita_servicio') THEN
        ALTER TABLE cita ADD CONSTRAINT ck_cita_servicio
            CHECK (servicio IN ('CORTE', 'BARBA', 'CORTE_Y_BARBA'));
    END IF;
END $$;

-- Perfil de las cuentas iniciales, solo si aún no tienen uno.
UPDATE usuario SET nombre = 'Mateo Herrera',
                   descripcion = 'Cortes clásicos y diseño de barba, con más de 10 años de experiencia.'
 WHERE correo = 'dueno@barberia.com' AND descripcion IS NULL;
UPDATE usuario SET nombre = 'Carlos Ramírez',
                   descripcion = 'Especialista en degradados y estilos modernos.'
 WHERE correo = 'barbero1@barberia.com' AND descripcion IS NULL;
UPDATE usuario SET nombre = 'Luis Peña',
                   descripcion = 'Experto en afeitado tradicional y cuidado de la barba.'
 WHERE correo = 'barbero2@barberia.com' AND descripcion IS NULL;

COMMIT;

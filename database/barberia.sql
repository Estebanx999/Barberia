-- =============================================================================
-- Sistema de gestión de citas y control contable - Barbería
-- Esquema PostgreSQL del MVP (Release 1)
--
-- Uso:
--   createdb barberia
--   psql -d barberia -f barberia.sql
--
-- Las tablas corresponden a las entidades del dominio (nivel 4 del modelo C4).
-- =============================================================================

BEGIN;

-- -----------------------------------------------------------------------------
-- Usuarios: clientes, barberos y dueño en una sola tabla (herencia SINGLE_TABLE).
-- El dueño también es barbero (RN-02).
-- -----------------------------------------------------------------------------
CREATE TABLE usuario (
    id       BIGSERIAL    PRIMARY KEY,
    tipo     VARCHAR(20)  NOT NULL,
    nombre   VARCHAR(120) NOT NULL,
    correo   VARCHAR(160),
    telefono VARCHAR(30),
    contrasena_hash VARCHAR(100),
    CONSTRAINT ck_usuario_tipo   CHECK (tipo IN ('CLIENTE', 'BARBERO', 'DUENO')),
    CONSTRAINT ck_usuario_nombre CHECK (btrim(nombre) <> ''),
    CONSTRAINT uq_usuario_correo UNIQUE (correo)
);

COMMENT ON TABLE  usuario      IS 'Personas del sistema: clientes, barberos y dueño';
COMMENT ON COLUMN usuario.tipo IS 'CLIENTE, BARBERO o DUENO';
COMMENT ON COLUMN usuario.contrasena_hash IS 'Hash BCrypt; nulo si el usuario no tiene cuenta (cliente registrado en un corte sin cita)';

ALTER TABLE usuario ADD CONSTRAINT ck_usuario_cuenta_con_correo CHECK (contrasena_hash IS NULL OR correo IS NOT NULL);

-- -----------------------------------------------------------------------------
-- Citas (HU-01, HU-02, HU-05, HU-06, HU-07, HU-12)
-- -----------------------------------------------------------------------------
CREATE TABLE cita (
    id          BIGSERIAL   PRIMARY KEY,
    fecha       DATE        NOT NULL,
    hora_inicio TIME        NOT NULL,
    hora_fin    TIME        NOT NULL,
    estado      VARCHAR(20) NOT NULL DEFAULT 'AGENDADA',
    cliente_id  BIGINT      NOT NULL,
    barbero_id  BIGINT      NOT NULL,
    CONSTRAINT fk_cita_cliente FOREIGN KEY (cliente_id) REFERENCES usuario (id),
    CONSTRAINT fk_cita_barbero FOREIGN KEY (barbero_id) REFERENCES usuario (id),
    CONSTRAINT ck_cita_estado  CHECK (estado IN ('AGENDADA', 'ATENDIDA')),
    CONSTRAINT ck_cita_horas   CHECK (hora_fin > hora_inicio)
);

CREATE INDEX ix_cita_barbero_fecha ON cita (barbero_id, fecha);
CREATE INDEX ix_cita_fecha         ON cita (fecha, hora_inicio);

COMMENT ON TABLE  cita        IS 'Citas agendadas; el cruce de horario (RN-04) se valida en la aplicación';
COMMENT ON COLUMN cita.estado IS 'AGENDADA o ATENDIDA';

-- -----------------------------------------------------------------------------
-- Comisiones (HU-13, RN-01, RN-02): reparto del valor cobrado de un corte.
-- -----------------------------------------------------------------------------
CREATE TABLE comision (
    id                 BIGSERIAL      PRIMARY KEY,
    monto_barbero      NUMERIC(12, 2) NOT NULL,
    monto_dueno        NUMERIC(12, 2) NOT NULL,
    porcentaje_barbero NUMERIC(5, 2)  NOT NULL,
    porcentaje_dueno   NUMERIC(5, 2)  NOT NULL,
    CONSTRAINT ck_comision_montos      CHECK (monto_barbero >= 0 AND monto_dueno >= 0),
    CONSTRAINT ck_comision_porcentajes CHECK (porcentaje_barbero BETWEEN 0 AND 100
                                          AND porcentaje_dueno   BETWEEN 0 AND 100
                                          AND porcentaje_barbero + porcentaje_dueno = 100)
);

COMMENT ON TABLE comision IS 'Reparto barbero/dueño: 60/40 si corta un barbero; 100 % para el dueño si corta él';

-- -----------------------------------------------------------------------------
-- Cortes (HU-27, HU-28): servicio realizado y cobrado, con o sin cita previa.
-- -----------------------------------------------------------------------------
CREATE TABLE corte (
    id                BIGSERIAL      PRIMARY KEY,
    fecha             DATE           NOT NULL,
    servicio          VARCHAR(120)   NOT NULL,
    valor_cobrado     NUMERIC(12, 2) NOT NULL,
    tiene_cita_previa BOOLEAN        NOT NULL,
    cita_id           BIGINT,
    barbero_id        BIGINT         NOT NULL,
    cliente_id        BIGINT         NOT NULL,
    comision_id       BIGINT,
    CONSTRAINT fk_corte_cita       FOREIGN KEY (cita_id)     REFERENCES cita (id),
    CONSTRAINT fk_corte_barbero    FOREIGN KEY (barbero_id)  REFERENCES usuario (id),
    CONSTRAINT fk_corte_cliente    FOREIGN KEY (cliente_id)  REFERENCES usuario (id),
    CONSTRAINT fk_corte_comision   FOREIGN KEY (comision_id) REFERENCES comision (id),
    CONSTRAINT uq_corte_cita       UNIQUE (cita_id),
    CONSTRAINT uq_corte_comision   UNIQUE (comision_id),
    CONSTRAINT ck_corte_valor      CHECK (valor_cobrado > 0),
    CONSTRAINT ck_corte_servicio   CHECK (btrim(servicio) <> ''),
    CONSTRAINT ck_corte_cita_previa CHECK (tiene_cita_previa = (cita_id IS NOT NULL))
);

CREATE INDEX ix_corte_fecha   ON corte (fecha);
CREATE INDEX ix_corte_barbero ON corte (barbero_id, fecha);

COMMENT ON TABLE  corte                   IS 'Cortes atendidos; fuente de los ingresos del sistema';
COMMENT ON COLUMN corte.cliente_id        IS 'Cliente atendido; en cortes con cita coincide con cita.cliente_id';
COMMENT ON COLUMN corte.tiene_cita_previa IS 'Verdadero si el corte viene de una cita (HU-27); falso si fue sin cita (HU-28)';

-- -----------------------------------------------------------------------------
-- Gastos operativos (HU-14)
-- -----------------------------------------------------------------------------
CREATE TABLE gasto (
    id       BIGSERIAL      PRIMARY KEY,
    concepto VARCHAR(160)   NOT NULL,
    fecha    DATE           NOT NULL,
    valor    NUMERIC(12, 2) NOT NULL,
    CONSTRAINT ck_gasto_valor    CHECK (valor > 0),
    CONSTRAINT ck_gasto_concepto CHECK (btrim(concepto) <> '')
);

CREATE INDEX ix_gasto_fecha ON gasto (fecha);

COMMENT ON TABLE gasto IS 'Gastos operativos; se restan en la utilidad neta (RN-03)';

COMMIT;

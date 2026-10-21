CREATE TABLE IF NOT EXISTS cliente (
                                       id        SERIAL PRIMARY KEY,
                                       nombres   VARCHAR(80)  NOT NULL,
    apellidos VARCHAR(80)  NOT NULL,
    telefono  VARCHAR(20)  NOT NULL,
    correo    VARCHAR(120) NOT NULL UNIQUE,
    activo    BOOLEAN      NOT NULL DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS cita_tecnica (
                                            id            SERIAL PRIMARY KEY,
                                            cliente_id    INT          NOT NULL REFERENCES cliente(id),
    fecha         DATE         NOT NULL,
    hora          TIME         NOT NULL,
    tipo_servicio VARCHAR(80)  NOT NULL,
    descripcion   VARCHAR(255) NOT NULL,
    estado        VARCHAR(20)  NOT NULL
    CHECK (estado IN ('Pendiente','En proceso','Finalizada','Cancelada'))
    );

CREATE INDEX IF NOT EXISTS idx_cita_cliente ON cita_tecnica(cliente_id);
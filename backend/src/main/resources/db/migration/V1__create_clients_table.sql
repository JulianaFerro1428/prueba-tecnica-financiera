-- Clientes del banco.
CREATE TABLE clients (
    id                    BIGSERIAL    PRIMARY KEY,
    identification_type   VARCHAR(2)   NOT NULL,
    identification_number VARCHAR(20)  NOT NULL,
    first_names           VARCHAR(100) NOT NULL,
    last_name             VARCHAR(100) NOT NULL,
    email                 VARCHAR(150) NOT NULL,
    birth_date            DATE         NOT NULL,
    created_at            TIMESTAMP    NOT NULL,
    updated_at            TIMESTAMP    NOT NULL,

    -- Una misma persona no puede registrarse dos veces con el mismo documento.
    CONSTRAINT uk_clients_identification UNIQUE (identification_type, identification_number),
    CONSTRAINT ck_clients_identification_type CHECK (identification_type IN ('CC', 'CE', 'PA')),
    -- Segunda barrera para la regla del PDF: aunque alguien inserte directo en la base,
    -- el cliente debía tener 18 años cumplidos el día en que se registró.
    CONSTRAINT ck_clients_adult CHECK (birth_date <= (created_at - INTERVAL '18 years')::date)
);

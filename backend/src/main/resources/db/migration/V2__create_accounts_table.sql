-- Consecutivos para generar números de cuenta únicos, uno por tipo.
-- PostgreSQL nunca entrega dos veces el mismo valor, aunque dos personas abran cuentas al mismo tiempo.
CREATE SEQUENCE savings_account_number_seq  START 1 MAXVALUE 99999999 NO CYCLE;
CREATE SEQUENCE checking_account_number_seq START 1 MAXVALUE 99999999 NO CYCLE;

-- Productos financieros: cuentas de ahorros y corrientes.
CREATE TABLE accounts (
    id                BIGSERIAL     PRIMARY KEY,
    account_type      VARCHAR(10)   NOT NULL,
    account_number    VARCHAR(10)   NOT NULL,
    status            VARCHAR(10)   NOT NULL,
    balance           NUMERIC(19,2) NOT NULL,
    available_balance NUMERIC(19,2) NOT NULL,
    gmf_exempt        BOOLEAN       NOT NULL,
    client_id         BIGINT        NOT NULL,
    created_at        TIMESTAMP     NOT NULL,
    updated_at        TIMESTAMP     NOT NULL,

    CONSTRAINT uk_accounts_number UNIQUE (account_number),
    -- Una cuenta solo existe si su cliente existe, y un cliente con cuentas no se puede borrar.
    CONSTRAINT fk_accounts_client FOREIGN KEY (client_id) REFERENCES clients (id) ON DELETE RESTRICT,
    CONSTRAINT ck_accounts_type CHECK (account_type IN ('SAVINGS', 'CHECKING')),
    CONSTRAINT ck_accounts_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'CANCELLED')),
    -- 10 dígitos: 53 para ahorros, 33 para corriente.
    CONSTRAINT ck_accounts_number_format CHECK (
        (account_type = 'SAVINGS'  AND account_number ~ '^53[0-9]{8}$') OR
        (account_type = 'CHECKING' AND account_number ~ '^33[0-9]{8}$')
    ),
    -- El PDF lo exige para ahorros; por decisión del proyecto (sin sobregiro definido) aplica a ambas.
    CONSTRAINT ck_accounts_non_negative_balance CHECK (balance >= 0 AND available_balance >= 0),
    CONSTRAINT ck_accounts_cancelled_with_zero CHECK (status <> 'CANCELLED' OR balance = 0)
);

-- Acelera "las cuentas de este cliente" y la revisión antes de borrar un cliente.
CREATE INDEX idx_accounts_client_id ON accounts (client_id);

-- Historial de movimientos: cada cambio de saldo deja una fila. Las filas no se editan ni se borran.
CREATE TABLE movements (
    id                      BIGSERIAL     PRIMARY KEY,
    reference               UUID          NOT NULL,
    account_id              BIGINT        NOT NULL,
    transaction_type        VARCHAR(10)   NOT NULL,
    movement_type           VARCHAR(6)    NOT NULL,
    amount                  NUMERIC(19,2) NOT NULL,
    balance_after           NUMERIC(19,2) NOT NULL,
    counterparty_account_id BIGINT,
    created_at              TIMESTAMP     NOT NULL,

    CONSTRAINT fk_movements_account      FOREIGN KEY (account_id)              REFERENCES accounts (id) ON DELETE RESTRICT,
    CONSTRAINT fk_movements_counterparty FOREIGN KEY (counterparty_account_id) REFERENCES accounts (id) ON DELETE RESTRICT,
    CONSTRAINT ck_movements_transaction_type CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),
    CONSTRAINT ck_movements_movement_type    CHECK (movement_type IN ('CREDIT', 'DEBIT')),
    CONSTRAINT ck_movements_amount_positive  CHECK (amount > 0),
    CONSTRAINT ck_movements_balance_after_non_negative CHECK (balance_after >= 0),
    -- Coherencia: la consignación siempre es crédito, el retiro siempre débito,
    -- y solo la transferencia tiene contraparte (y nunca es la misma cuenta).
    CONSTRAINT ck_movements_consistency CHECK (
        (transaction_type = 'DEPOSIT'    AND movement_type = 'CREDIT' AND counterparty_account_id IS NULL) OR
        (transaction_type = 'WITHDRAWAL' AND movement_type = 'DEBIT'  AND counterparty_account_id IS NULL) OR
        (transaction_type = 'TRANSFER'   AND counterparty_account_id IS NOT NULL AND counterparty_account_id <> account_id)
    )
);

-- El estado de cuenta busca los movimientos de una cuenta ordenados por fecha.
CREATE INDEX idx_movements_account_created ON movements (account_id, created_at DESC);
-- Permite encontrar las dos partes de una transferencia por su comprobante.
CREATE INDEX idx_movements_reference ON movements (reference);

-- Decisión del proyecto: un cliente tiene como máximo una cuenta de cada tipo que no esté cancelada.
-- El servicio ya lo revisa antes de abrir la cuenta; este índice es la última barrera si dos personas
-- abren al mismo tiempo una cuenta del mismo tipo para el mismo cliente.
-- "WHERE status <> 'CANCELLED'" hace que las cuentas canceladas no cuenten para la regla.
CREATE UNIQUE INDEX uk_accounts_one_open_per_type
    ON accounts (client_id, account_type)
    WHERE status <> 'CANCELLED';

package com.bank.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro de un cambio de saldo en una cuenta. Es inmutable: un movimiento ya hecho no se edita,
 * para conservar la trazabilidad del dinero.
 *
 * @param reference             comprobante de la operación; los dos movimientos de una transferencia lo comparten
 * @param balanceAfter          saldo de la cuenta justo después de este movimiento
 * @param counterpartyAccountId en transferencias, la otra cuenta; null en consignaciones y retiros
 */
public record Movement(
        Long id,
        UUID reference,
        Long accountId,
        TransactionType transactionType,
        MovementType movementType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Long counterpartyAccountId,
        LocalDateTime createdAt
) {
}

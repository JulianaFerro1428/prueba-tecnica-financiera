package com.bank.infrastructure.adapter.in.rest.dto;

import com.bank.domain.model.Movement;
import com.bank.domain.model.MovementType;
import com.bank.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** JSON de un movimiento: una línea del estado de cuenta. */
public record MovementResponse(
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

    public static MovementResponse from(Movement movement) {
        return new MovementResponse(
                movement.id(),
                movement.reference(),
                movement.accountId(),
                movement.transactionType(),
                movement.movementType(),
                movement.amount(),
                movement.balanceAfter(),
                movement.counterpartyAccountId(),
                movement.createdAt());
    }
}

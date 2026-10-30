package com.bank.infrastructure.adapter.out.persistence.mapper;

import com.bank.domain.model.Movement;
import com.bank.infrastructure.adapter.out.persistence.entity.MovementEntity;

/** Traduce entre el movimiento del dominio y la fila de la tabla movements. */
public final class MovementPersistenceMapper {

    private MovementPersistenceMapper() {
    }

    public static MovementEntity toEntity(Movement movement) {
        return new MovementEntity(
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

    public static Movement toDomain(MovementEntity entity) {
        return new Movement(
                entity.getId(),
                entity.getReference(),
                entity.getAccountId(),
                entity.getTransactionType(),
                entity.getMovementType(),
                entity.getAmount(),
                entity.getBalanceAfter(),
                entity.getCounterpartyAccountId(),
                entity.getCreatedAt());
    }
}

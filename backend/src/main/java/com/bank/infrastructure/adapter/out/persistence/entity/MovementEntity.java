package com.bank.infrastructure.adapter.out.persistence.entity;

import com.bank.domain.model.MovementType;
import com.bank.domain.model.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Representa una fila de la tabla movements. Todas las columnas son de solo inserción. */
@Entity
@Table(name = "movements")
public class MovementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private UUID reference;

    @Column(name = "account_id", nullable = false, updatable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, updatable = false, length = 10)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, updatable = false, length = 6)
    private MovementType movementType;

    @Column(nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "counterparty_account_id", updatable = false)
    private Long counterpartyAccountId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected MovementEntity() {
    }

    public MovementEntity(Long id, UUID reference, Long accountId, TransactionType transactionType,
                          MovementType movementType, BigDecimal amount, BigDecimal balanceAfter,
                          Long counterpartyAccountId, LocalDateTime createdAt) {
        this.id = id;
        this.reference = reference;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.movementType = movementType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.counterpartyAccountId = counterpartyAccountId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public UUID getReference() {
        return reference;
    }

    public Long getAccountId() {
        return accountId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public Long getCounterpartyAccountId() {
        return counterpartyAccountId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

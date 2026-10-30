package com.bank.infrastructure.adapter.out.persistence.entity;

import com.bank.domain.model.AccountStatus;
import com.bank.domain.model.AccountType;
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

/** Representa una fila de la tabla accounts. Las reglas viven en el modelo de dominio Account. */
@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 10)
    private AccountType type;

    @Column(name = "account_number", nullable = false, length = 10, unique = true, updatable = false)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AccountStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "available_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance;

    @Column(name = "gmf_exempt", nullable = false)
    private boolean gmfExempt;

    // Se guarda solo el id del cliente: la cuenta no necesita cargar todos sus datos para aplicar sus reglas.
    @Column(name = "client_id", nullable = false, updatable = false)
    private Long clientId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected AccountEntity() {
    }

    public AccountEntity(Long id, AccountType type, String number, AccountStatus status, BigDecimal balance,
                         BigDecimal availableBalance, boolean gmfExempt, Long clientId,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.type = type;
        this.number = number;
        this.status = status;
        this.balance = balance;
        this.availableBalance = availableBalance;
        this.gmfExempt = gmfExempt;
        this.clientId = clientId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public AccountType getType() {
        return type;
    }

    public String getNumber() {
        return number;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public boolean isGmfExempt() {
        return gmfExempt;
    }

    public Long getClientId() {
        return clientId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

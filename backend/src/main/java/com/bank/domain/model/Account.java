package com.bank.domain.model;

import com.bank.domain.exception.AccountBalanceNotZeroException;
import com.bank.domain.exception.AccountNotActiveException;
import com.bank.domain.exception.CancelledAccountException;
import com.bank.domain.exception.InsufficientFundsException;
import com.bank.domain.exception.InvalidAmountException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Producto financiero (cuenta de ahorros o corriente). Es Java puro y protege sus reglas:
 * siempre pertenece a un cliente, nace activa con saldo 0 y solo se cancela con saldo 0.
 */
public class Account {

    private final Long id;
    private final AccountType type;
    private final AccountNumber number;
    private AccountStatus status;
    private BigDecimal balance;
    // Sin retenciones ni bloqueos definidos en el enunciado, el saldo disponible es igual al saldo.
    private BigDecimal availableBalance;
    private final boolean gmfExempt;
    private final Long clientId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Account(Long id, AccountType type, AccountNumber number, AccountStatus status,
                    BigDecimal balance, BigDecimal availableBalance, boolean gmfExempt, Long clientId,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.type = Objects.requireNonNull(type, "type");
        this.number = Objects.requireNonNull(number, "number");
        this.status = Objects.requireNonNull(status, "status");
        this.balance = Objects.requireNonNull(balance, "balance");
        this.availableBalance = Objects.requireNonNull(availableBalance, "availableBalance");
        this.gmfExempt = gmfExempt;
        this.clientId = Objects.requireNonNull(clientId, "clientId");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    /**
     * Abre una cuenta nueva. El PDF exige que la de ahorros nazca activa; por decisión del proyecto
     * la corriente también. El saldo inicial es 0: el dinero entra después con una consignación.
     */
    public static Account open(AccountType type, AccountNumber number, Long clientId, boolean gmfExempt,
                               LocalDateTime now) {
        if (!number.belongsTo(type)) {
            throw new IllegalArgumentException("El número " + number.value() + " no corresponde al tipo " + type);
        }
        return new Account(null, type, number, AccountStatus.ACTIVE, BigDecimal.ZERO, BigDecimal.ZERO,
                gmfExempt, clientId, now, now);
    }

    /** Reconstruye una cuenta que ya estaba guardada. */
    public static Account restore(Long id, AccountType type, AccountNumber number, AccountStatus status,
                                  BigDecimal balance, BigDecimal availableBalance, boolean gmfExempt,
                                  Long clientId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Account(id, type, number, status, balance, availableBalance, gmfExempt, clientId,
                createdAt, updatedAt);
    }

    public void activate(LocalDateTime now) {
        changeStatus(AccountStatus.ACTIVE, now);
    }

    public void deactivate(LocalDateTime now) {
        changeStatus(AccountStatus.INACTIVE, now);
    }

    public void cancel(LocalDateTime now) {
        // compareTo y no equals: para BigDecimal, 0 y 0.00 son distintos con equals.
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            throw new AccountBalanceNotZeroException(number, balance);
        }
        changeStatus(AccountStatus.CANCELLED, now);
    }

    /** Consignación: entra dinero a la cuenta. */
    public Movement deposit(BigDecimal amount, UUID reference, LocalDateTime now) {
        return credit(TransactionType.DEPOSIT, amount, null, reference, now);
    }

    /** Retiro: sale dinero de la cuenta. */
    public Movement withdraw(BigDecimal amount, UUID reference, LocalDateTime now) {
        return debit(TransactionType.WITHDRAWAL, amount, null, reference, now);
    }

    /** Lado que envía una transferencia: genera el débito. */
    public Movement sendTransfer(BigDecimal amount, Long targetAccountId, UUID reference, LocalDateTime now) {
        return debit(TransactionType.TRANSFER, amount, targetAccountId, reference, now);
    }

    /** Lado que recibe una transferencia: genera el crédito. */
    public Movement receiveTransfer(BigDecimal amount, Long sourceAccountId, UUID reference, LocalDateTime now) {
        return credit(TransactionType.TRANSFER, amount, sourceAccountId, reference, now);
    }

    // Cada cambio de saldo devuelve su movimiento: así el saldo nunca cambia sin dejar rastro.
    private Movement credit(TransactionType type, BigDecimal amount, Long counterpartyAccountId,
                            UUID reference, LocalDateTime now) {
        ensureCanMoveMoney(amount);
        applyNewBalance(balance.add(amount), now);
        return new Movement(null, reference, id, type, MovementType.CREDIT, amount, balance,
                counterpartyAccountId, now);
    }

    private Movement debit(TransactionType type, BigDecimal amount, Long counterpartyAccountId,
                           UUID reference, LocalDateTime now) {
        ensureCanMoveMoney(amount);
        BigDecimal newBalance = balance.subtract(amount);
        // El PDF lo exige para ahorros; por decisión del proyecto (sin sobregiro definido) aplica a ambos tipos.
        if (newBalance.signum() < 0) {
            throw new InsufficientFundsException(number, balance, amount);
        }
        applyNewBalance(newBalance, now);
        return new Movement(null, reference, id, type, MovementType.DEBIT, amount, balance,
                counterpartyAccountId, now);
    }

    /** Decisión del proyecto: solo las cuentas activas mueven dinero; inactivas y canceladas no. */
    private void ensureCanMoveMoney(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 2) {
            throw new InvalidAmountException();
        }
        if (status != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(number, status);
        }
    }

    private void applyNewBalance(BigDecimal newBalance, LocalDateTime now) {
        this.balance = newBalance;
        this.availableBalance = newBalance;
        this.updatedAt = now;
    }

    private void changeStatus(AccountStatus newStatus, LocalDateTime now) {
        ensureNotCancelled();
        this.status = newStatus;
        this.updatedAt = now;
    }

    /** Una cuenta cancelada queda cerrada definitivamente (decisión del proyecto: no se reabre). */
    private void ensureNotCancelled() {
        if (status == AccountStatus.CANCELLED) {
            throw new CancelledAccountException(number);
        }
    }

    public Long getId() {
        return id;
    }

    public AccountType getType() {
        return type;
    }

    public AccountNumber getNumber() {
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

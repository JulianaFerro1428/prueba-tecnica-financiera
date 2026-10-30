package com.bank.domain.model;

import com.bank.domain.exception.AccountNotActiveException;
import com.bank.domain.exception.InsufficientFundsException;
import com.bank.domain.exception.InvalidAmountException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountMovementsTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 30, 10, 0);
    private static final UUID REFERENCE = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void deposit_increasesBalanceAndReturnsCreditMovement() {
        Account account = account(1L, AccountStatus.ACTIVE, "0");

        Movement movement = account.deposit(new BigDecimal("100000"), REFERENCE, NOW);

        assertThat(account.getBalance()).isEqualByComparingTo("100000");
        assertThat(account.getAvailableBalance()).isEqualByComparingTo("100000");
        assertThat(account.getUpdatedAt()).isEqualTo(NOW);
        assertThat(movement.accountId()).isEqualTo(1L);
        assertThat(movement.transactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(movement.movementType()).isEqualTo(MovementType.CREDIT);
        assertThat(movement.amount()).isEqualByComparingTo("100000");
        assertThat(movement.balanceAfter()).isEqualByComparingTo("100000");
        assertThat(movement.reference()).isEqualTo(REFERENCE);
    }

    @Test
    void withdraw_withEnoughBalance_decreasesBalanceAndReturnsDebitMovement() {
        Account account = account(1L, AccountStatus.ACTIVE, "100000");

        Movement movement = account.withdraw(new BigDecimal("30000"), REFERENCE, NOW);

        assertThat(account.getBalance()).isEqualByComparingTo("70000");
        assertThat(movement.movementType()).isEqualTo(MovementType.DEBIT);
        assertThat(movement.transactionType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(movement.balanceAfter()).isEqualByComparingTo("70000");
    }

    @Test
    void withdraw_exactBalance_leavesZero() {
        Account account = account(1L, AccountStatus.ACTIVE, "50000");

        account.withdraw(new BigDecimal("50000"), REFERENCE, NOW);

        assertThat(account.getBalance()).isEqualByComparingTo("0");
    }

    @Test
    void withdraw_moreThanBalance_isRejectedAndBalanceDoesNotChange() {
        Account account = account(1L, AccountStatus.ACTIVE, "20000");

        assertThatThrownBy(() -> account.withdraw(new BigDecimal("20000.01"), REFERENCE, NOW))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(account.getBalance()).isEqualByComparingTo("20000");
    }

    @Test
    void movements_withInvalidAmount_areRejected() {
        Account account = account(1L, AccountStatus.ACTIVE, "100000");

        assertThatThrownBy(() -> account.deposit(BigDecimal.ZERO, REFERENCE, NOW))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> account.deposit(new BigDecimal("-5000"), REFERENCE, NOW))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> account.withdraw(new BigDecimal("10.001"), REFERENCE, NOW))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> account.deposit(null, REFERENCE, NOW))
                .isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void movements_onInactiveOrCancelledAccount_areRejected() {
        Account inactive = account(1L, AccountStatus.INACTIVE, "100000");
        Account cancelled = account(2L, AccountStatus.CANCELLED, "0");

        assertThatThrownBy(() -> inactive.deposit(new BigDecimal("1000"), REFERENCE, NOW))
                .isInstanceOf(AccountNotActiveException.class);
        assertThatThrownBy(() -> cancelled.deposit(new BigDecimal("1000"), REFERENCE, NOW))
                .isInstanceOf(AccountNotActiveException.class);
        assertThat(inactive.getBalance()).isEqualByComparingTo("100000");
    }

    @Test
    void transfer_producesDebitInSourceAndCreditInTargetWithSameReference() {
        Account source = account(1L, AccountStatus.ACTIVE, "100000");
        Account target = account(2L, AccountStatus.ACTIVE, "0");
        BigDecimal amount = new BigDecimal("50000");

        Movement debit = source.sendTransfer(amount, target.getId(), REFERENCE, NOW);
        Movement credit = target.receiveTransfer(amount, source.getId(), REFERENCE, NOW);

        assertThat(source.getBalance()).isEqualByComparingTo("50000");
        assertThat(target.getBalance()).isEqualByComparingTo("50000");
        assertThat(debit.movementType()).isEqualTo(MovementType.DEBIT);
        assertThat(debit.counterpartyAccountId()).isEqualTo(2L);
        assertThat(credit.movementType()).isEqualTo(MovementType.CREDIT);
        assertThat(credit.counterpartyAccountId()).isEqualTo(1L);
        assertThat(debit.transactionType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(credit.reference()).isEqualTo(debit.reference());
    }

    private static Account account(Long id, AccountStatus status, String balance) {
        String number = String.format("53%08d", id);
        BigDecimal amount = new BigDecimal(balance);
        return Account.restore(id, AccountType.SAVINGS, new AccountNumber(number), status,
                amount, amount, false, 7L, NOW.minusMonths(1), NOW.minusMonths(1));
    }
}

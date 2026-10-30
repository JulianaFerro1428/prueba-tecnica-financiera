package com.bank.application.service;

import com.bank.application.port.in.AccountStatement;
import com.bank.application.port.in.TransferCommand;
import com.bank.application.port.in.TransferResult;
import com.bank.application.port.out.AccountRepositoryPort;
import com.bank.application.port.out.MovementRepositoryPort;
import com.bank.domain.exception.AccountNotActiveException;
import com.bank.domain.exception.AccountNotFoundException;
import com.bank.domain.exception.InsufficientFundsException;
import com.bank.domain.exception.SameAccountTransferException;
import com.bank.domain.model.Account;
import com.bank.domain.model.AccountNumber;
import com.bank.domain.model.AccountStatus;
import com.bank.domain.model.AccountType;
import com.bank.domain.model.Movement;
import com.bank.domain.model.MovementType;
import com.bank.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 30, 10, 0);

    @Mock
    private AccountRepositoryPort accountRepository;
    @Mock
    private MovementRepositoryPort movementRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        transactionService = new TransactionService(accountRepository, movementRepository, fixedClock);
    }

    @Test
    void deposit_activeAccount_increasesBalanceAndRecordsCredit() {
        Account account = account(1L, "5300000001", AccountStatus.ACTIVE, "100000");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(movementRepository.save(any(Movement.class))).thenAnswer(call -> call.getArgument(0));

        Movement movement = transactionService.deposit(1L, new BigDecimal("50000"));

        assertThat(account.getBalance()).isEqualByComparingTo("150000");
        assertThat(movement.movementType()).isEqualTo(MovementType.CREDIT);
        assertThat(movement.balanceAfter()).isEqualByComparingTo("150000");
        verify(accountRepository).save(account);
    }

    @Test
    void withdraw_moreThanBalance_isRejectedAndNothingIsSaved() {
        Account account = account(1L, "5300000001", AccountStatus.ACTIVE, "30000");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> transactionService.withdraw(1L, new BigDecimal("50000")))
                .isInstanceOf(InsufficientFundsException.class);
        verify(accountRepository, never()).save(any());
        verifyNoInteractions(movementRepository);
    }

    @Test
    void deposit_unknownAccount_isRejected() {
        when(accountRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deposit(99L, BigDecimal.TEN))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void transfer_validAccounts_movesMoneyAndRecordsDebitAndCreditWithSameReference() {
        Account source = account(1L, "5300000001", AccountStatus.ACTIVE, "100000");
        Account target = account(2L, "3300000001", AccountStatus.ACTIVE, "0");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(target));
        when(movementRepository.save(any(Movement.class))).thenAnswer(call -> call.getArgument(0));

        TransferResult result = transactionService.transfer(new TransferCommand(1L, 2L, new BigDecimal("50000")));

        assertThat(source.getBalance()).isEqualByComparingTo("50000");
        assertThat(target.getBalance()).isEqualByComparingTo("50000");
        assertThat(result.debit().movementType()).isEqualTo(MovementType.DEBIT);
        assertThat(result.debit().counterpartyAccountId()).isEqualTo(2L);
        assertThat(result.credit().movementType()).isEqualTo(MovementType.CREDIT);
        assertThat(result.credit().counterpartyAccountId()).isEqualTo(1L);
        assertThat(result.debit().reference()).isNotNull().isEqualTo(result.credit().reference());
        assertThat(result.debit().transactionType()).isEqualTo(TransactionType.TRANSFER);
        verify(accountRepository).save(source);
        verify(accountRepository).save(target);
    }

    @Test
    void transfer_insufficientFunds_savesNothing() {
        Account source = account(1L, "5300000001", AccountStatus.ACTIVE, "20000");
        Account target = account(2L, "3300000001", AccountStatus.ACTIVE, "0");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> transactionService.transfer(new TransferCommand(1L, 2L, new BigDecimal("50000"))))
                .isInstanceOf(InsufficientFundsException.class);
        verify(accountRepository, never()).save(any());
        verifyNoInteractions(movementRepository);
    }

    @Test
    void transfer_inactiveTarget_savesNothingEvenThoughSourceWasValid() {
        Account source = account(1L, "5300000001", AccountStatus.ACTIVE, "100000");
        Account target = account(2L, "3300000001", AccountStatus.INACTIVE, "0");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> transactionService.transfer(new TransferCommand(1L, 2L, new BigDecimal("50000"))))
                .isInstanceOf(AccountNotActiveException.class);
        verify(accountRepository, never()).save(any());
        verifyNoInteractions(movementRepository);
    }

    @Test
    void transfer_toSameAccount_isRejectedBeforeLockingAnything() {
        assertThatThrownBy(() -> transactionService.transfer(new TransferCommand(1L, 1L, BigDecimal.TEN)))
                .isInstanceOf(SameAccountTransferException.class);
        verifyNoInteractions(accountRepository, movementRepository);
    }

    @Test
    void transfer_alwaysLocksLowerIdFirstToAvoidDeadlocks() {
        Account source = account(8L, "5300000008", AccountStatus.ACTIVE, "100000");
        Account target = account(3L, "3300000003", AccountStatus.ACTIVE, "0");
        when(accountRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(target));
        when(movementRepository.save(any(Movement.class))).thenAnswer(call -> call.getArgument(0));

        transactionService.transfer(new TransferCommand(8L, 3L, new BigDecimal("1000")));

        InOrder order = inOrder(accountRepository);
        order.verify(accountRepository).findByIdForUpdate(3L);
        order.verify(accountRepository).findByIdForUpdate(8L);
    }

    @Test
    void getStatement_returnsAccountWithItsMovements() {
        Account account = account(1L, "5300000001", AccountStatus.ACTIVE, "70000");
        Movement withdrawal = account(1L, "5300000001", AccountStatus.ACTIVE, "100000")
                .withdraw(new BigDecimal("30000"), UUID.randomUUID(), NOW);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(movementRepository.findByAccountId(1L)).thenReturn(List.of(withdrawal));

        AccountStatement statement = transactionService.getStatement(1L);

        assertThat(statement.account()).isSameAs(account);
        assertThat(statement.movements()).containsExactly(withdrawal);
    }

    private Account account(Long id, String number, AccountStatus status, String balance) {
        AccountType type = number.startsWith("53") ? AccountType.SAVINGS : AccountType.CHECKING;
        LocalDateTime createdAt = NOW.minusDays(10);
        return Account.restore(id, type, new AccountNumber(number), status, new BigDecimal(balance),
                new BigDecimal(balance), false, 7L, createdAt, createdAt);
    }
}

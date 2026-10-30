package com.bank.application.service;

import com.bank.application.port.in.OpenAccountCommand;
import com.bank.application.port.out.AccountNumberSequencePort;
import com.bank.application.port.out.AccountRepositoryPort;
import com.bank.application.port.out.ClientRepositoryPort;
import com.bank.domain.exception.AccountBalanceNotZeroException;
import com.bank.domain.exception.AccountNotFoundException;
import com.bank.domain.exception.ClientNotFoundException;
import com.bank.domain.exception.DuplicateAccountTypeException;
import com.bank.domain.model.Account;
import com.bank.domain.model.AccountNumber;
import com.bank.domain.model.AccountStatus;
import com.bank.domain.model.AccountType;
import com.bank.domain.model.Client;
import com.bank.domain.model.ClientDetails;
import com.bank.domain.model.IdentificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 30, 10, 0);

    @Mock
    private AccountRepositoryPort accountRepository;
    @Mock
    private AccountNumberSequencePort numberSequence;
    @Mock
    private ClientRepositoryPort clientRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        accountService = new AccountService(accountRepository, numberSequence, clientRepository, fixedClock);
    }

    @Test
    void open_existingClient_generatesNumberWithPrefixAndSavesActiveAccount() {
        when(clientRepository.findById(7L)).thenReturn(Optional.of(existingClient(7L)));
        when(numberSequence.nextSequence(AccountType.SAVINGS)).thenReturn(7L);
        when(accountRepository.save(any(Account.class))).thenAnswer(call -> call.getArgument(0));

        Account account = accountService.open(new OpenAccountCommand(7L, AccountType.SAVINGS, false));

        assertThat(account.getNumber().value()).isEqualTo("5300000007");
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getBalance()).isEqualByComparingTo("0");
        assertThat(account.getClientId()).isEqualTo(7L);
        assertThat(account.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void open_unknownClient_isRejectedWithoutUsingSequenceOrSaving() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.open(new OpenAccountCommand(99L, AccountType.CHECKING, false)))
                .isInstanceOf(ClientNotFoundException.class);
        verifyNoInteractions(numberSequence);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void open_clientWithInactiveAccountOfSameType_isRejectedWithoutUsingSequenceOrSaving() {
        when(clientRepository.findById(7L)).thenReturn(Optional.of(existingClient(7L)));
        when(accountRepository.findByClientId(7L))
                .thenReturn(List.of(storedAccount(AccountType.SAVINGS, AccountStatus.INACTIVE)));

        assertThatThrownBy(() -> accountService.open(new OpenAccountCommand(7L, AccountType.SAVINGS, false)))
                .isInstanceOf(DuplicateAccountTypeException.class)
                .hasMessageContaining("5300000001");
        verifyNoInteractions(numberSequence);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void open_clientWithAccountOfOtherType_opensIt() {
        when(clientRepository.findById(7L)).thenReturn(Optional.of(existingClient(7L)));
        when(accountRepository.findByClientId(7L))
                .thenReturn(List.of(storedAccount(AccountType.SAVINGS, AccountStatus.ACTIVE)));
        when(numberSequence.nextSequence(AccountType.CHECKING)).thenReturn(2L);
        when(accountRepository.save(any(Account.class))).thenAnswer(call -> call.getArgument(0));

        Account account = accountService.open(new OpenAccountCommand(7L, AccountType.CHECKING, false));

        assertThat(account.getNumber().value()).isEqualTo("3300000002");
    }

    @Test
    void open_clientWhoseOnlyAccountOfThatTypeIsCancelled_opensANewOne() {
        when(clientRepository.findById(7L)).thenReturn(Optional.of(existingClient(7L)));
        when(accountRepository.findByClientId(7L))
                .thenReturn(List.of(storedAccount(AccountType.SAVINGS, AccountStatus.CANCELLED)));
        when(numberSequence.nextSequence(AccountType.SAVINGS)).thenReturn(8L);
        when(accountRepository.save(any(Account.class))).thenAnswer(call -> call.getArgument(0));

        Account account = accountService.open(new OpenAccountCommand(7L, AccountType.SAVINGS, false));

        assertThat(account.getNumber().value()).isEqualTo("5300000008");
    }

    @Test
    void getById_unknownAccount_throwsNotFound() {
        when(accountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getById(5L))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void getByClientId_unknownClient_throwsClientNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getByClientId(99L))
                .isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    void deactivate_existingAccount_savesItAsInactive() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(storedAccount("0")));
        when(accountRepository.save(any(Account.class))).thenAnswer(call -> call.getArgument(0));

        Account account = accountService.deactivate(1L);

        assertThat(account.getStatus()).isEqualTo(AccountStatus.INACTIVE);
        assertThat(account.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void cancel_accountWithBalance_isRejectedAndNotSaved() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(storedAccount("50000.00")));

        assertThatThrownBy(() -> accountService.cancel(1L))
                .isInstanceOf(AccountBalanceNotZeroException.class);
        verify(accountRepository, never()).save(any());
    }

    private static Client existingClient(Long id) {
        ClientDetails details = new ClientDetails(IdentificationType.CC, "1012345678",
                "María", "Gómez", "maria@mail.com", LocalDate.of(1995, 5, 20));
        return Client.restore(id, details, NOW.minusYears(1), NOW.minusYears(1));
    }

    private static Account storedAccount(AccountType type, AccountStatus status) {
        LocalDateTime createdAt = NOW.minusMonths(1);
        return Account.restore(1L, type, new AccountNumber(type.numberPrefix() + "00000001"), status,
                BigDecimal.ZERO, BigDecimal.ZERO, false, 7L, createdAt, createdAt);
    }

    private static Account storedAccount(String balance) {
        LocalDateTime createdAt = NOW.minusMonths(1);
        return Account.restore(1L, AccountType.SAVINGS, new AccountNumber("5300000001"), AccountStatus.ACTIVE,
                new BigDecimal(balance), new BigDecimal(balance), false, 7L, createdAt, createdAt);
    }
}

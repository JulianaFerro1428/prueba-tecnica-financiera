package com.bank.application.service;

import com.bank.application.port.in.ChangeAccountStatusUseCase;
import com.bank.application.port.in.GetAccountUseCase;
import com.bank.application.port.in.OpenAccountCommand;
import com.bank.application.port.in.OpenAccountUseCase;
import com.bank.application.port.out.AccountNumberSequencePort;
import com.bank.application.port.out.AccountRepositoryPort;
import com.bank.application.port.out.ClientRepositoryPort;
import com.bank.domain.exception.AccountNotFoundException;
import com.bank.domain.exception.ClientNotFoundException;
import com.bank.domain.exception.DuplicateAccountTypeException;
import com.bank.domain.model.Account;
import com.bank.domain.model.AccountNumber;
import com.bank.domain.model.AccountType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;

/** Casos de uso de cuentas: abrir, consultar y cambiar su estado. */
@Service
@Transactional
public class AccountService implements OpenAccountUseCase, GetAccountUseCase, ChangeAccountStatusUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountNumberSequencePort numberSequence;
    private final ClientRepositoryPort clientRepository;
    private final Clock clock;

    public AccountService(AccountRepositoryPort accountRepository, AccountNumberSequencePort numberSequence,
                          ClientRepositoryPort clientRepository, Clock clock) {
        this.accountRepository = accountRepository;
        this.numberSequence = numberSequence;
        this.clientRepository = clientRepository;
        this.clock = clock;
    }

    @Override
    public Account open(OpenAccountCommand command) {
        // El cliente se valida antes de pedir el consecutivo, para no gastar números en cuentas que no se abren.
        ensureClientExists(command.clientId());
        ensureNoOpenAccountOfType(command.clientId(), command.type());
        AccountNumber number = AccountNumber.generate(command.type(), numberSequence.nextSequence(command.type()));
        Account account = Account.open(command.type(), number, command.clientId(), command.gmfExempt(), now());
        return accountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Account getById(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAll() {
        return accountRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getByClientId(Long clientId) {
        ensureClientExists(clientId);
        return accountRepository.findByClientId(clientId);
    }

    @Override
    public Account activate(Long id) {
        return changeStatus(id, Account::activate);
    }

    @Override
    public Account deactivate(Long id) {
        return changeStatus(id, Account::deactivate);
    }

    @Override
    public Account cancel(Long id) {
        return changeStatus(id, Account::cancel);
    }

    /** Los tres cambios de estado siguen los mismos pasos: buscar, aplicar la regla del dominio y guardar. */
    private Account changeStatus(Long id, BiConsumer<Account, LocalDateTime> statusChange) {
        Account account = getById(id);
        statusChange.accept(account, now());
        return accountRepository.save(account);
    }

    private void ensureClientExists(Long clientId) {
        if (clientRepository.findById(clientId).isEmpty()) {
            throw new ClientNotFoundException(clientId);
        }
    }

    /**
     * Decisión del proyecto (el PDF no fija un límite): una cuenta de cada tipo por cliente.
     * Las inactivas cuentan porque se pueden reactivar; las canceladas no, porque quedan cerradas para siempre.
     */
    private void ensureNoOpenAccountOfType(Long clientId, AccountType type) {
        accountRepository.findByClientId(clientId).stream()
                .filter(account -> account.getType() == type && !account.isCancelled())
                .findFirst()
                .ifPresent(existing -> {
                    throw new DuplicateAccountTypeException(type, existing.getNumber());
                });
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}

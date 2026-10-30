package com.bank.application.service;

import com.bank.application.port.in.AccountStatement;
import com.bank.application.port.in.DepositUseCase;
import com.bank.application.port.in.GetAccountStatementUseCase;
import com.bank.application.port.in.TransferCommand;
import com.bank.application.port.in.TransferResult;
import com.bank.application.port.in.TransferUseCase;
import com.bank.application.port.in.WithdrawUseCase;
import com.bank.application.port.out.AccountRepositoryPort;
import com.bank.application.port.out.MovementRepositoryPort;
import com.bank.domain.exception.AccountNotFoundException;
import com.bank.domain.exception.SameAccountTransferException;
import com.bank.domain.model.Account;
import com.bank.domain.model.Movement;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Casos de uso que mueven dinero. Cada método es una transacción de base de datos:
 * si algo falla a mitad de camino, PostgreSQL deshace todo lo que se alcanzó a escribir.
 */
@Service
@Transactional
public class TransactionService implements DepositUseCase, WithdrawUseCase, TransferUseCase,
        GetAccountStatementUseCase {

    private final AccountRepositoryPort accountRepository;
    private final MovementRepositoryPort movementRepository;
    private final Clock clock;

    public TransactionService(AccountRepositoryPort accountRepository, MovementRepositoryPort movementRepository,
                              Clock clock) {
        this.accountRepository = accountRepository;
        this.movementRepository = movementRepository;
        this.clock = clock;
    }

    @Override
    public Movement deposit(Long accountId, BigDecimal amount) {
        Account account = lockAccount(accountId);
        Movement movement = account.deposit(amount, UUID.randomUUID(), now());
        accountRepository.save(account);
        return movementRepository.save(movement);
    }

    @Override
    public Movement withdraw(Long accountId, BigDecimal amount) {
        Account account = lockAccount(accountId);
        Movement movement = account.withdraw(amount, UUID.randomUUID(), now());
        accountRepository.save(account);
        return movementRepository.save(movement);
    }

    @Override
    public TransferResult transfer(TransferCommand command) {
        Long sourceId = command.sourceAccountId();
        Long targetId = command.targetAccountId();
        if (sourceId.equals(targetId)) {
            throw new SameAccountTransferException();
        }

        // Siempre se bloquea primero el id menor. Si dos transferencias cruzadas (A→B y B→A) bloquearan
        // en distinto orden, cada una esperaría a la otra para siempre (deadlock).
        Account source;
        Account target;
        if (sourceId < targetId) {
            source = lockAccount(sourceId);
            target = lockAccount(targetId);
        } else {
            target = lockAccount(targetId);
            source = lockAccount(sourceId);
        }

        // Ambas reglas se validan en memoria antes de guardar: si el destino no puede recibir,
        // no se alcanza a escribir nada del origen.
        UUID reference = UUID.randomUUID();
        LocalDateTime now = now();
        Movement debit = source.sendTransfer(command.amount(), targetId, reference, now);
        Movement credit = target.receiveTransfer(command.amount(), sourceId, reference, now);

        accountRepository.save(source);
        accountRepository.save(target);
        return new TransferResult(movementRepository.save(debit), movementRepository.save(credit));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountStatement getStatement(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return new AccountStatement(account, movementRepository.findByAccountId(accountId));
    }

    private Account lockAccount(Long id) {
        return accountRepository.findByIdForUpdate(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}

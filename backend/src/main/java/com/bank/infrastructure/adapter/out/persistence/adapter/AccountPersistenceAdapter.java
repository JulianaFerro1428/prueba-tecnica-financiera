package com.bank.infrastructure.adapter.out.persistence.adapter;

import com.bank.application.port.out.AccountRepositoryPort;
import com.bank.domain.model.Account;
import com.bank.infrastructure.adapter.out.persistence.mapper.AccountPersistenceMapper;
import com.bank.infrastructure.adapter.out.persistence.repository.SpringDataAccountRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Cumple el puerto AccountRepositoryPort usando Spring Data JPA y PostgreSQL. */
@Component
public class AccountPersistenceAdapter implements AccountRepositoryPort {

    private final SpringDataAccountRepository repository;

    public AccountPersistenceAdapter(SpringDataAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Account save(Account account) {
        return AccountPersistenceMapper.toDomain(repository.save(AccountPersistenceMapper.toEntity(account)));
    }

    @Override
    public Optional<Account> findById(Long id) {
        return repository.findById(id).map(AccountPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Account> findByIdForUpdate(Long id) {
        return repository.findByIdForUpdate(id).map(AccountPersistenceMapper::toDomain);
    }

    @Override
    public List<Account> findAll() {
        return repository.findAll().stream().map(AccountPersistenceMapper::toDomain).toList();
    }

    @Override
    public List<Account> findByClientId(Long clientId) {
        return repository.findByClientIdOrderByIdAsc(clientId).stream()
                .map(AccountPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByClientId(Long clientId) {
        return repository.existsByClientId(clientId);
    }
}

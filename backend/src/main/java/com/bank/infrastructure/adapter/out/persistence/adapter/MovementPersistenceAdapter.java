package com.bank.infrastructure.adapter.out.persistence.adapter;

import com.bank.application.port.out.MovementRepositoryPort;
import com.bank.domain.model.Movement;
import com.bank.infrastructure.adapter.out.persistence.mapper.MovementPersistenceMapper;
import com.bank.infrastructure.adapter.out.persistence.repository.SpringDataMovementRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/** Cumple el puerto MovementRepositoryPort usando Spring Data JPA y PostgreSQL. */
@Component
public class MovementPersistenceAdapter implements MovementRepositoryPort {

    private final SpringDataMovementRepository repository;

    public MovementPersistenceAdapter(SpringDataMovementRepository repository) {
        this.repository = repository;
    }

    @Override
    public Movement save(Movement movement) {
        return MovementPersistenceMapper.toDomain(repository.save(MovementPersistenceMapper.toEntity(movement)));
    }

    @Override
    public List<Movement> findByAccountId(Long accountId) {
        return repository.findByAccountIdOrderByCreatedAtDescIdDesc(accountId).stream()
                .map(MovementPersistenceMapper::toDomain)
                .toList();
    }
}

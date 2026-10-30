package com.bank.infrastructure.adapter.out.persistence.adapter;

import com.bank.application.port.out.ClientRepositoryPort;
import com.bank.domain.model.Client;
import com.bank.domain.model.IdentificationType;
import com.bank.infrastructure.adapter.out.persistence.mapper.ClientPersistenceMapper;
import com.bank.infrastructure.adapter.out.persistence.repository.SpringDataClientRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Cumple el puerto ClientRepositoryPort usando Spring Data JPA y PostgreSQL. */
@Component
public class ClientPersistenceAdapter implements ClientRepositoryPort {

    private final SpringDataClientRepository repository;

    public ClientPersistenceAdapter(SpringDataClientRepository repository) {
        this.repository = repository;
    }

    @Override
    public Client save(Client client) {
        return ClientPersistenceMapper.toDomain(repository.save(ClientPersistenceMapper.toEntity(client)));
    }

    @Override
    public Optional<Client> findById(Long id) {
        return repository.findById(id).map(ClientPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Client> findByIdentification(IdentificationType type, String number) {
        return repository.findByIdentificationTypeAndIdentificationNumber(type, number)
                .map(ClientPersistenceMapper::toDomain);
    }

    @Override
    public List<Client> findAll() {
        return repository.findAll().stream().map(ClientPersistenceMapper::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

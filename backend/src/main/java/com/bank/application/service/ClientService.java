package com.bank.application.service;

import com.bank.application.port.in.CreateClientUseCase;
import com.bank.application.port.in.DeleteClientUseCase;
import com.bank.application.port.in.GetClientUseCase;
import com.bank.application.port.in.UpdateClientUseCase;
import com.bank.application.port.out.ClientRepositoryPort;
import com.bank.domain.exception.ClientNotFoundException;
import com.bank.domain.exception.DuplicateIdentificationException;
import com.bank.domain.model.Client;
import com.bank.domain.model.ClientDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Casos de uso de clientes: coordina el dominio (reglas del cliente) con el puerto de persistencia.
 * Depende de la interfaz ClientRepositoryPort, nunca de JPA directamente.
 */
@Service
@Transactional
public class ClientService implements CreateClientUseCase, GetClientUseCase, UpdateClientUseCase, DeleteClientUseCase {

    private final ClientRepositoryPort clientRepository;
    private final Clock clock;

    public ClientService(ClientRepositoryPort clientRepository, Clock clock) {
        this.clientRepository = clientRepository;
        this.clock = clock;
    }

    @Override
    public Client create(ClientDetails details) {
        ensureIdentificationIsFree(details, null);
        return clientRepository.save(Client.create(details, now()));
    }

    @Override
    @Transactional(readOnly = true)
    public Client getById(Long id) {
        return clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    @Override
    public Client update(Long id, ClientDetails details) {
        Client client = getById(id);
        ensureIdentificationIsFree(details, id);
        client.update(details, now());
        return clientRepository.save(client);
    }

    @Override
    public void delete(Long id) {
        getById(id);
        clientRepository.deleteById(id);
    }

    /** El documento puede repetirse solo si pertenece al mismo cliente que se está actualizando. */
    private void ensureIdentificationIsFree(ClientDetails details, Long currentClientId) {
        clientRepository.findByIdentification(details.identificationType(), details.identificationNumber())
                .filter(existing -> !existing.getId().equals(currentClientId))
                .ifPresent(existing -> {
                    throw new DuplicateIdentificationException(
                            details.identificationType(), details.identificationNumber());
                });
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}

package com.bank.application.port.out;

import com.bank.domain.model.Client;
import com.bank.domain.model.IdentificationType;

import java.util.List;
import java.util.Optional;

/**
 * Lo que la aplicación necesita para guardar y buscar clientes,
 * sin saber si detrás hay PostgreSQL, otra base de datos o una lista en memoria.
 */
public interface ClientRepositoryPort {

    Client save(Client client);

    Optional<Client> findById(Long id);

    Optional<Client> findByIdentification(IdentificationType type, String number);

    List<Client> findAll();

    void deleteById(Long id);
}

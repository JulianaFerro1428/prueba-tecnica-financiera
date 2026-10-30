package com.bank.infrastructure.adapter.out.persistence.mapper;

import com.bank.domain.model.Client;
import com.bank.domain.model.ClientDetails;
import com.bank.infrastructure.adapter.out.persistence.entity.ClientEntity;

/** Traduce entre el cliente del dominio y la fila de la tabla clients. */
public final class ClientPersistenceMapper {

    private ClientPersistenceMapper() {
    }

    public static ClientEntity toEntity(Client client) {
        ClientDetails details = client.getDetails();
        return new ClientEntity(
                client.getId(),
                details.identificationType(),
                details.identificationNumber(),
                details.firstNames(),
                details.lastName(),
                details.email(),
                details.birthDate(),
                client.getCreatedAt(),
                client.getUpdatedAt());
    }

    public static Client toDomain(ClientEntity entity) {
        ClientDetails details = new ClientDetails(
                entity.getIdentificationType(),
                entity.getIdentificationNumber(),
                entity.getFirstNames(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getBirthDate());
        return Client.restore(entity.getId(), details, entity.getCreatedAt(), entity.getUpdatedAt());
    }
}

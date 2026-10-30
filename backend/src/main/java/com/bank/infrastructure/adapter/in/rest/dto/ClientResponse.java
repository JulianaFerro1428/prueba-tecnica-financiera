package com.bank.infrastructure.adapter.in.rest.dto;

import com.bank.domain.model.Client;
import com.bank.domain.model.ClientDetails;
import com.bank.domain.model.IdentificationType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** JSON que el backend devuelve al frontend con los datos de un cliente. */
public record ClientResponse(
        Long id,
        IdentificationType identificationType,
        String identificationNumber,
        String firstNames,
        String lastName,
        String email,
        LocalDate birthDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ClientResponse from(Client client) {
        ClientDetails details = client.getDetails();
        return new ClientResponse(
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
}

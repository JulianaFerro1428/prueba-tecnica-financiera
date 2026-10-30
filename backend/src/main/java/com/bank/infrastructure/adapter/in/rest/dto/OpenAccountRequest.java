package com.bank.infrastructure.adapter.in.rest.dto;

import com.bank.application.port.in.OpenAccountCommand;
import com.bank.domain.model.AccountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * JSON para abrir una cuenta. No tiene número, estado ni saldo:
 * esos datos los decide el sistema, así que no hay forma de enviarlos.
 */
public record OpenAccountRequest(
        @NotNull(message = "El cliente es obligatorio")
        @Positive(message = "El id del cliente debe ser positivo")
        Long clientId,

        @NotNull(message = "El tipo de cuenta es obligatorio (SAVINGS o CHECKING)")
        AccountType type,

        @NotNull(message = "Debe indicar si la cuenta está exenta de GMF")
        Boolean gmfExempt
) {

    public OpenAccountCommand toCommand() {
        return new OpenAccountCommand(clientId, type, gmfExempt);
    }
}

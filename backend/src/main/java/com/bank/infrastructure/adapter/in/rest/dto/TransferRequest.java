package com.bank.infrastructure.adapter.in.rest.dto;

import com.bank.application.port.in.TransferCommand;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** JSON para transferir dinero entre dos cuentas existentes. */
public record TransferRequest(
        @NotNull(message = "La cuenta de origen es obligatoria")
        Long sourceAccountId,

        @NotNull(message = "La cuenta de destino es obligatoria")
        Long targetAccountId,

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor que $0")
        @Digits(integer = 17, fraction = 2, message = "El monto admite máximo 2 decimales")
        BigDecimal amount
) {

    public TransferCommand toCommand() {
        return new TransferCommand(sourceAccountId, targetAccountId, amount);
    }
}

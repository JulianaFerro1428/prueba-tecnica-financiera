package com.bank.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** JSON para consignar o retirar: solo el monto. La cuenta viene en la URL. */
public record AmountRequest(
        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor que $0")
        @Digits(integer = 17, fraction = 2, message = "El monto admite máximo 2 decimales")
        BigDecimal amount
) {
}

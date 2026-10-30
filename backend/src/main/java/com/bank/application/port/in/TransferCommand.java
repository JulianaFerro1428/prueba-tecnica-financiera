package com.bank.application.port.in;

import java.math.BigDecimal;

/** Datos que pide una transferencia: de qué cuenta sale, a cuál llega y cuánto. */
public record TransferCommand(Long sourceAccountId, Long targetAccountId, BigDecimal amount) {
}

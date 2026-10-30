package com.bank.application.port.out;

import com.bank.domain.model.AccountType;

/**
 * Entrega el siguiente consecutivo único para un tipo de cuenta.
 * El formato del número (prefijo + 8 dígitos) lo arma el dominio con AccountNumber.generate.
 */
public interface AccountNumberSequencePort {

    long nextSequence(AccountType type);
}

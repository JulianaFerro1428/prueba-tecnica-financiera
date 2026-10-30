package com.bank.domain.exception;

import com.bank.domain.model.AccountNumber;

import java.math.BigDecimal;

public class InsufficientFundsException extends DomainException {

    public InsufficientFundsException(AccountNumber number, BigDecimal balance, BigDecimal amount) {
        super("Saldo insuficiente en la cuenta " + number.value() + ". Saldo: $" + balance.toPlainString()
                + ", monto solicitado: $" + amount.toPlainString());
    }
}

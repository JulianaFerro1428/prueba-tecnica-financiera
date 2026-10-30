package com.bank.domain.exception;

import com.bank.domain.model.AccountNumber;

import java.math.BigDecimal;

public class AccountBalanceNotZeroException extends DomainException {

    public AccountBalanceNotZeroException(AccountNumber number, BigDecimal balance) {
        super("La cuenta " + number.value() + " solo se puede cancelar con saldo $0. Saldo actual: $"
                + balance.toPlainString());
    }
}

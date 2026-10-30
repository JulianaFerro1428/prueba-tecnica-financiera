package com.bank.domain.exception;

import com.bank.domain.model.AccountNumber;
import com.bank.domain.model.AccountStatus;

public class AccountNotActiveException extends DomainException {

    public AccountNotActiveException(AccountNumber number, AccountStatus status) {
        super("La cuenta " + number.value() + " está " + (status == AccountStatus.CANCELLED ? "cancelada" : "inactiva")
                + " y no admite movimientos");
    }
}

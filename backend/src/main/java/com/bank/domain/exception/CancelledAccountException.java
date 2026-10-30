package com.bank.domain.exception;

import com.bank.domain.model.AccountNumber;

public class CancelledAccountException extends DomainException {

    public CancelledAccountException(AccountNumber number) {
        super("La cuenta " + number.value() + " está cancelada y no admite cambios");
    }
}

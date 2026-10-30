package com.bank.domain.exception;

import com.bank.domain.model.AccountNumber;
import com.bank.domain.model.AccountType;

/** Decisión del proyecto: un cliente tiene como máximo una cuenta de cada tipo que no esté cancelada. */
public class DuplicateAccountTypeException extends DomainException {

    public DuplicateAccountTypeException(AccountType type, AccountNumber existingAccount) {
        super("El cliente ya tiene la cuenta " + (type == AccountType.SAVINGS ? "de ahorros " : "corriente ")
                + existingAccount.value() + ". Solo puede tener una de cada tipo mientras no esté cancelada");
    }
}

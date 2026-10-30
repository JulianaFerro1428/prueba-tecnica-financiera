package com.bank.domain.exception;

import com.bank.domain.model.IdentificationType;

public class DuplicateIdentificationException extends DomainException {

    public DuplicateIdentificationException(IdentificationType type, String number) {
        super("Ya existe un cliente con el documento " + type + " " + number);
    }
}

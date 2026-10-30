package com.bank.domain.exception;

public class UnderageClientException extends DomainException {

    public UnderageClientException() {
        super("El cliente debe ser mayor de edad");
    }
}

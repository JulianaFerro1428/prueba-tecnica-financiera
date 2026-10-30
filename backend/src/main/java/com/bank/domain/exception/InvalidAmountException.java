package com.bank.domain.exception;

public class InvalidAmountException extends DomainException {

    public InvalidAmountException() {
        super("El monto debe ser mayor que $0 y tener máximo 2 decimales");
    }
}

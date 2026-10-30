package com.bank.domain.exception;

/** Base de los errores "no existe" (cliente, cuenta...), para responder siempre igual. */
public abstract class NotFoundException extends DomainException {

    protected NotFoundException(String message) {
        super(message);
    }
}

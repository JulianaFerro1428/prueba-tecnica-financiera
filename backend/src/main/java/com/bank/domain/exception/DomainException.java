package com.bank.domain.exception;

/**
 * Base de las reglas de negocio incumplidas (por ejemplo, un cliente menor de edad).
 * Permite al adaptador REST tratarlas todas igual sin conocer cada una.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}

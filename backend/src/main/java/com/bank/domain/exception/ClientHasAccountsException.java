package com.bank.domain.exception;

public class ClientHasAccountsException extends DomainException {

    public ClientHasAccountsException(Long clientId) {
        super("El cliente " + clientId + " tiene productos vinculados y no se puede eliminar");
    }
}

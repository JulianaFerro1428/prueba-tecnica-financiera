package com.bank.domain.exception;

public class ClientNotFoundException extends NotFoundException {

    public ClientNotFoundException(Long id) {
        super("No existe un cliente con id " + id);
    }
}

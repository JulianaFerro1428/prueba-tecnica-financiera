package com.bank.domain.exception;

public class AccountNotFoundException extends NotFoundException {

    public AccountNotFoundException(Long id) {
        super("No existe una cuenta con id " + id);
    }
}

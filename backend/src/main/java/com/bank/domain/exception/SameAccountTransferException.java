package com.bank.domain.exception;

public class SameAccountTransferException extends DomainException {

    public SameAccountTransferException() {
        super("La cuenta de origen y la de destino deben ser diferentes");
    }
}

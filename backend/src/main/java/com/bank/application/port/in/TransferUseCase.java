package com.bank.application.port.in;

public interface TransferUseCase {

    TransferResult transfer(TransferCommand command);
}

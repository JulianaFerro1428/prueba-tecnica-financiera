package com.bank.application.port.in;

public interface GetAccountStatementUseCase {

    AccountStatement getStatement(Long accountId);
}

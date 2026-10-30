package com.bank.infrastructure.adapter.in.rest.dto;

import com.bank.application.port.in.AccountStatement;

import java.util.List;

/** JSON del estado de cuenta: la cuenta con su saldo y la lista de movimientos. */
public record AccountStatementResponse(AccountResponse account, List<MovementResponse> movements) {

    public static AccountStatementResponse from(AccountStatement statement) {
        return new AccountStatementResponse(
                AccountResponse.from(statement.account()),
                statement.movements().stream().map(MovementResponse::from).toList());
    }
}

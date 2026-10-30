package com.bank.infrastructure.adapter.in.rest.controller;

import com.bank.application.port.in.DepositUseCase;
import com.bank.application.port.in.GetAccountStatementUseCase;
import com.bank.application.port.in.TransferUseCase;
import com.bank.application.port.in.WithdrawUseCase;
import com.bank.infrastructure.adapter.in.rest.dto.AccountStatementResponse;
import com.bank.infrastructure.adapter.in.rest.dto.AmountRequest;
import com.bank.infrastructure.adapter.in.rest.dto.MovementResponse;
import com.bank.infrastructure.adapter.in.rest.dto.TransferRequest;
import com.bank.infrastructure.adapter.in.rest.dto.TransferResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada HTTP para mover dinero y consultar el estado de cuenta.
 * Cada operación crea movimientos nuevos (201); no hay endpoints para editarlos ni borrarlos.
 */
@RestController
@RequestMapping("/api")
public class TransactionController {

    private final DepositUseCase deposit;
    private final WithdrawUseCase withdraw;
    private final TransferUseCase transfer;
    private final GetAccountStatementUseCase getStatement;

    public TransactionController(DepositUseCase deposit, WithdrawUseCase withdraw, TransferUseCase transfer,
                                 GetAccountStatementUseCase getStatement) {
        this.deposit = deposit;
        this.withdraw = withdraw;
        this.transfer = transfer;
        this.getStatement = getStatement;
    }

    @PostMapping("/accounts/{id}/deposits")
    @ResponseStatus(HttpStatus.CREATED)
    public MovementResponse deposit(@PathVariable Long id, @Valid @RequestBody AmountRequest request) {
        return MovementResponse.from(deposit.deposit(id, request.amount()));
    }

    @PostMapping("/accounts/{id}/withdrawals")
    @ResponseStatus(HttpStatus.CREATED)
    public MovementResponse withdraw(@PathVariable Long id, @Valid @RequestBody AmountRequest request) {
        return MovementResponse.from(withdraw.withdraw(id, request.amount()));
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        return TransferResponse.from(transfer.transfer(request.toCommand()));
    }

    @GetMapping("/accounts/{id}/statement")
    public AccountStatementResponse statement(@PathVariable Long id) {
        return AccountStatementResponse.from(getStatement.getStatement(id));
    }
}

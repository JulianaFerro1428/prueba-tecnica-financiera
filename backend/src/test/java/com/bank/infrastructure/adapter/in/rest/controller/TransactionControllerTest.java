package com.bank.infrastructure.adapter.in.rest.controller;

import com.bank.application.port.in.AccountStatement;
import com.bank.application.port.in.DepositUseCase;
import com.bank.application.port.in.GetAccountStatementUseCase;
import com.bank.application.port.in.TransferCommand;
import com.bank.application.port.in.TransferResult;
import com.bank.application.port.in.TransferUseCase;
import com.bank.application.port.in.WithdrawUseCase;
import com.bank.domain.exception.InsufficientFundsException;
import com.bank.domain.model.Account;
import com.bank.domain.model.AccountNumber;
import com.bank.domain.model.AccountStatus;
import com.bank.domain.model.AccountType;
import com.bank.domain.model.Movement;
import com.bank.domain.model.MovementType;
import com.bank.domain.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 30, 10, 0);
    private static final UUID REFERENCE = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepositUseCase deposit;
    @MockitoBean
    private WithdrawUseCase withdraw;
    @MockitoBean
    private TransferUseCase transfer;
    @MockitoBean
    private GetAccountStatementUseCase getStatement;

    @Test
    void deposit_validAmount_returns201WithNewBalance() throws Exception {
        when(deposit.deposit(1L, new BigDecimal("50000")))
                .thenReturn(movement(1L, TransactionType.DEPOSIT, MovementType.CREDIT, "150000", null));

        mockMvc.perform(post("/api/accounts/1/deposits").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":50000}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movementType").value("CREDIT"))
                .andExpect(jsonPath("$.balanceAfter").value(150000));
    }

    @Test
    void deposit_negativeOrTooManyDecimals_returns400AndDoesNotCallUseCase() throws Exception {
        mockMvc.perform(post("/api/accounts/1/deposits").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
        mockMvc.perform(post("/api/accounts/1/deposits").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":10.555}"))
                .andExpect(status().isBadRequest());

        verify(deposit, never()).deposit(anyLong(), any());
    }

    @Test
    void withdraw_insufficientFunds_returns422WithMessage() throws Exception {
        when(withdraw.withdraw(1L, new BigDecimal("50000"))).thenThrow(new InsufficientFundsException(
                new AccountNumber("5300000001"), new BigDecimal("30000"), new BigDecimal("50000")));

        mockMvc.perform(post("/api/accounts/1/withdrawals").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":50000}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void transfer_validRequest_returns201WithDebitAndCredit() throws Exception {
        when(transfer.transfer(new TransferCommand(1L, 2L, new BigDecimal("50000")))).thenReturn(new TransferResult(
                movement(1L, TransactionType.TRANSFER, MovementType.DEBIT, "50000", 2L),
                movement(2L, TransactionType.TRANSFER, MovementType.CREDIT, "50000", 1L)));

        mockMvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":1,\"targetAccountId\":2,\"amount\":50000}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value(REFERENCE.toString()))
                .andExpect(jsonPath("$.debit.accountId").value(1))
                .andExpect(jsonPath("$.credit.accountId").value(2));
    }

    @Test
    void transfer_missingTarget_returns400() throws Exception {
        mockMvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":1,\"amount\":50000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.targetAccountId").exists());

        verify(transfer, never()).transfer(any());
    }

    @Test
    void statement_returnsAccountAndMovements() throws Exception {
        Account account = Account.restore(1L, AccountType.SAVINGS, new AccountNumber("5300000001"),
                AccountStatus.ACTIVE, new BigDecimal("70000"), new BigDecimal("70000"), false, 7L, NOW, NOW);
        when(getStatement.getStatement(1L)).thenReturn(new AccountStatement(account,
                List.of(movement(1L, TransactionType.WITHDRAWAL, MovementType.DEBIT, "70000", null))));

        mockMvc.perform(get("/api/accounts/1/statement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.number").value("5300000001"))
                .andExpect(jsonPath("$.account.balance").value(70000))
                .andExpect(jsonPath("$.movements[0].transactionType").value("WITHDRAWAL"));
    }

    private Movement movement(Long accountId, TransactionType transactionType, MovementType movementType,
                              String balanceAfter, Long counterpartyAccountId) {
        return new Movement(10L, REFERENCE, accountId, transactionType, movementType, new BigDecimal("50000"),
                new BigDecimal(balanceAfter), counterpartyAccountId, NOW);
    }
}

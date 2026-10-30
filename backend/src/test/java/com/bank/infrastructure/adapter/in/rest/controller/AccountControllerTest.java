package com.bank.infrastructure.adapter.in.rest.controller;

import com.bank.application.port.in.ChangeAccountStatusUseCase;
import com.bank.application.port.in.GetAccountUseCase;
import com.bank.application.port.in.OpenAccountCommand;
import com.bank.application.port.in.OpenAccountUseCase;
import com.bank.domain.exception.AccountBalanceNotZeroException;
import com.bank.domain.exception.AccountNotFoundException;
import com.bank.domain.exception.DuplicateAccountTypeException;
import com.bank.domain.model.Account;
import com.bank.domain.model.AccountNumber;
import com.bank.domain.model.AccountStatus;
import com.bank.domain.model.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 30, 10, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OpenAccountUseCase openAccount;
    @MockitoBean
    private GetAccountUseCase getAccount;
    @MockitoBean
    private ChangeAccountStatusUseCase changeAccountStatus;

    @Test
    void open_validRequest_returns201WithGeneratedNumber() throws Exception {
        when(openAccount.open(new OpenAccountCommand(7L, AccountType.SAVINGS, false)))
                .thenReturn(account(AccountStatus.ACTIVE, "0"));

        mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clientId\":7,\"type\":\"SAVINGS\",\"gmfExempt\":false}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/accounts/1"))
                .andExpect(jsonPath("$.number").value("5300000001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void open_missingTypeAndClient_returns400AndDoesNotCallUseCase() throws Exception {
        mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gmfExempt\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.clientId").exists())
                .andExpect(jsonPath("$.errors.type").exists());

        verify(openAccount, never()).open(any());
    }

    @Test
    void open_clientAlreadyHasThatType_returns409WithReason() throws Exception {
        when(openAccount.open(any(OpenAccountCommand.class))).thenThrow(
                new DuplicateAccountTypeException(AccountType.SAVINGS, new AccountNumber("5300000001")));

        mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clientId\":7,\"type\":\"SAVINGS\",\"gmfExempt\":false}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("5300000001")));
    }

    @Test
    void getAll_withClientId_returnsOnlyThatClientsAccounts() throws Exception {
        when(getAccount.getByClientId(7L)).thenReturn(List.of(account(AccountStatus.ACTIVE, "0")));

        mockMvc.perform(get("/api/accounts").param("clientId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientId").value(7));

        verify(getAccount, never()).getAll();
    }

    @Test
    void getById_unknownAccount_returns404() throws Exception {
        when(getAccount.getById(9L)).thenThrow(new AccountNotFoundException(9L));

        mockMvc.perform(get("/api/accounts/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivate_returns200WithNewStatus() throws Exception {
        when(changeAccountStatus.deactivate(1L)).thenReturn(account(AccountStatus.INACTIVE, "0"));

        mockMvc.perform(patch("/api/accounts/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void cancel_accountWithBalance_returns422() throws Exception {
        when(changeAccountStatus.cancel(1L)).thenThrow(
                new AccountBalanceNotZeroException(new AccountNumber("5300000001"), new BigDecimal("50000.00")));

        mockMvc.perform(patch("/api/accounts/1/cancel"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").exists());
    }

    private static Account account(AccountStatus status, String balance) {
        return Account.restore(1L, AccountType.SAVINGS, new AccountNumber("5300000001"), status,
                new BigDecimal(balance), new BigDecimal(balance), false, 7L, NOW, NOW);
    }
}

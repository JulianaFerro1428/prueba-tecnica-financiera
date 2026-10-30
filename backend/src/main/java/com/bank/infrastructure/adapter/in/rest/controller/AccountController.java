package com.bank.infrastructure.adapter.in.rest.controller;

import com.bank.application.port.in.ChangeAccountStatusUseCase;
import com.bank.application.port.in.GetAccountUseCase;
import com.bank.application.port.in.OpenAccountUseCase;
import com.bank.domain.model.Account;
import com.bank.infrastructure.adapter.in.rest.dto.AccountResponse;
import com.bank.infrastructure.adapter.in.rest.dto.OpenAccountRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/** Adaptador de entrada HTTP para cuentas. No contiene reglas de negocio. */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final OpenAccountUseCase openAccount;
    private final GetAccountUseCase getAccount;
    private final ChangeAccountStatusUseCase changeAccountStatus;

    public AccountController(OpenAccountUseCase openAccount, GetAccountUseCase getAccount,
                             ChangeAccountStatusUseCase changeAccountStatus) {
        this.openAccount = openAccount;
        this.getAccount = getAccount;
        this.changeAccountStatus = changeAccountStatus;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> open(@Valid @RequestBody OpenAccountRequest request) {
        Account account = openAccount.open(request.toCommand());
        return ResponseEntity.created(URI.create("/api/accounts/" + account.getId()))
                .body(AccountResponse.from(account));
    }

    /** Sin parámetro devuelve todas las cuentas; con ?clientId=7 solo las de ese cliente. */
    @GetMapping
    public List<AccountResponse> getAll(@RequestParam(required = false) Long clientId) {
        List<Account> accounts = clientId == null ? getAccount.getAll() : getAccount.getByClientId(clientId);
        return accounts.stream().map(AccountResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AccountResponse getById(@PathVariable Long id) {
        return AccountResponse.from(getAccount.getById(id));
    }

    // PATCH porque se modifica solo una parte de la cuenta (su estado), no la cuenta completa.
    @PatchMapping("/{id}/activate")
    public AccountResponse activate(@PathVariable Long id) {
        return AccountResponse.from(changeAccountStatus.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    public AccountResponse deactivate(@PathVariable Long id) {
        return AccountResponse.from(changeAccountStatus.deactivate(id));
    }

    @PatchMapping("/{id}/cancel")
    public AccountResponse cancel(@PathVariable Long id) {
        return AccountResponse.from(changeAccountStatus.cancel(id));
    }
}

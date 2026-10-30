package com.bank.application.port.in;

import com.bank.domain.model.Account;

import java.util.List;

public interface GetAccountUseCase {

    Account getById(Long id);

    List<Account> getAll();

    List<Account> getByClientId(Long clientId);
}

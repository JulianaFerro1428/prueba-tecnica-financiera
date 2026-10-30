package com.bank.application.port.in;

import com.bank.domain.model.Account;

public interface OpenAccountUseCase {

    Account open(OpenAccountCommand command);
}

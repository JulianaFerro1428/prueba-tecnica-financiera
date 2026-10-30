package com.bank.application.port.in;

import com.bank.domain.model.Account;

public interface ChangeAccountStatusUseCase {

    Account activate(Long id);

    Account deactivate(Long id);

    Account cancel(Long id);
}

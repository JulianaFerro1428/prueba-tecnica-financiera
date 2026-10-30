package com.bank.application.port.in;

import com.bank.domain.model.AccountType;

/** Datos que se necesitan para abrir una cuenta. El número y el estado los decide el sistema. */
public record OpenAccountCommand(Long clientId, AccountType type, boolean gmfExempt) {
}

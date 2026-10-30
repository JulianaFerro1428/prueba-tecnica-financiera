package com.bank.application.port.in;

import com.bank.domain.model.Account;
import com.bank.domain.model.Movement;

import java.util.List;

/** Estado de cuenta: la cuenta con su saldo actual y sus movimientos, del más reciente al más antiguo. */
public record AccountStatement(Account account, List<Movement> movements) {
}

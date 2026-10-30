package com.bank.application.port.in;

import com.bank.domain.model.Movement;

import java.math.BigDecimal;

public interface WithdrawUseCase {

    Movement withdraw(Long accountId, BigDecimal amount);
}

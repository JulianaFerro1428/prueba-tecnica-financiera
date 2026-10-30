package com.bank.application.port.in;

import com.bank.domain.model.Movement;

import java.math.BigDecimal;

public interface DepositUseCase {

    Movement deposit(Long accountId, BigDecimal amount);
}

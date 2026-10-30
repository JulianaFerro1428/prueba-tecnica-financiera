package com.bank.infrastructure.adapter.in.rest.dto;

import com.bank.domain.model.Account;
import com.bank.domain.model.AccountStatus;
import com.bank.domain.model.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** JSON que el backend devuelve con los datos de una cuenta. */
public record AccountResponse(
        Long id,
        AccountType type,
        String number,
        AccountStatus status,
        BigDecimal balance,
        BigDecimal availableBalance,
        boolean gmfExempt,
        Long clientId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getType(),
                account.getNumber().value(),
                account.getStatus(),
                account.getBalance(),
                account.getAvailableBalance(),
                account.isGmfExempt(),
                account.getClientId(),
                account.getCreatedAt(),
                account.getUpdatedAt());
    }
}

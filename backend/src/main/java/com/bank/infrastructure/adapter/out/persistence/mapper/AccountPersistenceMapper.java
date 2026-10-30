package com.bank.infrastructure.adapter.out.persistence.mapper;

import com.bank.domain.model.Account;
import com.bank.domain.model.AccountNumber;
import com.bank.infrastructure.adapter.out.persistence.entity.AccountEntity;

/** Traduce entre la cuenta del dominio y la fila de la tabla accounts. */
public final class AccountPersistenceMapper {

    private AccountPersistenceMapper() {
    }

    public static AccountEntity toEntity(Account account) {
        return new AccountEntity(
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

    public static Account toDomain(AccountEntity entity) {
        return Account.restore(
                entity.getId(),
                entity.getType(),
                new AccountNumber(entity.getNumber()),
                entity.getStatus(),
                entity.getBalance(),
                entity.getAvailableBalance(),
                entity.isGmfExempt(),
                entity.getClientId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}

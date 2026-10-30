package com.bank.infrastructure.adapter.out.persistence.adapter;

import com.bank.application.port.out.AccountNumberSequencePort;
import com.bank.domain.model.AccountType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Pide el siguiente valor a la secuencia de PostgreSQL de cada tipo de cuenta.
 * La base de datos garantiza que nunca entrega el mismo valor dos veces, aunque haya peticiones simultáneas.
 */
@Component
public class AccountNumberSequenceAdapter implements AccountNumberSequencePort {

    private final JdbcClient jdbcClient;

    public AccountNumberSequenceAdapter(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public long nextSequence(AccountType type) {
        String sequenceName = switch (type) {
            case SAVINGS -> "savings_account_number_seq";
            case CHECKING -> "checking_account_number_seq";
        };
        return jdbcClient.sql("SELECT nextval('" + sequenceName + "')")
                .query(Long.class)
                .single();
    }
}

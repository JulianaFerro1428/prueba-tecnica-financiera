package com.bank.application.port.out;

import com.bank.domain.model.Account;

import java.util.List;
import java.util.Optional;

/** Lo que la aplicación necesita para guardar y buscar cuentas. */
public interface AccountRepositoryPort {

    Account save(Account account);

    Optional<Account> findById(Long id);

    List<Account> findAll();

    List<Account> findByClientId(Long clientId);

    boolean existsByClientId(Long clientId);
}

package com.bank.application.port.out;

import com.bank.domain.model.Account;

import java.util.List;
import java.util.Optional;

/** Lo que la aplicación necesita para guardar y buscar cuentas. */
public interface AccountRepositoryPort {

    Account save(Account account);

    Optional<Account> findById(Long id);

    /**
     * Busca la cuenta y la reserva hasta que termine la transacción actual: otra operación que quiera
     * mover dinero en esa misma cuenta espera su turno. Así dos retiros simultáneos no leen el mismo saldo.
     */
    Optional<Account> findByIdForUpdate(Long id);

    List<Account> findAll();

    List<Account> findByClientId(Long clientId);

    boolean existsByClientId(Long clientId);
}

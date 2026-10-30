package com.bank.application.port.out;

import com.bank.domain.model.Movement;

import java.util.List;

/** Lo que la aplicación necesita para registrar y consultar movimientos. No hay editar ni borrar. */
public interface MovementRepositoryPort {

    Movement save(Movement movement);

    /** Movimientos de una cuenta, del más reciente al más antiguo. */
    List<Movement> findByAccountId(Long accountId);
}

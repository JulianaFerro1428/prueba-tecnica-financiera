package com.bank.application.port.in;

import com.bank.domain.model.Movement;

/** Las dos caras de una transferencia: el débito en el origen y el crédito en el destino. */
public record TransferResult(Movement debit, Movement credit) {
}

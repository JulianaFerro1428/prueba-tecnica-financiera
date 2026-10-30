package com.bank.infrastructure.adapter.in.rest.dto;

import com.bank.application.port.in.TransferResult;

import java.util.UUID;

/** JSON de una transferencia: el comprobante y sus dos movimientos. */
public record TransferResponse(UUID reference, MovementResponse debit, MovementResponse credit) {

    public static TransferResponse from(TransferResult result) {
        return new TransferResponse(
                result.debit().reference(),
                MovementResponse.from(result.debit()),
                MovementResponse.from(result.credit()));
    }
}

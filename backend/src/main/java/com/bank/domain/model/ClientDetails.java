package com.bank.domain.model;

import java.time.LocalDate;

/**
 * Datos personales que el trabajador del banco escribe al crear o actualizar un cliente.
 * Se agrupan para no pasar seis parámetros sueltos en cada método.
 */
public record ClientDetails(
        IdentificationType identificationType,
        String identificationNumber,
        String firstNames,
        String lastName,
        String email,
        LocalDate birthDate
) {
}

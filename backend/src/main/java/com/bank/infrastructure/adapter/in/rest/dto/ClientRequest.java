package com.bank.infrastructure.adapter.in.rest.dto;

import com.bank.domain.model.ClientDetails;
import com.bank.domain.model.IdentificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * JSON que envía el frontend para crear o actualizar un cliente.
 * Aquí se revisa el formato de los datos; las reglas del banco (edad) las revisa el dominio.
 */
public record ClientRequest(
        @NotNull(message = "El tipo de identificación es obligatorio")
        IdentificationType identificationType,

        @NotBlank(message = "El número de identificación es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9]{3,20}$", message = "El número de identificación debe tener entre 3 y 20 letras o dígitos")
        String identificationNumber,

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(min = 2, max = 100, message = "Los nombres deben tener entre 2 y 100 caracteres")
        String firstNames,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
        String lastName,

        // @Email por sí solo acepta "maria@mail"; el PDF pide el formato xxxx@xxxxx.xxx.
        @NotBlank(message = "El correo es obligatorio")
        @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "El correo debe tener el formato usuario@dominio.com")
        @Size(max = 150, message = "El correo no puede superar 150 caracteres")
        String email,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        LocalDate birthDate
) {

    public ClientDetails toDetails() {
        return new ClientDetails(identificationType, identificationNumber.trim(),
                firstNames.trim(), lastName.trim(), email.trim().toLowerCase(), birthDate);
    }
}

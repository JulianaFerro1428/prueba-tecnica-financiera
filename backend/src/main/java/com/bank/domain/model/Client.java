package com.bank.domain.model;

import com.bank.domain.exception.UnderageClientException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Objects;

/**
 * Cliente del banco. Es Java puro: no sabe de bases de datos ni de HTTP,
 * solo protege sus reglas (un cliente siempre es mayor de edad).
 */
public class Client {

    private static final int ADULT_AGE = 18;

    private final Long id;
    private ClientDetails details;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Client(Long id, ClientDetails details, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.details = Objects.requireNonNull(details, "details");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    /** Registra un cliente nuevo; todavía no tiene id porque no se ha guardado. */
    public static Client create(ClientDetails details, LocalDateTime now) {
        ensureAdult(details.birthDate(), now.toLocalDate());
        return new Client(null, details, now, now);
    }

    /** Reconstruye un cliente que ya estaba guardado, sin volver a calcular sus fechas. */
    public static Client restore(Long id, ClientDetails details, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Client(id, details, createdAt, updatedAt);
    }

    /** Cambia los datos validando antes, para no dejar al cliente a medio modificar si la regla falla. */
    public void update(ClientDetails newDetails, LocalDateTime now) {
        ensureAdult(newDetails.birthDate(), now.toLocalDate());
        this.details = newDetails;
        this.updatedAt = now;
    }

    private static void ensureAdult(LocalDate birthDate, LocalDate today) {
        Objects.requireNonNull(birthDate, "birthDate");
        if (Period.between(birthDate, today).getYears() < ADULT_AGE) {
            throw new UnderageClientException();
        }
    }

    public Long getId() {
        return id;
    }

    public ClientDetails getDetails() {
        return details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

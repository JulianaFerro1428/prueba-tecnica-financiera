package com.bank.infrastructure.adapter.out.persistence.entity;

import com.bank.domain.model.IdentificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa una fila de la tabla clients. Solo la usa el adaptador de persistencia;
 * las reglas del negocio están en el modelo de dominio Client.
 */
@Entity
@Table(name = "clients")
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "identification_type", nullable = false, length = 2)
    private IdentificationType identificationType;

    @Column(name = "identification_number", nullable = false, length = 20)
    private String identificationNumber;

    @Column(name = "first_names", nullable = false, length = 100)
    private String firstNames;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPA necesita un constructor vacío para crear el objeto al leer una fila.
    protected ClientEntity() {
    }

    public ClientEntity(Long id, IdentificationType identificationType, String identificationNumber,
                        String firstNames, String lastName, String email, LocalDate birthDate,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.identificationType = identificationType;
        this.identificationNumber = identificationNumber;
        this.firstNames = firstNames;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public IdentificationType getIdentificationType() {
        return identificationType;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public String getFirstNames() {
        return firstNames;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

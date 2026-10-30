package com.bank.infrastructure.adapter.out.persistence.repository;

import com.bank.domain.model.IdentificationType;
import com.bank.infrastructure.adapter.out.persistence.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data genera el SQL de estos métodos a partir de sus nombres. */
public interface SpringDataClientRepository extends JpaRepository<ClientEntity, Long> {

    Optional<ClientEntity> findByIdentificationTypeAndIdentificationNumber(
            IdentificationType identificationType, String identificationNumber);
}

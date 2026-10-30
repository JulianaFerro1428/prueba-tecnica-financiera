package com.bank.infrastructure.adapter.out.persistence.repository;

import com.bank.infrastructure.adapter.out.persistence.entity.MovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataMovementRepository extends JpaRepository<MovementEntity, Long> {

    // El id desempata movimientos con la misma fecha (por ejemplo, las dos partes de una transferencia).
    List<MovementEntity> findByAccountIdOrderByCreatedAtDescIdDesc(Long accountId);
}

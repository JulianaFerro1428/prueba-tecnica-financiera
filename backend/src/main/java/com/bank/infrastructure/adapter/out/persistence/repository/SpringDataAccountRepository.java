package com.bank.infrastructure.adapter.out.persistence.repository;

import com.bank.infrastructure.adapter.out.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataAccountRepository extends JpaRepository<AccountEntity, Long> {

    List<AccountEntity> findByClientIdOrderByIdAsc(Long clientId);

    boolean existsByClientId(Long clientId);
}

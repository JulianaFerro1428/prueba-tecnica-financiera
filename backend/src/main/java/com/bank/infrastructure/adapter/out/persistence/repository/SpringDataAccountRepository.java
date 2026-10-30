package com.bank.infrastructure.adapter.out.persistence.repository;

import com.bank.infrastructure.adapter.out.persistence.entity.AccountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SpringDataAccountRepository extends JpaRepository<AccountEntity, Long> {

    List<AccountEntity> findByClientIdOrderByIdAsc(Long clientId);

    boolean existsByClientId(Long clientId);

    // PESSIMISTIC_WRITE genera "SELECT ... FOR UPDATE": PostgreSQL bloquea la fila hasta el COMMIT o ROLLBACK.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountEntity a WHERE a.id = :id")
    Optional<AccountEntity> findByIdForUpdate(Long id);
}

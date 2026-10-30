package com.bank.application.service;

import com.bank.application.port.in.CreateClientUseCase;
import com.bank.application.port.in.DepositUseCase;
import com.bank.application.port.in.GetAccountStatementUseCase;
import com.bank.application.port.in.GetAccountUseCase;
import com.bank.application.port.in.OpenAccountCommand;
import com.bank.application.port.in.OpenAccountUseCase;
import com.bank.application.port.in.TransferCommand;
import com.bank.application.port.in.TransferUseCase;
import com.bank.application.port.out.MovementRepositoryPort;
import com.bank.domain.model.Account;
import com.bank.domain.model.AccountType;
import com.bank.domain.model.ClientDetails;
import com.bank.domain.model.IdentificationType;
import com.bank.domain.model.Movement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;

/**
 * Prueba con PostgreSQL real (en un contenedor desechable) que la transferencia es todo o nada:
 * las pruebas unitarias usan repositorios falsos y no pueden demostrar el ROLLBACK de la base de datos.
 */
@SpringBootTest
@Testcontainers
class TransferIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private CreateClientUseCase createClient;
    @Autowired
    private OpenAccountUseCase openAccount;
    @Autowired
    private DepositUseCase deposit;
    @Autowired
    private TransferUseCase transfer;
    @Autowired
    private GetAccountUseCase getAccount;
    @Autowired
    private GetAccountStatementUseCase getStatement;

    // Envuelve el adaptador real: funciona normal, salvo cuando la prueba le pide fallar.
    @MockitoSpyBean
    private MovementRepositoryPort movementRepository;

    @Test
    void transfer_success_persistsBothBalancesAndBothMovements() {
        Long clientId = createClient("1000000001");
        Account source = openAccount.open(new OpenAccountCommand(clientId, AccountType.SAVINGS, false));
        Account target = openAccount.open(new OpenAccountCommand(clientId, AccountType.CHECKING, false));
        deposit.deposit(source.getId(), new BigDecimal("100000"));

        transfer.transfer(new TransferCommand(source.getId(), target.getId(), new BigDecimal("50000")));

        assertThat(getAccount.getById(source.getId()).getBalance()).isEqualByComparingTo("50000");
        assertThat(getAccount.getById(target.getId()).getBalance()).isEqualByComparingTo("50000");
        assertThat(getStatement.getStatement(source.getId()).movements()).hasSize(2);
        assertThat(getStatement.getStatement(target.getId()).movements()).hasSize(1);
    }

    @Test
    void transfer_failsAfterDebitWasSaved_rollsBackEverything() {
        Long clientId = createClient("1000000002");
        Account source = openAccount.open(new OpenAccountCommand(clientId, AccountType.SAVINGS, false));
        Account target = openAccount.open(new OpenAccountCommand(clientId, AccountType.CHECKING, false));
        deposit.deposit(source.getId(), new BigDecimal("100000"));

        // Simula una caída a mitad de camino: los dos saldos y el débito ya se escribieron,
        // pero al guardar el crédito algo falla.
        doCallRealMethod()
                .doThrow(new IllegalStateException("Fallo simulado al guardar el crédito"))
                .when(movementRepository).save(any(Movement.class));

        assertThatThrownBy(() -> transfer.transfer(
                new TransferCommand(source.getId(), target.getId(), new BigDecimal("50000"))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(getAccount.getById(source.getId()).getBalance()).isEqualByComparingTo("100000");
        assertThat(getAccount.getById(target.getId()).getBalance()).isEqualByComparingTo("0");
        assertThat(getStatement.getStatement(source.getId()).movements()).hasSize(1);
        assertThat(getStatement.getStatement(target.getId()).movements()).isEmpty();
    }

    private Long createClient(String identificationNumber) {
        return createClient.create(new ClientDetails(IdentificationType.CC, identificationNumber, "Maria",
                "Gomez", identificationNumber + "@correo.com", LocalDate.of(1990, 5, 10))).getId();
    }
}

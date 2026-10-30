package com.bank.application.service;

import com.bank.application.port.out.AccountRepositoryPort;
import com.bank.application.port.out.ClientRepositoryPort;
import com.bank.domain.exception.ClientHasAccountsException;
import com.bank.domain.exception.ClientNotFoundException;
import com.bank.domain.exception.DuplicateIdentificationException;
import com.bank.domain.exception.UnderageClientException;
import com.bank.domain.model.Client;
import com.bank.domain.model.ClientDetails;
import com.bank.domain.model.IdentificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 30, 10, 0);
    private static final ClientDetails MARIA = new ClientDetails(IdentificationType.CC, "1012345678",
            "María", "Gómez", "maria@mail.com", LocalDate.of(1995, 5, 20));

    @Mock
    private ClientRepositoryPort clientRepository;
    @Mock
    private AccountRepositoryPort accountRepository;

    private ClientService clientService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        clientService = new ClientService(clientRepository, accountRepository, fixedClock);
    }

    @Test
    void create_newAdultClient_isSavedWithAutomaticDates() {
        when(clientRepository.findByIdentification(IdentificationType.CC, "1012345678")).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenAnswer(call -> call.getArgument(0));

        Client created = clientService.create(MARIA);

        assertThat(created.getCreatedAt()).isEqualTo(NOW);
        assertThat(created.getUpdatedAt()).isEqualTo(NOW);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void create_duplicateIdentification_isRejectedAndNotSaved() {
        when(clientRepository.findByIdentification(IdentificationType.CC, "1012345678"))
                .thenReturn(Optional.of(storedClient(7L)));

        assertThatThrownBy(() -> clientService.create(MARIA))
                .isInstanceOf(DuplicateIdentificationException.class);
        verify(clientRepository, never()).save(any());
    }

    @Test
    void create_underageClient_isRejectedAndNotSaved() {
        ClientDetails minor = new ClientDetails(IdentificationType.CC, "1099999999",
                "Pedro", "Ruiz", "pedro@mail.com", LocalDate.of(2010, 1, 1));

        assertThatThrownBy(() -> clientService.create(minor))
                .isInstanceOf(UnderageClientException.class);
        verify(clientRepository, never()).save(any());
    }

    @Test
    void getById_unknownClient_throwsNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getById(99L))
                .isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    void update_existingClient_changesDataAndModificationDate() {
        when(clientRepository.findById(7L)).thenReturn(Optional.of(storedClient(7L)));
        when(clientRepository.findByIdentification(IdentificationType.CC, "1012345678"))
                .thenReturn(Optional.of(storedClient(7L)));
        when(clientRepository.save(any(Client.class))).thenAnswer(call -> call.getArgument(0));
        ClientDetails newEmail = new ClientDetails(IdentificationType.CC, "1012345678",
                "María", "Gómez", "maria.nueva@mail.com", LocalDate.of(1995, 5, 20));

        Client updated = clientService.update(7L, newEmail);

        assertThat(updated.getDetails().email()).isEqualTo("maria.nueva@mail.com");
        assertThat(updated.getCreatedAt()).isEqualTo(NOW.minusYears(1));
        assertThat(updated.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void update_identificationOfAnotherClient_isRejected() {
        when(clientRepository.findById(7L)).thenReturn(Optional.of(storedClient(7L)));
        when(clientRepository.findByIdentification(IdentificationType.CC, "1012345678"))
                .thenReturn(Optional.of(storedClient(8L)));

        assertThatThrownBy(() -> clientService.update(7L, MARIA))
                .isInstanceOf(DuplicateIdentificationException.class);
        verify(clientRepository, never()).save(any());
    }

    @Test
    void delete_clientWithoutAccounts_isDeleted() {
        when(clientRepository.findById(7L)).thenReturn(Optional.of(storedClient(7L)));
        when(accountRepository.existsByClientId(7L)).thenReturn(false);

        clientService.delete(7L);

        verify(clientRepository).deleteById(7L);
    }

    @Test
    void delete_clientWithAccounts_isRejectedAndNotDeleted() {
        when(clientRepository.findById(7L)).thenReturn(Optional.of(storedClient(7L)));
        when(accountRepository.existsByClientId(7L)).thenReturn(true);

        assertThatThrownBy(() -> clientService.delete(7L))
                .isInstanceOf(ClientHasAccountsException.class);
        verify(clientRepository, never()).deleteById(any());
    }

    @Test
    void delete_unknownClient_throwsNotFoundAndDeletesNothing() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.delete(99L))
                .isInstanceOf(ClientNotFoundException.class);
        verify(clientRepository, never()).deleteById(any());
    }

    private static Client storedClient(Long id) {
        LocalDateTime createdAt = NOW.minusYears(1);
        return Client.restore(id, MARIA, createdAt, createdAt);
    }
}

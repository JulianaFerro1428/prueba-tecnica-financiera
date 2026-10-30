package com.bank.domain.model;

import com.bank.domain.exception.UnderageClientException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClientTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 30, 10, 0);

    @Test
    void create_adultClient_setsCreationAndModificationDates() {
        Client client = Client.create(detailsBornOn(LocalDate.of(1995, 5, 20)), NOW);

        assertThat(client.getId()).isNull();
        assertThat(client.getCreatedAt()).isEqualTo(NOW);
        assertThat(client.getUpdatedAt()).isEqualTo(NOW);
        assertThat(client.getDetails().firstNames()).isEqualTo("María");
    }

    @Test
    void create_clientTurningEighteenToday_isAllowed() {
        Client client = Client.create(detailsBornOn(LocalDate.of(2008, 10, 30)), NOW);

        assertThat(client.getDetails().birthDate()).isEqualTo(LocalDate.of(2008, 10, 30));
    }

    @Test
    void create_clientTurningEighteenTomorrow_isRejected() {
        ClientDetails underage = detailsBornOn(LocalDate.of(2008, 10, 31));

        assertThatThrownBy(() -> Client.create(underage, NOW))
                .isInstanceOf(UnderageClientException.class);
    }

    @Test
    void update_changesDetailsAndModificationDateButKeepsCreationDate() {
        Client client = Client.create(detailsBornOn(LocalDate.of(1995, 5, 20)), NOW);
        LocalDateTime later = NOW.plusDays(3);
        ClientDetails newDetails = new ClientDetails(IdentificationType.CC, "1012345678",
                "María José", "Gómez", "maria.jose@mail.com", LocalDate.of(1995, 5, 20));

        client.update(newDetails, later);

        assertThat(client.getDetails().firstNames()).isEqualTo("María José");
        assertThat(client.getCreatedAt()).isEqualTo(NOW);
        assertThat(client.getUpdatedAt()).isEqualTo(later);
    }

    @Test
    void update_birthDateThatMakesClientUnderage_isRejectedAndKeepsOldData() {
        Client client = Client.create(detailsBornOn(LocalDate.of(1995, 5, 20)), NOW);

        assertThatThrownBy(() -> client.update(detailsBornOn(LocalDate.of(2012, 1, 1)), NOW.plusDays(1)))
                .isInstanceOf(UnderageClientException.class);
        assertThat(client.getDetails().birthDate()).isEqualTo(LocalDate.of(1995, 5, 20));
        assertThat(client.getUpdatedAt()).isEqualTo(NOW);
    }

    private static ClientDetails detailsBornOn(LocalDate birthDate) {
        return new ClientDetails(IdentificationType.CC, "1012345678",
                "María", "Gómez", "maria@mail.com", birthDate);
    }
}

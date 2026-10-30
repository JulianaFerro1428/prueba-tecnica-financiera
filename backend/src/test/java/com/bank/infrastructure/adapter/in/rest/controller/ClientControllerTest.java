package com.bank.infrastructure.adapter.in.rest.controller;

import com.bank.application.port.in.CreateClientUseCase;
import com.bank.application.port.in.DeleteClientUseCase;
import com.bank.application.port.in.GetClientUseCase;
import com.bank.application.port.in.UpdateClientUseCase;
import com.bank.domain.exception.ClientNotFoundException;
import com.bank.domain.exception.DuplicateIdentificationException;
import com.bank.domain.exception.UnderageClientException;
import com.bank.domain.model.Client;
import com.bank.domain.model.ClientDetails;
import com.bank.domain.model.IdentificationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 30, 10, 0);
    private static final ClientDetails MARIA = new ClientDetails(IdentificationType.CC, "1012345678",
            "María", "Gómez", "maria@mail.com", LocalDate.of(1995, 5, 20));
    private static final String VALID_BODY = """
            {
              "identificationType": "CC",
              "identificationNumber": "1012345678",
              "firstNames": "María",
              "lastName": "Gómez",
              "email": "maria@mail.com",
              "birthDate": "1995-05-20"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateClientUseCase createClient;
    @MockitoBean
    private GetClientUseCase getClient;
    @MockitoBean
    private UpdateClientUseCase updateClient;
    @MockitoBean
    private DeleteClientUseCase deleteClient;

    @Test
    void create_validRequest_returns201WithLocationAndBody() throws Exception {
        when(createClient.create(MARIA)).thenReturn(Client.restore(1L, MARIA, CREATED_AT, CREATED_AT));

        mockMvc.perform(post("/api/clients").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/clients/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstNames").value("María"))
                .andExpect(jsonPath("$.createdAt").value("2026-10-30T10:00:00"));
    }

    @Test
    void create_invalidEmailAndShortName_returns400AndDoesNotCallUseCase() throws Exception {
        String invalidBody = VALID_BODY.replace("maria@mail.com", "maria@mail").replace("\"María\"", "\"M\"");

        mockMvc.perform(post("/api/clients").contentType(MediaType.APPLICATION_JSON).content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.firstNames").exists());

        verify(createClient, never()).create(any());
    }

    @Test
    void create_underageClient_returns422WithMessage() throws Exception {
        when(createClient.create(any())).thenThrow(new UnderageClientException());

        mockMvc.perform(post("/api/clients").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("El cliente debe ser mayor de edad"));
    }

    @Test
    void create_duplicateIdentification_returns409() throws Exception {
        when(createClient.create(any()))
                .thenThrow(new DuplicateIdentificationException(IdentificationType.CC, "1012345678"));

        mockMvc.perform(post("/api/clients").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void getById_unknownClient_returns404() throws Exception {
        when(getClient.getById(99L)).thenThrow(new ClientNotFoundException(99L));

        mockMvc.perform(get("/api/clients/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe un cliente con id 99"));
    }

    @Test
    void getById_nonNumericId_returns400() throws Exception {
        mockMvc.perform(get("/api/clients/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_returnsClientList() throws Exception {
        when(getClient.getAll()).thenReturn(List.of(Client.restore(1L, MARIA, CREATED_AT, CREATED_AT)));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].identificationNumber").value("1012345678"));
    }

    @Test
    void getById_existingClient_returns200() throws Exception {
        when(getClient.getById(1L)).thenReturn(Client.restore(1L, MARIA, CREATED_AT, CREATED_AT));

        mockMvc.perform(get("/api/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("maria@mail.com"));
    }

    @Test
    void update_validRequest_returns200WithNewModificationDate() throws Exception {
        LocalDateTime updatedAt = CREATED_AT.plusDays(2);
        when(updateClient.update(eq(1L), any(ClientDetails.class)))
                .thenReturn(Client.restore(1L, MARIA, CREATED_AT, updatedAt));

        mockMvc.perform(put("/api/clients/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedAt").value("2026-11-01T10:00:00"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isNoContent());

        verify(deleteClient).delete(1L);
    }
}

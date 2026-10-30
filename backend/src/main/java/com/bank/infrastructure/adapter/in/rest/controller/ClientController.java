package com.bank.infrastructure.adapter.in.rest.controller;

import com.bank.application.port.in.CreateClientUseCase;
import com.bank.application.port.in.DeleteClientUseCase;
import com.bank.application.port.in.GetClientUseCase;
import com.bank.application.port.in.UpdateClientUseCase;
import com.bank.domain.model.Client;
import com.bank.infrastructure.adapter.in.rest.dto.ClientRequest;
import com.bank.infrastructure.adapter.in.rest.dto.ClientResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Adaptador de entrada HTTP: convierte peticiones REST en llamadas a los casos de uso
 * y sus resultados en JSON. No contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final CreateClientUseCase createClient;
    private final GetClientUseCase getClient;
    private final UpdateClientUseCase updateClient;
    private final DeleteClientUseCase deleteClient;

    public ClientController(CreateClientUseCase createClient, GetClientUseCase getClient,
                            UpdateClientUseCase updateClient, DeleteClientUseCase deleteClient) {
        this.createClient = createClient;
        this.getClient = getClient;
        this.updateClient = updateClient;
        this.deleteClient = deleteClient;
    }

    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
        Client client = createClient.create(request.toDetails());
        return ResponseEntity.created(URI.create("/api/clients/" + client.getId()))
                .body(ClientResponse.from(client));
    }

    @GetMapping
    public List<ClientResponse> getAll() {
        return getClient.getAll().stream().map(ClientResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ClientResponse getById(@PathVariable Long id) {
        return ClientResponse.from(getClient.getById(id));
    }

    @PutMapping("/{id}")
    public ClientResponse update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return ClientResponse.from(updateClient.update(id, request.toDetails()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deleteClient.delete(id);
    }
}

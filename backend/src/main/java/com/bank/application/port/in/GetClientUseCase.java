package com.bank.application.port.in;

import com.bank.domain.model.Client;

import java.util.List;

public interface GetClientUseCase {

    Client getById(Long id);

    List<Client> getAll();
}

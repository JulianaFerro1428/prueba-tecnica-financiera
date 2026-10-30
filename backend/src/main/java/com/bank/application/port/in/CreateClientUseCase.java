package com.bank.application.port.in;

import com.bank.domain.model.Client;
import com.bank.domain.model.ClientDetails;

public interface CreateClientUseCase {

    Client create(ClientDetails details);
}

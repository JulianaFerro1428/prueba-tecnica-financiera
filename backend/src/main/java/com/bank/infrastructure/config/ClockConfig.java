package com.bank.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Entrega el reloj del sistema a los casos de uso. En las pruebas se reemplaza
 * por un reloj fijo para que "hoy" no cambie y los resultados sean repetibles.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}

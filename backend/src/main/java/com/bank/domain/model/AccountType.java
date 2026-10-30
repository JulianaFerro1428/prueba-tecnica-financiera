package com.bank.domain.model;

/** Los dos únicos productos que permite el enunciado, cada uno con el prefijo de su número. */
public enum AccountType {
    SAVINGS("53"),  // Cuenta de ahorros
    CHECKING("33"); // Cuenta corriente

    private final String numberPrefix;

    AccountType(String numberPrefix) {
        this.numberPrefix = numberPrefix;
    }

    public String numberPrefix() {
        return numberPrefix;
    }
}
